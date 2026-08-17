package com.pain.devops.entity;

import com.pain.devops.constants.HostStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class Host {
    private String id;              // 唯一标识
    private String name;            // 主机名，比如"会员服务-生产-1"
    private String ip;              // IP地址
    private int port = 22;          // SSH端口
    private String username;        // 登录用户
    private String password;        // 密码（生产环境建议用密钥）
    private String osType;          // Linux/Windows
    private Map<String, String> tags; // 标签,比如env=prod, service=member
    private HostStatus status;          // ONLINE/OFFLINE/UNREACHABLE
    private LocalDateTime lastHeartbeat;    // 最后一次心跳时间
}
