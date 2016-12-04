package com.javafx.experiments.importers.dae.parser;

import org.xml.sax.Attributes;

/**
 * @author Eclion
 */
final class ParserUtils {

    static float[] extractFloatArray(StringBuilder charBuf) {
        String[] numbers = charBuf.toString().trim().split("\\s+");
        float[] array = new float[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            array[i] = Float.parseFloat(numbers[i].trim());
        }
        return array;
    }

    static Input createInput(Attributes attributes) {
        return new Input(
                attributes.getValue("offset") != null ? Integer.parseInt(attributes.getValue("offset")) : 0,
                attributes.getValue("semantic"),
                attributes.getValue("source"));
    }
}