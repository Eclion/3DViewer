package com.javafx.experiments.importers.dae.parser.libraries;

import javafx.scene.transform.Transform;

import java.util.*;

/**
 * @author Eclion
 *         make subclasses of this one
 */
public final class DaeNode {
    public final String id;
    public final String name;
    public final String type;
    public List<Transform> transforms = new ArrayList<>();
    public String instance_camera_id;
    public String instance_geometry_id;
    public String instance_controller_id;
    public String instance_light_id;
    public String instance_material_id;
    public String skeleton_id;
    public Map<String, DaeNode> children = new HashMap<>();

    public DaeNode(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public boolean isCamera() {
        return instance_camera_id != null;
    }

    public boolean isLight() {
        return instance_light_id != null;
    }

    public boolean isMesh() {
        return instance_geometry_id != null;
    }

    public boolean isController() {
        return instance_controller_id != null;
    }

    public boolean hasBones() {
        return children.values().stream().anyMatch(child -> child.type.equalsIgnoreCase("JOINT"));
    }

    public boolean isJoint() {
        return type.equalsIgnoreCase("JOINT");
    }

    @Override
    public String toString() {
        return "DaeNode{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", instance_camera=" + instance_camera_id +
                ", instance_geometry=" + instance_geometry_id +
                ", instance_controller=" + instance_controller_id +
                '}';
    }

    Optional<DaeNode> getChildFromId(String id) {
        return children.containsKey(id)
                ? Optional.of(children.get(id))
                : children.values().stream().map(child -> child.getChildFromId(id))
                .filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}