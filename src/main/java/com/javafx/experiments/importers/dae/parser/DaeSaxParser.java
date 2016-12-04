package com.javafx.experiments.importers.dae.parser;

import javafx.scene.Camera;
import javafx.scene.Group;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
    private double firstCameraAspectRatio = 4.0f/3.0f;

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
        return firstCamera;
    }

    public double getFirstCameraAspectRatio() {
        return firstCameraAspectRatio;
    }

    public void build() {
        rootNode = libraryVisualSceneParser.getRootNode();
        if (libraryCamerasParser != null){
            firstCamera = libraryCamerasParser.getFirstCamera();
            firstCameraAspectRatio = libraryCamerasParser.getFirstCameraAspectRatio();
        }
    }
}
