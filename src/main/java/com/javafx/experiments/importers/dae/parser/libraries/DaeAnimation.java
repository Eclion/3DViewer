package com.javafx.experiments.importers.dae.parser.libraries;

import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.scene.shape.TriangleMesh;

/**
 * @author Eclion
 */
public class DaeAnimation {

    String id;
    public float[] input;
    public float[] output;
    private String[] interpolations;
    public String target;

    //interpolation
    //sampler

    public DaeAnimation(String id)
    {
        this.id = id;
    }

    public void animate(TriangleMesh triangleMesh){

    }

    public Timeline getTimeline()
    {
        return null;
    }

    public void setInterpolations(String[] interpolations) {
        this.interpolations = interpolations;
    }

    public void addChild(DaeAnimation animation) {

    }
}
