package com.javafx.experiments.importers.dae.parser;

import com.javafx.experiments.importers.dae.parser.libraries.DaeController;
import com.javafx.experiments.importers.dae.parser.libraries.DaeNode;
import com.javafx.experiments.importers.dae.parser.libraries.DaeScene;
import com.javafx.experiments.importers.dae.parser.libraries.DaeSkeleton;
import javafx.geometry.Point3D;
import javafx.scene.transform.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Eclion
 */
final class LibraryVisualSceneParser extends DefaultHandler {
    private final static Logger LOGGER = Logger.getLogger(LibraryVisualSceneParser.class.getSimpleName());
    private StringBuilder charBuf = new StringBuilder();
    private Map<String, String> currentId = new HashMap<>();
    LinkedList<DaeScene> scenes = new LinkedList<>();
    LinkedList<DaeNode> nodes = new LinkedList<>();
    List<DaeController> controllers = new ArrayList<>();

    private enum State {
        UNKNOWN,
        bind_material,
        connect,
        extra,
        instance_camera,
        instance_controller,
        instance_geometry,
        instance_light,
        instance_material,
        layer,
        node,
        matrix,
        rotate,
        skeleton,
        technique,
        technique_common,
        tip_x,
        tip_y,
        tip_z,
        translate,
        scale,
        visual_scene
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
            case instance_camera:
                nodes.peek().instance_camera_id = attributes.getValue("url").substring(1);
                break;
            case instance_controller:
                nodes.peek().instance_controller_id = attributes.getValue("url").substring(1);
                break;
            case instance_geometry:
                nodes.peek().instance_geometry_id = attributes.getValue("url").substring(1);
                break;
            case instance_light:
                nodes.peek().instance_light_id = attributes.getValue("url").substring(1);
                break;
            case instance_material:
                nodes.peek().instance_material_id = attributes.getValue("target").substring(1);
                break;
            case node:
                createDaeNode(attributes);
                break;
            case visual_scene:
                createVisualScene(attributes);
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
            case node:
                setDaeNode();
                break;
            case matrix:
                addMatrixTransformation();
                break;
            case rotate:
                addRotation();
                break;
            case scale:
                addScaling();
                break;
            case translate:
                addTranslation();
                break;
            case skeleton:
                nodes.peek().skeleton_id = charBuf.toString().trim().substring(1);
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        charBuf.append(ch, start, length);
    }


    private void addTranslation() {
        String[] tv = charBuf.toString().trim().split("\\s+");
        nodes.peek().transforms.add(new Translate(
                Double.parseDouble(tv[0].trim()),
                Double.parseDouble(tv[1].trim()),
                Double.parseDouble(tv[2].trim())
        ));
    }

    private void addRotation() {
        String[] rv = charBuf.toString().trim().split("\\s+");
        nodes.peek().transforms.add(new Rotate(
                Double.parseDouble(rv[3].trim()),
                0, 0, 0,
                new Point3D(
                        Double.parseDouble(rv[0].trim()),
                        Double.parseDouble(rv[1].trim()),
                        Double.parseDouble(rv[2].trim())
                )
        ));
    }

    private void addScaling() {
        String[] sv = charBuf.toString().trim().split("\\s+");
        nodes.peek().transforms.add(new Scale(
                Double.parseDouble(sv[0].trim()),
                Double.parseDouble(sv[1].trim()),
                Double.parseDouble(sv[2].trim()),
                0, 0, 0
        ));
    }

    private void addMatrixTransformation() {
        String[] mv = charBuf.toString().trim().split("\\s+");
        nodes.peek().transforms.add(new Affine(
                Double.parseDouble(mv[0].trim()), // mxx
                Double.parseDouble(mv[1].trim()), // mxy
                Double.parseDouble(mv[2].trim()), // mxz
                Double.parseDouble(mv[3].trim()), // tx
                Double.parseDouble(mv[4].trim()), // myx
                Double.parseDouble(mv[5].trim()), // myy
                Double.parseDouble(mv[6].trim()), // myz
                Double.parseDouble(mv[7].trim()), // ty
                Double.parseDouble(mv[8].trim()), // mzx
                Double.parseDouble(mv[9].trim()), // mzy
                Double.parseDouble(mv[10].trim()), // mzz
                Double.parseDouble(mv[11].trim()) // tz
        ));
    }

    private void createVisualScene(Attributes attributes) {
        scenes.push(new DaeScene(attributes.getValue("id"), attributes.getValue("name")));
    }

    private void createDaeNode(Attributes attributes) {
        nodes.push(new DaeNode(
                attributes.getValue("id"),
                attributes.getValue("name"),
                attributes.getValue("type")
        ));
    }

    private void setDaeNode() {
        DaeNode thisNode = nodes.pop();
        if (nodes.isEmpty()) {
            if(thisNode.isCamera())
            {
                scenes.peek().cameraNodes.put(thisNode.id, thisNode);
            }
            else if(thisNode.isLight())
            {
                scenes.peek().lightNodes.put(thisNode.id, thisNode);
            }
            else if(thisNode.hasBones())
            {
                scenes.peek().skeletons.put(thisNode.id, DaeSkeleton.fromDaeNode(thisNode));
            }
            else if(thisNode.isMesh())
            {
                scenes.peek().meshNodes.put(thisNode.id, thisNode);
            }
            else if(thisNode.isController())
            {
                scenes.peek().controllerNodes.put(thisNode.id, thisNode);
            }
        } else {
            nodes.peek().children.put(thisNode.id, thisNode);
        }
    }
}