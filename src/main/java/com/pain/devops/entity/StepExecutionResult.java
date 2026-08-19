package com.pain.devops.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StepExecutionResult {
    private String stepName;
    private boolean success;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String detail;
    private String error;


    public boolean isSuccess() {
        return success;
    }
}
