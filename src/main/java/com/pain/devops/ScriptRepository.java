package com.pain.devops;

import com.pain.devops.entity.ScriptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// 脚本仓库管理
@Service
public class ScriptRepository {
    private final Map<String, ScriptTemplate> scripts = new ConcurrentHashMap<>();

    public void save(ScriptTemplate script) {
        scripts.put(script.getId(), script);
    }

    public ScriptTemplate get(String id) {
        return scripts.get(id);
    }

    public List<ScriptTemplate> findByOs(String os) {
        return scripts.values().stream()
                .filter(s -> s.getCompatibleOs().contains(os))
                .collect(Collectors.toList());
    }

    // 渲染带参数的脚本
    public String renderScript(String scriptId, Map<String, String> params) {
        ScriptTemplate template = get(scriptId);
        if (template == null) {
            throw new IllegalArgumentException("脚本不存在：" + scriptId);
        }
        String content = template.getScriptContent();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return content;
    }
}
