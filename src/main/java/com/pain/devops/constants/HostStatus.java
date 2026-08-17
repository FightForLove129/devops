package com.pain.devops.constants;

public enum HostStatus {
    ONLINE("online"),
    OFFLINE("offline"),
    UNREACHABLE("unreachable");

    private String status;

    HostStatus(String status) {
        this.status = status;
    }
}
