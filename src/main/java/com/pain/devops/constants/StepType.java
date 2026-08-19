package com.pain.devops.constants;

public enum StepType {
    HOST_SELECT("hostSelect"),
    EXEC_SCRIPT("execScript"),
    SEND_NOTIFICATION("sendNotification"),
    WAIT("wait");

    private String value;
    StepType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
