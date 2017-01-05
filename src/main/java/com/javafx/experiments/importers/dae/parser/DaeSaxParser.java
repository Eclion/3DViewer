package com.javafx.experiments.importers.dae.parser;

import com.javafx.experiments.importers.dae.parser.libraries.DaeController;
import com.javafx.experiments.importers.dae.parser.libraries.DaeNode;
import com.javafx.experiments.importers.dae.parser.libraries.DaeScene;
import com.javafx.experiments.importers.dae.parser.libraries.DaeSkeleton;
import com.javafx.experiments.importers.maya.Joint;
import com.javafx.experiments.shape3d.PolygonMesh;
import com.javafx.experiments.shape3d.PolygonMeshView;
import com.javafx.experiments.shape3d.SkinningMesh;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Eclion
 */
final public class DaeSaxParser extends DefaultHandler {
    private DefaultHandler subHandler;

    AssetParser assetParser;
    SceneParser sceneParser;
    LibraryAnimationsParser libraryAnimationsParser;
    LibraryCamerasParser libraryCamerasParser;
    LibraryControllerParser libraryControllerParser;
    LibraryEffectsParser libraryEffects;
    LibraryGeometriesParser libraryGeometriesParser;
    LibraryImagesParser libraryImagesParser;
    LibraryLightsParser libraryLightsParser;
    LibraryMaterialsParser libraryMaterialsParser;
    LibraryVisualSceneParser libraryVisualSceneParser;
    private boolean createPolyMesh = true;
    private Camera firstCamera;
    private Group rootNode;
    private double firstCameraAspectRatio = 4.0f / 3.0f;

    private enum State {
        UNKNOWN,
        asset,
        scene,
        library_animations,
        library_cameras,
        library_controllers,
        library_effects,
        library_geometries,
        library_images,
        library_lights,
        library_materials,
        library_visual_scenes
    }

