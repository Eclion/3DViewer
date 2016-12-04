package com.javafx.experiments.importers.dae.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eclion
 */
class AssetParser extends DefaultHandler {
    private StringBuilder charBuf = new StringBuilder();
    private Map<String, String> currentId = new HashMap<>();
    private String author;
    private String authoringTool;
    private String unit;
    private float scale;
    private String upAxis;

    private enum State {
        UNKNOWN,
        author,
        authoring_tool,
        //contributor,
        //created,
        //modified,
        unit,
        up_axis

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
            case unit:
                unit = attributes.getValue("name");
                scale = Float.parseFloat(attributes.getValue(unit));
            default:
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (state(qName)) {
            case author:
                author = charBuf.toString().trim();
                break;
            case authoring_tool:
                authoringTool = charBuf.toString().trim();
                break;
            case up_axis:
                upAxis = charBuf.toString().trim();
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        charBuf.append(ch, start, length);
    }
}
