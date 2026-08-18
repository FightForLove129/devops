package com.pain.devops.entity;

import com.pain.devops.constants.StepType;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TaskStep {
    private String name;
    private StepType type;              // HOST_SELECT / EXEC_SCRIPT / SEND_NOTIFICATION / WAIT
    private List<String> targetHostIds;     // 目标主机ID
    private String scriptId;                // 脚本ID
    private Map<String, String> parameters; // 脚本参数
    private String notificationTarget;      // 通知目标（邮箱、企微）
    private int retryTimes = 0;             // 重试次数
    private int retryDelaySeconds = 5;      // 重试间隔
}