    private static State state(String name) {
        try {
            return State.valueOf(name);
        } catch (Exception e) {
            return State.UNKNOWN;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (state(qName)) {
            case asset:
                assetParser = new AssetParser();
                subHandler = assetParser;
                break;
            case scene:
                sceneParser = new SceneParser();
                subHandler = sceneParser;
                break;
            case library_animations:
                libraryAnimationsParser = new LibraryAnimationsParser();
                subHandler = libraryAnimationsParser;
                break;
            case library_cameras:
                libraryCamerasParser = new LibraryCamerasParser();
                subHandler = libraryCamerasParser;
                break;
            case library_controllers:
                libraryControllerParser = new LibraryControllerParser();
                subHandler = libraryControllerParser;
                break;
            case library_effects:
                libraryEffects = new LibraryEffectsParser();
                subHandler = libraryEffects;
                break;
            case library_geometries:
                libraryGeometriesParser = new LibraryGeometriesParser();
                libraryGeometriesParser.setCreatePolyMesh(createPolyMesh);
                subHandler = libraryGeometriesParser;
                break;
            case library_images:
                libraryImagesParser = new LibraryImagesParser();
                subHandler = libraryImagesParser;
                break;
            case library_lights:
                libraryLightsParser = new LibraryLightsParser();
                subHandler = libraryLightsParser;
                break;
            case library_materials:
                libraryMaterialsParser = new LibraryMaterialsParser();
                subHandler = libraryMaterialsParser;
                break;
            case library_visual_scenes:
                libraryVisualSceneParser = new LibraryVisualSceneParser();
                subHandler = libraryVisualSceneParser;
                break;
            default:
                if (subHandler != null) subHandler.startElement(uri, localName, qName, attributes);
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (subHandler != null) subHandler.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (subHandler != null) subHandler.characters(ch, start, length);
    }

    public void setCreatePolyMesh(boolean createPolyMesh) {
        this.createPolyMesh = createPolyMesh;
    }

    public Group getRootNode() {
        return rootNode;
    }

    public Camera getFirstCamera() {
        return libraryCamerasParser == null
                ? null
                : libraryCamerasParser.firstCamera;
    }

    public double getFirstCameraAspectRatio() {
        return libraryCamerasParser == null
                ? 4.0 / 3.0
                : libraryCamerasParser.firstCameraAspectRatio;
    }

    public void buildScene(Group rootNode) {

        /*DaeNode sceneNode = libraryVisualSceneParser.nodes.peek();
        if (sceneNode == null) return;

        rootNode.setId(sceneNode.name);
        List<Node> nodes = dive(sceneNode);
        rootNode.getChildren().addAll(nodes);*/
        DaeScene scene = libraryVisualSceneParser.scenes.peek();
        rootNode.getChildren().addAll(scene.meshNodes.values().stream().map(this::getMesh).collect(Collectors.toList()));
        rootNode.getChildren().addAll(scene.controllerNodes.values().stream().map(this::getController).collect(Collectors.toList()));
    }

    private List<Node> dive(DaeNode parent) {
        List<Node> nodes = new ArrayList<>();
        for (DaeNode child : parent.children.values()) {
            if (child.isCamera())
                nodes.add(getCamera(child));
            else if (child.isLight()) {
                //Light light = libraryLightsParser.lights.get(child.instance_light_id);
            } else if (child.isMesh()) {
                nodes.add(getMesh(child));
            } else if (child.isController())
                nodes.add(getController(child));
            else {
                Group group = new Group();
                group.setId(child.name);
                group.getTransforms().addAll(child.transforms);
                group.getChildren().addAll(dive(child));
                nodes.add(group);
            }
        }
        return nodes;
    }

    private Camera getCamera(DaeNode node) {
        Camera camera = libraryCamerasParser.cameras.get(node.instance_camera_id);
        camera.setId(node.name);
        camera.getTransforms().addAll(node.transforms);
        return camera;
    }

    private Node getMesh(DaeNode node) {
        Node meshView;
        Object mesh = libraryGeometriesParser.meshes.get(node.instance_geometry_id);
        if (mesh instanceof PolygonMesh) {
            meshView = new PolygonMeshView((PolygonMesh) mesh);
        } else {
            meshView = new MeshView((TriangleMesh) mesh);
        }

        meshView.setId(node.name);
        meshView.getTransforms().addAll(node.transforms);
        return meshView;
    }

    private Node getController(DaeNode node) {
        Node meshView;
        DaeController controller = libraryControllerParser.controllers.get(node.instance_controller_id);
        DaeSkeleton skeleton = (DaeSkeleton) libraryVisualSceneParser.scenes.get(0).skeletons.values().toArray()[0];
        Object mesh = libraryGeometriesParser.meshes.get(controller.skinId);

//        Object mesh = libraryGeometriesParser.meshes.get(node.instance_geometry_id);
        if (mesh instanceof PolygonMesh) {

            String[] bones = skeleton.joints.keySet().toArray(new String[]{});
            Affine[] bindTransforms = new Affine[bones.length];
            Joint[] joints = new Joint[bones.length];
            skeleton.joints.values();
            skeleton.bindTransforms.values();

            for(int i = 0; i < bones.length; i++)
            {
                bindTransforms[i] = controller.bindTransforms.get(i);
                joints[i] = skeleton.joints.get(bones[i]);
            }

            SkinningMesh skinningMesh = new SkinningMesh(
                    (PolygonMesh) mesh, controller.vertexWeights, bindTransforms,
                    controller.bindShapeMatrix, Arrays.asList(joints), Arrays.asList(skeleton));

            meshView = new PolygonMeshView(skinningMesh);

            final SkinningMeshTimer skinningMeshTimer = new SkinningMeshTimer(skinningMesh);
            if (meshView.getScene() != null) {
                skinningMeshTimer.start();
            }
            meshView.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    skinningMeshTimer.stop();
                } else {
                    skinningMeshTimer.start();
                }
            });
        } else {
            meshView = new MeshView((TriangleMesh) mesh);
        }

        meshView.setId(node.name);
        return meshView;
    }

    public List<KeyFrame> getAllKeyFrames() {
        List<KeyFrame> frames = new ArrayList<>();
        libraryVisualSceneParser.scenes.peek().skeletons.values()
                .forEach(skeleton -> libraryAnimationsParser.animations.values()
                        .forEach(animation -> frames.addAll(animation.calculateAnimation(skeleton))));
        return frames;
    }


    private class SkinningMeshTimer extends AnimationTimer {
        private SkinningMesh mesh;

        SkinningMeshTimer(SkinningMesh mesh) {
            this.mesh = mesh;
        }

        @Override
        public void handle(long l) {
            mesh.update();
        }
    }

}
