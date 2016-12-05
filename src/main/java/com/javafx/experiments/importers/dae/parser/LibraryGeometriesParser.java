package com.javafx.experiments.importers.dae.parser;

import com.javafx.experiments.shape3d.PolygonMesh;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * @author Eclion
 */
final class LibraryGeometriesParser extends DefaultHandler {
    private final static Logger LOGGER = Logger.getLogger(LibraryGeometriesParser.class.getSimpleName());
    private StringBuilder charBuf = new StringBuilder();
    private Map<String, String> currentId = new HashMap<>();
    private Map<String, float[]> floatArrays = new HashMap<>();
    private Map<String, Input> inputs = new HashMap<>();
    private List<int[]> pLists = new ArrayList<>();
    Map<String, Object> meshes = new HashMap<>();
    private boolean createPolyMesh;
    private int[] vCounts;

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
                LOGGER.log(Level.WARNING, "Unknown element: " + qName);
                break;
            case input:
                Input input = ParserUtils.createInput(attributes);
                inputs.put(input.semantic, input);
                break;
            case polylist:
                inputs.clear();
                pLists.clear();
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
            case p:
                savePoints();
                break;
            case polygons:
                createPolygons();
                break;
            case polylist:
                createPolylistMesh();
                break;
            case vcount:
                saveVerticesCounts();
                break;
            case vertices:
                saveVertices();
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        charBuf.append(ch, start, length);
    }

    private void savePoints() {
        String[] numbers = charBuf.toString().trim().split("\\s+");
        int[] iArray = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            iArray[i] = Integer.parseInt(numbers[i].trim());
        }
        pLists.add(iArray);
    }

    private void createPolygons() {
        // create mesh put in map
        if (createPolyMesh) {
            Input vertexInput = inputs.get("VERTEX");
            Input texInput = inputs.get("TEXCOORD");
            float[] points = floatArrays.get(vertexInput.source.substring(1));
            float[] texCoords = floatArrays.get(texInput.source.substring(1));
            int[][] faces = new int[pLists.size()][];
            for (int f = 0; f < faces.length; f++) {
                faces[f] = pLists.get(f);
            }
            PolygonMesh mesh = new PolygonMesh(points, texCoords, faces);
            meshes.put(currentId.get("geometry"), mesh);
        } else {
            TriangleMesh mesh = new TriangleMesh();
            meshes.put(currentId.get("geometry"), mesh);
            throw new UnsupportedOperationException("Need to implement TriangleMesh creation");
        }
    }

    private void createPolylistMesh() {
        // create mesh put in map
        if (createPolyMesh) {
            int faceStep = 1;
            Input vertexInput = inputs.get("VERTEX");
            if (vertexInput != null && (vertexInput.offset + 1) > faceStep)
                faceStep = vertexInput.offset + 1;
            Input texInput = inputs.get("TEXCOORD");
            if (texInput != null && (texInput.offset + 1) > faceStep) faceStep = texInput.offset + 1;
            Input normalInput = inputs.get("NORMAL");
            if (normalInput != null && (normalInput.offset + 1) > faceStep)
                faceStep = normalInput.offset + 1;
            float[] points = floatArrays.get(vertexInput.source.substring(1));
            float[] texCoords;
            if (texInput == null) {
                texCoords = new float[]{0, 0};
            } else {
                texCoords = floatArrays.get(texInput.source.substring(1));
            }
            int[][] faces = new int[vCounts.length][];
            int[] p = pLists.get(0);
            int faceIndex = 0;
            for (int f = 0; f < faces.length; f++) {
                final int numOfVertex = vCounts[f];
                final int[] face = new int[numOfVertex * 2];
                for (int v = 0; v < numOfVertex; v++) {
                    final int vertexIndex = faceIndex + (v * faceStep);
                    face[v * 2] = p[vertexIndex + vertexInput.offset];
                    face[(v * 2) + 1] = (texInput == null) ? 0 : p[vertexIndex + texInput.offset];
                }
                faces[f] = face;
                faceIndex += numOfVertex * faceStep;
            }
            PolygonMesh mesh = new PolygonMesh(points, texCoords, faces);
            meshes.put(currentId.get("geometry"), mesh);
        } else {

            int faceStep = 1;

            Input vertexInput = inputs.get("VERTEX");
            if (vertexInput != null && (vertexInput.offset + 1) > faceStep)
                faceStep = vertexInput.offset + 1;
            float[] points = floatArrays.get(vertexInput.source.substring(1));

            Input texInput = inputs.get("TEXCOORD");
            if (texInput != null && (texInput.offset + 1) > faceStep) faceStep = texInput.offset + 1;
            float[] texCoords;
            if (texInput == null) {
                texCoords = new float[]{0, 0};
            } else {
                texCoords = floatArrays.get(texInput.source.substring(1));
            }

            Input normalInput = inputs.get("NORMAL");
            boolean hasNormals = (normalInput != null);
            float[] normals = new float[]{};
            if (hasNormals) {
                normals = floatArrays.get(normalInput.source.substring(1));
                if ((normalInput.offset + 1) > faceStep)
                    faceStep = normalInput.offset + 1;
            }

            final int inputCount = (hasNormals) ? 3 : 2;

            int[] faces = new int[IntStream.of(vCounts).sum() * inputCount];
            int[] p = pLists.get(0);
            int pIndex = 0;

            int faceIndex = 0;
            for (int vCount : vCounts) {
                for (int v = 0; v < vCount; v++) {
                    faces[faceIndex + v * inputCount] = p[pIndex + vertexInput.offset];
                    if (hasNormals)
                        faces[faceIndex + v * inputCount + 1] = p[pIndex + normalInput.offset];
                    faces[faceIndex + v * inputCount + inputCount - 1] = (texInput == null) ? 0 : p[pIndex + texInput.offset];
                    pIndex += faceStep;
                }
                faceIndex += vCount * inputCount;
            }

            TriangleMesh mesh = (hasNormals)
                    ? new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD)
                    : new TriangleMesh(VertexFormat.POINT_TEXCOORD);

            mesh.getPoints().setAll(points);
            if (hasNormals) mesh.getNormals().setAll(normals);
            mesh.getTexCoords().setAll(texCoords);
            mesh.getFaces().setAll(faces);

            meshes.put(currentId.get("geometry"), mesh);
        }
    }

    private void saveVerticesCounts() {
        String[] numbers = charBuf.toString().trim().split("\\s+");
        vCounts = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            vCounts[i] = Integer.parseInt(numbers[i].trim());
        }
    }

    private void saveVertices() {
        // put vertex float into map again with new ID
        String sourceId = inputs.get("POSITION").source.substring(1);
        float[] points = floatArrays.get(sourceId);
        floatArrays.put(
                currentId.get("vertices"),
                points);
    }

    void setCreatePolyMesh(boolean createPolyMesh){
        this.createPolyMesh = createPolyMesh;
    }

}
