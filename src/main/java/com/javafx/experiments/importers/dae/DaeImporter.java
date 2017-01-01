/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.javafx.experiments.importers.dae;

import com.javafx.experiments.importers.Importer;
import com.javafx.experiments.importers.dae.parser.DaeSaxParser;
import javafx.animation.Timeline;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * Loader for ".dae" 3D files
 * <p>
 * Notes:
 * <p>
 * - Assume Y is up for now
 * - Assume 1 Unit = 1 FX Unit
 */
@SuppressWarnings("UnusedDeclaration")
public class DaeImporter extends Importer {
    private Group rootNode = new Group();
    private Camera firstCamera = null;
    private double firstCameraAspectRatio = 4 / 3;
    private boolean createPolyMesh;
    private Timeline timeline = null;

    {
        // CHANGE FOR Y_UP
        rootNode.getTransforms().add(new Rotate(180, 0, 0, 0, Rotate.X_AXIS));
    }

    public DaeImporter() {
    }

    public DaeImporter(String url, boolean createPolyMesh) {
        load(url, createPolyMesh);
    }

    public DaeImporter(File file, boolean createPolyMesh) {
        this.createPolyMesh = createPolyMesh;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(file, new DaeSaxParser());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    public DaeImporter(InputStream in, boolean createPolyMesh) {
        this.createPolyMesh = createPolyMesh;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(in, new DaeSaxParser());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    public Scene createScene(int width) {
        Scene scene = new Scene(rootNode, width, (int) (width / firstCameraAspectRatio), true);
        if (firstCamera != null) scene.setCamera(firstCamera);
        scene.setFill(Color.BEIGE);
        return scene;
    }

    public Camera getFirstCamera() {
        return firstCamera;
    }

    @Override
    public Group getRoot() {
        return rootNode;
    }

    @Override
    public void load(String url, boolean createPolygonMesh) {
        this.createPolyMesh = createPolygonMesh;
        long START = System.currentTimeMillis();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            DaeSaxParser parser = new DaeSaxParser();
            parser.setCreatePolyMesh(createPolygonMesh);
            saxParser.parse(url, parser);

            buildTimeline(parser);

            parser.buildScene(rootNode);
            firstCamera = parser.getFirstCamera();
            firstCameraAspectRatio = parser.getFirstCameraAspectRatio();
            //timeline = parser.getTimeline();

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        long END = System.currentTimeMillis();
        System.out.println("IMPORTED [" + url + "] in  " + ((END - START)) + "ms");
    }

    private void buildTimeline(DaeSaxParser parser) {
        timeline = new Timeline();
        timeline.getKeyFrames().addAll(parser.getAllKeyFrames());
    }

    @Override
    public boolean isSupported(String extension) {
        return extension != null && extension.equals("dae");
    }

    @Override
    public Timeline getTimeline() {
        return timeline;
    }
}
