package com.javafx.experiments.importers.dae.parser.libraries;

import javafx.geometry.Point3D;
import javafx.scene.Parent;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

/**
 * @author Eclion
 */
public class DaeBone extends Parent {

    Affine tip;
    Point3D localPosition = new Point3D(0, 0, 0);
    Point3D localRotationAxis = new Point3D(0, 0, 0);
    Rotate localRotate = new Rotate();


    public DaeBone(String id) {
        super();
        setId(id);
    }

    public void setTip(Affine tip) {
        this.tip = tip;
        //tip.deltaTransform()
        getChildren().forEach(child -> {
            child.setTranslateX(tip.getTx() + this.getTranslateX());
            child.setTranslateY(tip.getTy() + this.getTranslateY());
            child.setTranslateZ(tip.getTz() + this.getTranslateZ());
        });
    }

    public Affine getTip() {
        return tip;
    }

    public void addChild(DaeBone bone) {
        getChildren().add(bone);
        bone.getTransforms().add(tip);
        /*bone.setTranslateX(tip.getTx());
        bone.setTranslateY(tip.getTy());
        bone.setTranslateZ(tip.getTz());*/
        //bone.setRotationAxis(this.getRotationAxis());
        //bone.setRotate(this.getRotate());
    }

    public void setPosition(double x, double y, double z) {
        this.setTranslateX(x);
        this.setTranslateY(y);
        this.setTranslateZ(z);
    }

    public void setRotation(double xAxis, double yAxis, double zAxis, double value) {
        this.setRotationAxis(new Point3D(xAxis, yAxis, zAxis));
        this.setRotate(value);
    }
}
