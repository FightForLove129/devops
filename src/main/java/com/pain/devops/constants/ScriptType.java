package com.pain.devops.constants;

public enum ScriptType {
    SHELL("shell"),
    PYTHON("python"),
    YAML("yaml");

    private String value;

    ScriptType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
