package com.javafx.experiments.importers.dae.parser.libraries;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eclion
 */
public class DaeScene {
    private final String name;
    private final String id;
    public Map<String, DaeNode> cameraNodes = new HashMap<>();
    public Map<String, DaeNode> lightNodes = new HashMap<>();
    public Map<String, DaeNode> meshNodes = new HashMap<>();
    public Map<String, DaeSkeleton> skeletons = new HashMap<>();
    public Map<String, DaeNode> controllerNodes = new HashMap<>();

    public DaeScene(String id, String name)
    {
        this.id = id;
        this.name = name;
    }
}
