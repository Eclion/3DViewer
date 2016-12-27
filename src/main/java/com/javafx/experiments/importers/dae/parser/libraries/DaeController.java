package com.javafx.experiments.importers.dae.parser.libraries;

import javafx.scene.transform.Affine;

/**
 * @author Eclion
 */
public class DaeController {
    String name;
    String id;

    public String skinId;
    public Affine bindShapeMatrix;
    public String[] boneNames;
    Affine[] bindPoses;// not used in jagatoo...
    public float[][] vertexWeights;
    //cf XMLskin.buildInfluence
    // uses bindShapeMatrix to calculate the bone influence if no bone
    //

    public DaeController(String id, String name)
    {
        this.id = id;
        this.name = name;
    }
}
