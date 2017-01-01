package com.javafx.experiments.importers.dae.parser.libraries;

import com.javafx.experiments.importers.maya.Joint;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.scene.transform.Affine;
import javafx.scene.transform.MatrixType;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eclion
 */
public class DaeAnimation {

    String id;
    public float[] input;
    public double[] output;
    public String[] interpolations;
    public String target;
    final List<DaeAnimation> childAnimations = new ArrayList<>();

    //interpolation
    //sampler

    public DaeAnimation(String id)
    {
        this.id = id;
    }

    public List<KeyFrame> calculateAnimation(DaeSkeleton skeleton){
        List<KeyFrame> keyFrames = new ArrayList<>();
        final String targetJointName = target.split("/")[0];
        Joint animatedJoint = skeleton.joints.get(targetJointName);
        int count = input.length;
        for(int i = 0; i < count; i ++)
        {
            Duration duration = new Duration(input[i]*3000);
            Affine keyAffine = new Affine(output, MatrixType.MT_3D_4x4, i*16);
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.mxxProperty(), keyAffine.getMxx())));
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.mxyProperty(), keyAffine.getMxy())));
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.mxzProperty(), keyAffine.getMxz())));
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.myxProperty(), keyAffine.getMyx())));
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.myyProperty(), keyAffine.getMyy())));
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.myzProperty(), keyAffine.getMyz())));
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.mzxProperty(), keyAffine.getMzx())));
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.mzyProperty(), keyAffine.getMzy())));
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.mzzProperty(), keyAffine.getMzz())));
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.txProperty(), keyAffine.getTx())));
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.tyProperty(), keyAffine.getTy())));
            keyFrames.add(new KeyFrame(duration, new KeyValue(animatedJoint.a.tzProperty(), keyAffine.getTz())));
        }
        keyFrames.addAll(childAnimations.stream().map(animation -> animation.calculateAnimation(skeleton))
                .reduce(new ArrayList<>(), (l1, l2) -> {
            l1.addAll(l2);
            return l1;
        }));
        return keyFrames;
    }

    public void setInterpolations(String[] interpolations) {
        this.interpolations = interpolations;
    }

    public void addChild(DaeAnimation animation) {
        childAnimations.add(animation);
    }
}