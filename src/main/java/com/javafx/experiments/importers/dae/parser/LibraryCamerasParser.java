package com.javafx.experiments.importers.dae.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eclion
 */
final class LibraryCamerasParser extends DefaultHandler {
    private StringBuilder charBuf = new StringBuilder();
    private Map<String, String> currentId = new HashMap<>();

    private enum State {
        UNKNOWN,
        aspect_ratio,
        camera,
        extra,
        perspective,
        optics,
        shiftx,
        shifty,
        technique,
        technique_common,
        xfov,
        YF_dofdist,
        zfar,
        znear
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
        currentId.put(qName, attributes.getValue("id"));
        charBuf = new StringBuilder();
        switch (state(qName)) {
            case UNKNOWN:
                System.out.println("Unknown element: " + qName);
                break;
            default:
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (state(qName)) {
            case UNKNOWN:
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        charBuf.append(ch, start, length);
    }
}
