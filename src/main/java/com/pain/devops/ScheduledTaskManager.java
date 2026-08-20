package com.pain.devops;

import com.pain.devops.entity.ScheduledTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ScheduledTaskManager {
    private final Map<String, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public void registerTask(ScheduledTask task) {
        tasks.put(task.getId(), task);

        // 解析Cron表达式
        CronExpression cron = CronExpression.parse(task.getCronExpression());

        scheduler.scheduleAtFixedRate(() ->
            {executeAndAlert(task);
        }, 0, 1, TimeUnit.MINUTES); // 每分钟检查一次是否应该执行
    }

    private void executeAndAlert(ScheduledTask task) {
        // 判断是否到了执行时间

    }
}
