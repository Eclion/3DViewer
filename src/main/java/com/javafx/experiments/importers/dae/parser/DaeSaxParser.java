package com.javafx.experiments.importers.dae.parser;

import com.javafx.experiments.shape3d.PolygonMesh;
import com.javafx.experiments.shape3d.PolygonMeshView;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.Light;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

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
                ? 4.0/3.0
                : libraryCamerasParser.firstCameraAspectRatio;
    }

    public void buildScene(Group rootNode) {

        DaeNode sceneNode = libraryVisualSceneParser.nodes.pop();
        if (sceneNode == null) return;

        rootNode.setId(sceneNode.name);
        List<Node> nodes = dive(sceneNode);
        rootNode.getChildren().addAll(nodes);
    }

    private List<Node> dive(DaeNode parent) {
        List<Node> nodes = new ArrayList<>();
        for (DaeNode child : parent.children) {
            if (child.isCamera()) {
                Camera camera = libraryCamerasParser.cameras.get(child.instance_camera_id);
                camera.setId(child.name);
                camera.getTransforms().addAll(child.transforms);
                nodes.add(camera);
            } else if (child.isLight()) {
                //Light light = libraryLightsParser.lights.get(child.instance_light_id);
            } else if (child.isMesh()) {
                Node meshView;
                Object mesh = libraryGeometriesParser.meshes.get(child.instance_geometry_id);
                if (mesh instanceof PolygonMesh) {
                    meshView = new PolygonMeshView((PolygonMesh) mesh);
                } else {
                    meshView = new MeshView((TriangleMesh) mesh);
                }
                meshView.setId(child.name);
                meshView.getTransforms().addAll(child.transforms);
                nodes.add(meshView);
            } else {
                Group group = new Group();
                group.setId(child.name);
                group.getTransforms().addAll(child.transforms);
                group.getChildren().addAll(dive(child));
                nodes.add(group);
            }
        }
        return nodes;
    }
}
