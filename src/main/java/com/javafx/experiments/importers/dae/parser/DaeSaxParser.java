package com.javafx.experiments.importers.dae.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eclion
 */
final public class DaeSaxParser extends DefaultHandler {
    private DefaultHandler subHandler;
    private Map<String, DefaultHandler> finishedHandlers = new HashMap<>();

    private enum State {
        UNKNOWN,
        asset,
        scene,
        library_animations,
        library_cameras,
        library_controllers,
        library_geometries,
        library_images,
        library_lights,
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
        System.out.println("start: " + qName);
        switch (state(qName)) {
            case asset:
                subHandler = new AssetParser();
                break;
            case scene:
                subHandler = new SceneParser();
                break;
            case library_animations:
                subHandler = new LibraryAnimationsParser();
                break;
            case library_cameras:
                subHandler = new LibraryCamerasParser();
                break;
            case library_controllers:
                subHandler = new LibraryControllers();
                break;
            case library_geometries:
                subHandler = new LibraryGeometriesParser();
                break;
            case library_images:
                subHandler = new LibraryImagesParser();
                break;
            case library_lights:
                subHandler = new LibraryLightsParser();
                break;
            case library_visual_scenes:
                subHandler = new LibraryVisualSceneParser();
                break;
            default:
                if (subHandler != null) subHandler.startElement(uri, localName, qName, attributes);
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        System.out.println("start: " + qName);
        switch (state(qName)) {
            case asset:
            case library_images:
            case library_geometries:
            case library_animations:
            case library_controllers:
            case library_visual_scenes:
            case scene:
                finishedHandlers.put(qName, subHandler);
                break;
            default:
                if (subHandler != null) subHandler.endElement(uri, localName, qName);
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (subHandler != null) subHandler.characters(ch, start, length);
    }
}
