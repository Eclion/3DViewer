package com.javafx.experiments.importers.dae.parser.libraries;

import javafx.scene.Parent;
import javafx.scene.transform.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Eclion
 */
public class DaeSkeleton extends Parent {

    private final String name;
    Map<String, DaeBone> rootBones = new HashMap<>();


    DaeSkeleton(String id, String name) {
        setId(id);
        this.name = name;
    }

    public static DaeSkeleton fromDaeNode(DaeNode rootNode) {
        DaeSkeleton skeleton = new DaeSkeleton(rootNode.id, rootNode.name);
        rootNode.transforms.forEach(transform -> {
            if (transform instanceof Rotate) {
                skeleton.setRotationAxis(((Rotate) transform).getAxis());
                skeleton.setRotate(((Rotate) transform).getAngle());
            } else if (transform instanceof Translate) {
                skeleton.setTranslateX(transform.getTx());
                skeleton.setTranslateY(transform.getTy());
                skeleton.setTranslateZ(transform.getTz());
            } else if (transform instanceof Scale) {
                skeleton.setScaleX(((Scale) transform).getX());
                skeleton.setScaleX(((Scale) transform).getY());
                skeleton.setScaleX(((Scale) transform).getZ());
            }
        });

        buildBone(rootNode.children.values().stream().filter(DaeNode::isBones).collect(Collectors.toList()))
                .forEach(bone -> skeleton.rootBones.put(bone.getId(), bone));

        return skeleton;
    }

    private static List<DaeBone> buildBone(List<DaeNode> daeNodes) {
        List<DaeBone> bones = new ArrayList<>();
        for (DaeNode boneNode : daeNodes) {
            DaeBone bone = new DaeBone(boneNode.id);
            boneNode.transforms.stream()
                    .filter(transform -> transform instanceof Affine)
                    .findFirst()
                    .ifPresent(transform -> bone.setTip((Affine) transform));
            List<DaeNode> children = boneNode.children.values().stream().filter(DaeNode::isBones).collect(Collectors.toList());
            buildBone(children).forEach(bone::addChild);
            bones.add(bone);
        }
        return bones;
    }
}
