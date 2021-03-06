package com.javafx.experiments.importers.dae.parser.libraries;

import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eclion
 */
public class DaeController {
    final String name;
    final String id;

    public String skinId;
    public Affine bindShapeMatrix;
    public String[] jointNames;
    Affine[] bindPoses;// not used in jagatoo...
    public float[][] vertexWeights;
    public List<Affine> bindTransforms = new ArrayList<>();
    //cf XMLskin.buildInfluence
    // uses bindShapeMatrix to calculate the bone influence if no bone
    //

    public DaeController(String id, String name)
    {
        this.id = id;
        this.name = name;
    }
}
