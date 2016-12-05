package com.javafx.experiments.importers.dae.parser;

import javafx.scene.transform.Transform;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eclion
 */
final class DaeNode {
    private final String id;
    final String name;
    private final String type;
    List<Transform> transforms = new ArrayList<>();
    String instance_camera_id;
    String instance_geometry_id;
    String instance_controller_id;
    String instance_light_id;
    String instance_material_id;
    List<DaeNode> children = new ArrayList<>();

    DaeNode(String id, String name, String type) {
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
}