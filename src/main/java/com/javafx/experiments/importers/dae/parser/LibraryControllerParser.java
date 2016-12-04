package com.javafx.experiments.importers.dae.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Eclion
 */
final class LibraryControllerParser extends DefaultHandler {
    private static final Logger LOGGER = Logger.getLogger(LibraryControllerParser.class.getSimpleName());
    private StringBuilder charBuf = new StringBuilder();
    private Map<String, String> currentId = new HashMap<>();
    private Map<String, Input> inputs = new HashMap<>();
    private Map<String, float[]> floatArrays = new HashMap<>();
    private int[] vCounts;

    private enum State {
        UNKNOWN,
        accessor,
        bind_shape_matrix,
        controller,
        float_array,
        input,
        joints,
        Name_array,
        param,
        skin,
        source,
        technique_common,
        v,
        vcount,
        vertex_weights

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
                LOGGER.log(Level.WARNING, "Unknown element: " + qName);
                break;
            case input:
                Input input = ParserUtils.createInput(attributes);
                inputs.put(input.semantic, input);
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
            case float_array:
                floatArrays.put(currentId.get("source"),
                        ParserUtils.extractFloatArray(charBuf));
                break;
            case vcount:
                saveVerticesCounts();
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        charBuf.append(ch, start, length);
    }

    private void saveVerticesCounts() {
        String[] numbers = charBuf.toString().trim().split("\\s+");
        vCounts = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            vCounts[i] = Integer.parseInt(numbers[i].trim());
        }
    }
}
