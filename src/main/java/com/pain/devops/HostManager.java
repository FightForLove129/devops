package com.pain.devops;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.pain.devops.constants.HostStatus;
import com.pain.devops.entity.Host;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HostManager {
    private final Map<String, Host> hosts = new ConcurrentHashMap<>();
    private final Map<String, HostStatus> statusCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService healthChecker = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        // 每隔30秒检查一次所有主机的健康状态
        healthChecker.scheduleAtFixedRate(this::checkAllHosts, 0, 30, TimeUnit.SECONDS);
    }

    public void registerHost(Host host) {
        hosts.put(host.getId(), host);
        statusCache.put(host.getId(),HostStatus.UNREACHABLE);
        log.info("主机注册成功：{}（{}）", host.getName(), host.getIp());
    }

    public void unregisterHost(String hostId) {
        hosts.remove(hostId);
        statusCache.remove(hostId);
        log.info("主机已移除：{}", hostId);
    }


    private void checkAllHosts() {
        for (Host host : hosts.values()) {
            try {
                boolean reachable = tryConnect(host);
                statusCache.put(host.getId(), reachable ? HostStatus.ONLINE : HostStatus.UNREACHABLE);
            } catch (Exception e) {
                statusCache.put(host.getId(), HostStatus.UNREACHABLE);
                log.error("主机健康检查失败：{} - {}", host.getName(), e.getMessage());
            }
        }
    }

    private boolean tryConnect(Host host) {
        // 用SSH尝试链接，能连上说明活着
        try (Session session = new JSch().getSession(host.getUsername(), host.getIp(), host.getPort())) {
            session.setPassword(host.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(3000);
            session.disconnect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Host getHost (String id) {
        return hosts.get(id);
    }

    public List<Host> getHostsByTag(String tagKey, String tagValue) {
        return hosts.values().stream()
                .filter(h -> tagValue.equals(h.getTags().get(tagKey)))
                .collect(Collectors.toList());
    }

}
