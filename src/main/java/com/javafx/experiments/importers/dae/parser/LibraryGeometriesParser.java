package com.javafx.experiments.importers.dae.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eclion
 */
final class LibraryGeometriesParser extends DefaultHandler {
    private StringBuilder charBuf = new StringBuilder();
    private Map<String, String> currentId = new HashMap<>();
    private Map<String, float[]> floatArrays = new HashMap<>();

    private enum State {
        UNKNOWN,
        accessor,
        float_array,
        geometry,
        input,
        mesh,
        p,
        param,
        polygons,
        polylist,
        source,
        technique_common,
        vcount,
        vertices
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
            case accessor:
                break;
            case float_array:
                floatArrays.put(currentId.get("source"),
                        ParserUtils.extractFloatArray(charBuf));
                break;
            case geometry:
            case input:
            case mesh:
            case p:
            case param:
            case polygons:
            case polylist:
            case source:
            case technique_common:
            case vcount:
            case vertices:
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
