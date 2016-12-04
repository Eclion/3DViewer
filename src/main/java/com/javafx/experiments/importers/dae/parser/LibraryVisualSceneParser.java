package com.javafx.experiments.importers.dae.parser;

import com.javafx.experiments.shape3d.PolygonMesh;
import com.javafx.experiments.shape3d.PolygonMeshView;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
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
    private Group rootNode = new Group();
    private StringBuilder charBuf = new StringBuilder();
    private Map<String, String> currentId = new HashMap<>();
    private List<Transform> currentTransforms;
    private LinkedList<DaeNode> nodes = new LinkedList<>();

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
                //nodes.peek().instance_camera = cameras.get(attributes.getValue("url").substring(1));
                break;
            case instance_geometry:
                //nodes.peek().instance_geometry = meshes.get(attributes.getValue("url").substring(1));
                break;
            case instance_controller:
                //nodes.peek().instance_controller = controllers.get(attributes.getValue("url").substring(1));
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
        currentTransforms.add(new Translate(
                Double.parseDouble(tv[0].trim()),
                Double.parseDouble(tv[1].trim()),
                Double.parseDouble(tv[2].trim())
        ));
    }

    private void addRotation() {
        String[] rv = charBuf.toString().trim().split("\\s+");
        currentTransforms.add(new Rotate(
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
        currentTransforms.add(new Scale(
                Double.parseDouble(sv[0].trim()),
                Double.parseDouble(sv[1].trim()),
                Double.parseDouble(sv[2].trim()),
                0, 0, 0
        ));
    }

    private void addMatrixTransformation() {
        String[] mv = charBuf.toString().trim().split("\\s+");
        currentTransforms.add(new Affine(
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
        rootNode.setId(attributes.getValue("name"));
        DaeNode rootDaeNode = new DaeNode(attributes.getValue("id"), attributes.getValue("name"));
        rootDaeNode.group = rootNode;
        nodes.push(rootDaeNode);
    }

    Group getRootNode()
    {
        return rootNode;
    }

    private void createDaeNode(Attributes attributes) {
        currentTransforms = new ArrayList<>();
        nodes.push(new DaeNode(attributes.getValue("id"), attributes.getValue("name")));
    }

    private void setDaeNode() {
        DaeNode thisNode = nodes.pop();
        if (thisNode.isCamera()) { // IS CAMERA
            thisNode.instance_camera.setId(thisNode.name);
            thisNode.instance_camera.getTransforms().addAll(currentTransforms);
            nodes.peek().getGroup().getChildren().add(thisNode.instance_camera);
        } else if (thisNode.isMesh()) { // IS MESH VIEW
            Node meshView;
            if (thisNode.instance_geometry instanceof PolygonMesh) {
                meshView = new PolygonMeshView((PolygonMesh) thisNode.instance_geometry);
            } else {
                meshView = new MeshView((TriangleMesh) thisNode.instance_geometry);
            }
            meshView.setId(thisNode.name);
            meshView.getTransforms().addAll(currentTransforms);
            nodes.peek().getGroup().getChildren().add(meshView);
        } else if (thisNode.isController()) {
            System.out.println(thisNode.toString());


        } else { // IS GROUP
            Group group = thisNode.getGroup();
            group.setId(thisNode.name);
            group.getTransforms().addAll(currentTransforms);
            nodes.peek().getGroup().getChildren().add(group);
        }
    }

    /**
     * So far a node can be one of camera, geometry or group
     */
    private static final class DaeNode {
        private final String id;
        private final String name;
        private Camera instance_camera;
        private Object instance_geometry;
        private Object instance_controller;
        private Group group;

        private DaeNode(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public boolean isCamera() {
            return instance_camera != null;
        }

        public boolean isMesh() {
            return instance_geometry != null;
        }

        public boolean isController() {
            return instance_controller != null;
        }

        public boolean isGroup() {
            return instance_camera == null && instance_geometry == null && instance_controller == null;
        }

        public Group getGroup() {
            if (group == null) group = new Group();
            return group;
        }

        @Override
        public String toString() {
            return "DaeNode{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", instance_camera=" + instance_camera +
                    ", instance_geometry=" + instance_geometry +
                    ", instance_controller=" + instance_controller +
                    ", group=" + group +
                    '}';
        }
    }
}