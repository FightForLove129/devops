package com.pain.devops.entity;

import lombok.Data;

@Data
public class ScheduledTask {
    private String id;
    private String cronExpression;
}
