package com.pain.devops.entity;

import lombok.Data;

@Data
public class CommandResult {
    private int exitCode;           // 0表示成功, 非0表示失败
    private String output;          // 正常输出
    private String error;           // 错误信息
    private long executionTimeMs;  // 执行耗时

    public boolean isSuccess() {
        return exitCode == 0;
    }
}
