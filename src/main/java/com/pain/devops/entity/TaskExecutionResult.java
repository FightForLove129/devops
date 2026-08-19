package com.pain.devops.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskExecutionResult {
    private String taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean success;
    private List<StepExecutionResult> stepResults;
}
