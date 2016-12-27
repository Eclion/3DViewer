package com.javafx.experiments.importers.dae.parser;

import com.javafx.experiments.importers.dae.parser.libraries.DaeController;
import com.javafx.experiments.importers.dae.parser.libraries.DaeNode;
import com.javafx.experiments.importers.dae.parser.libraries.DaeScene;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
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
        meshView = new MeshView((TriangleMesh) mesh);
        meshView.setId(node.name);
        meshView.getTransforms().addAll(node.transforms);
        return meshView;
    }

    private Node getController(DaeNode node) {
        Node meshView;
        DaeController controller = libraryControllerParser.controllers.get(node.instance_controller_id);
        Object mesh = libraryGeometriesParser.meshes.get(controller.skinId);

        meshView = new MeshView((TriangleMesh) mesh);
        meshView.setId(node.name);

        //PolygonMesh mesh
        //float[][] vertexWeights
        //Affine[] bindTransforms
        //Affine globalTransform
        //List<Joint> joints
        //List<Parent> jointForest
        //SkinningMesh mesh = new SkinningMesh(mesh, vertexWeights, bindTransforms, globalTransform, joints, jointForest);
        //meshView = new PolygonMeshView((PolygonMesh) mesh);
        //cf @Loader l300
        return meshView;
    }
}
