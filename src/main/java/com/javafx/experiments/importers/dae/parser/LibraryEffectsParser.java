package com.javafx.experiments.importers.dae.parser;

import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Eclion
 */
final class LibraryEffectsParser extends DefaultHandler {
    private final static Logger LOGGER = Logger.getLogger(LibraryEffectsParser.class.getSimpleName());
    private StringBuilder charBuf = new StringBuilder();
    private Map<String, String> currentId = new HashMap<>();

    private Map<String, Material> materials = new HashMap<>();

    //private Color ambient;
    private Color diffuse;
    //private Color emission;
    private Color specular;

    //private Float shininess;
    //private Float refractionIndex;

    //private Float tempFloat;
    private Color tempColor;

    private enum State {
        UNKNOWN,
        ambient, //ignored
        color,
        diffuse,
        effect, //ignored
        emission, //ignored
        _float, //ignored
        index_of_refraction, //ignored
        phong,
        profile_COMMON, //ignored
        shininess, //ignored
        specular,
        technique //ignored
    }

    private static State state(String name) {
        try {
            return (!Objects.equals(name, "float"))
                    ? State.valueOf(name)
                    : State._float;
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
            case phong:
                diffuse = specular = null;
                //ambient = diffuse = emission = null;
                //shininess = refractionIndex = null;
                break;
            case color:
                tempColor = null;
                break;
            /*case _float:
                tempFloat = null;
                break;*/
            default:
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (state(qName)) {
            /*case ambient:
                ambient = tempColor;
                break;*/
            case color:
                tempColor = extractColor(charBuf);
            case diffuse:
                diffuse = tempColor;
                break;
            /*case emission:
                emission = tempColor;
                break;
            case _float:
                tempFloat = extractFloat(charBuf);
                break;
            case index_of_refraction:
                refractionIndex = tempFloat;
                break;*/
            case phong:
                PhongMaterial material = new PhongMaterial(diffuse);
                if(specular!=null) material.setSpecularColor(specular);
                materials.put(currentId.get("effect"), material);
                // commented nearly all parameters as only diffuse is used in the JavaFX's phong impl
                break;
            /*case shininess:
                shininess = tempFloat;
                break;*/
            case specular:
                specular = tempColor;
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        charBuf.append(ch, start, length);
    }

    Color extractColor(StringBuilder charBuf) {
        try {
            String[] colors = charBuf.toString().trim().split("\\s+");
            return new Color(
                    Double.parseDouble(colors[0]),
                    Double.parseDouble(colors[1]),
                    Double.parseDouble(colors[2]),
                    Double.parseDouble(colors[3])
            );
        } catch (Exception ignored) {

        }
        return null;
    }

    /*Float extractFloat(StringBuilder charBuf) {
        try {
            return Float.parseFloat(charBuf.toString().trim());
        } catch (Exception ignored) {

        }
        return null;
    }*/
}
