package com.pain.devops;

import com.pain.devops.entity.StepExecutionResult;
import com.pain.devops.entity.TaskDefinition;
import com.pain.devops.entity.TaskExecutionResult;
import com.pain.devops.entity.TaskStep;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/task/{taskId}")
@Slf4j
public class TaskWebSocket {
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Autowired
    private TaskEngine taskEngine;

    @OnOpen
    public void onOpen(@PathParam("taskId") String taskId, Session session) {
        sessions.put(taskId + "_" + session.getId(), session);
        log.info("WebSocket连接建立，taskId: {}", taskId);
    }

    @OnClose
    public void onClose(@PathParam("taskId") String taskId, Session session) {
        sessions.remove(taskId + "_" + session.getId());
        log.info("WebSocket连接关闭，taskId: {}", taskId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 可以处理客户端发来的消息
    }

    // 推送进度给前端
    public static void pushProgress(String taskId, String step, int percent, String detail) {
        String targetKey = taskId + "_";
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            if (entry.getKey().startsWith(targetKey)) {
                try {
                    entry.getValue().getBasicRemote().sendText(String.format("{\"step\":\"%s\",\"percent\":%d,\"detail\":\"%s\"}", step, percent, detail));
                } catch (IOException e) {
                    log.error("推送失败",e);
                }
            }
        }
    }

    // 在任务引擎里调用
    public TaskExecutionResult executeTask (TaskDefinition task) {
        TaskExecutionResult result = new TaskExecutionResult();
        TaskWebSocket.pushProgress(task.getId(), "开始执行", 0, "任务已启动");
        for (int i = 0; i < task.getSteps().size(); i++) {
            int progress = (int) (i * 100.0 / task.getSteps().size());
            TaskStep step = task.getSteps().get(i);
            StepExecutionResult stepResult = taskEngine.executeStep(step);
            TaskWebSocket.pushProgress(
                    task.getId(),
                    step.getName(),
                    progress,
                    stepResult.isSuccess() ? "成功" : "失败" + stepResult.getError());
        }
        TaskWebSocket.pushProgress(task.getId(), "完成", 100, "所有步骤已执行完毕");
        return result;
    }
}
