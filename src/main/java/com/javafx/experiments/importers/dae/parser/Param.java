package com.javafx.experiments.importers.dae.parser;

/**
 * @author Eclion
 */
public class Param {
    public final String name;
    public final String type;

    Param(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Input{" +
                "name=" + name +
                ", type='" + type + '\'' +
                '}';
    }
}
