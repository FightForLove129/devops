package com.pain.devops.entity;

import com.pain.devops.constants.ScriptType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ScriptTemplate {
    private String id;
    private String name;                    // 脚本名称，比如"部署Nginx"
    private String description;             // 描述
    private String scriptContent;           // 脚本内容
    private ScriptType type;                // SHELL / PYTHON / YAML
    private List<String> compatibleOs;      // CentOS7、 Ubuntu20.02 等
    private Map<String, String> parameters; // 参数定义，如{"PORT":"要监听的端口"}
    private String version;                 // 版本号
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
