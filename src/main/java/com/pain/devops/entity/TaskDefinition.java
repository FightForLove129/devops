package com.pain.devops.entity;

import lombok.Data;

import java.util.List;

// 任务定义
@Data
public class TaskDefinition {
    private String id;
    private String name;
    private List<TaskStep> steps;      // 步骤列表
    private boolean stopOnError;        // 出错是否终止
}
