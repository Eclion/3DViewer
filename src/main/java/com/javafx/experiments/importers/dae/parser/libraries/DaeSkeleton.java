package com.javafx.experiments.importers.dae.parser.libraries;

import com.javafx.experiments.importers.maya.Joint;
import javafx.scene.Parent;
import javafx.scene.transform.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Eclion
 */
public final class DaeSkeleton extends Parent {

    private final String name;
    public final Map<String, Joint> joints = new LinkedHashMap<>();
    public final Map<String, Affine> bindTransforms = new LinkedHashMap<>();

    DaeSkeleton(String id, String name) {
        setId(id);
        this.name = name;
    }

    public static DaeSkeleton fromDaeNode(DaeNode rootNode) {
        final DaeSkeleton skeleton = new DaeSkeleton(rootNode.id, rootNode.name);

        skeleton.getTransforms().addAll(rootNode.transforms);

        List<DaeNode> rootDaeNodes = new ArrayList<>();
        rootDaeNodes.addAll(rootNode.children.values().stream().filter(DaeNode::isJoint).collect(Collectors.toList()));

        skeleton.getChildren().addAll(buildBone(rootDaeNodes, skeleton.joints, skeleton.bindTransforms));

        return skeleton;
    }

    private static List<Joint> buildBone(final List<DaeNode> daeNodes, final Map<String, Joint> joints, final Map<String, Affine> bindTransforms) {
        List<Joint> childJoints = new ArrayList<>();
        for (final DaeNode nodes : daeNodes) {
            final Joint joint = new Joint();
            joint.setId(nodes.id);
            final List<DaeNode> children = nodes.children.values().stream().filter(DaeNode::isJoint).collect(Collectors.toList());

            childJoints.add(joint);
            joints.put(joint.getId(), joint);

            nodes.transforms.stream()
                    .filter(transform -> transform instanceof Affine)
                    .findFirst()
                    .ifPresent(affine -> {
                        Affine jointAffine = joint.a;
                        jointAffine.setMxx(affine.getMxx());
                        jointAffine.setMxy(affine.getMxy());
                        jointAffine.setMxz(affine.getMxz());
                        jointAffine.setMyx(affine.getMyx());
                        jointAffine.setMyy(affine.getMyy());
                        jointAffine.setMyz(affine.getMyz());
                        jointAffine.setMzx(affine.getMzx());
                        jointAffine.setMzy(affine.getMzy());
                        jointAffine.setMzz(affine.getMzz());
                        jointAffine.setTx(affine.getTx());
                        jointAffine.setTy(affine.getTy());
                        jointAffine.setTz(affine.getTz());
                        //bindTransforms.put(joint.getId(), (Affine) affine);
                    });

            bindTransforms.put(joint.getId(), joint.a);

            joint.getChildren().addAll(buildBone(children, joints, bindTransforms));
        }
        return childJoints;
    }
}
