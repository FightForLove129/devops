package com.pain.devops;

import com.pain.devops.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskEngine {
    @Autowired
    private HostManager hostManager;
    @Autowired
    private CommandExecutor executor;
    @Autowired
    private ScriptRepository scriptRepo;

    public TaskExecutionResult executeTask(TaskDefinition task) {
        log.info("开始执行任务：{}", task.getName());
        TaskExecutionResult result = new TaskExecutionResult();
        result.setTaskId(task.getId());
        result.setStartTime(LocalDateTime.now());

        List<StepExecutionResult> stepResults = new ArrayList<>();
        boolean hasError = false;

        for (int i = 0; i < task.getSteps().size(); i++) {
            TaskStep step = task.getSteps().get(i);

            // 如果上一步出错了并且设置了终止，就跳过后续步骤
            if (hasError && task.isStopOnError()) {
                log.warn("任务提前终止于步骤：{}", step.getName());
                break;
            }

            StepExecutionResult stepResult = executeStep(step);
            stepResults.add(stepResult);

            if (!stepResult.isSuccess()) {
                hasError = true;
                log.error("步骤执行失败：{}", step.getName());
            }
        }

        result.setStepResults(stepResults);
        result.setEndTime(LocalDateTime.now());
        result.setSuccess(!hasError);
        return result;
    }

    private StepExecutionResult executeStep(TaskStep step) {
        StepExecutionResult result = new StepExecutionResult();
        result.setStepName(step.getName());
        result.setStartTime(LocalDateTime.now());

        try {
            switch (step.getType()) {
                case HOST_SELECT:
                    // 主机选择步骤不执行具体操作，只是验证主机是否存在
                    List<Host> hosts = step.getTargetHostIds().stream()
                            .map(hostManager::getHost)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    result.setDetail("选择主机："+ hosts.stream()
                            .map(Host::getName)
                            .collect(Collectors.joining(", ")));
                    result.setSuccess(true);
                    break;

                case EXEC_SCRIPT:
                    String script = scriptRepo.renderScript(step.getScriptId(), step.getParameters());
                    List<Host> targetHosts = step.getTargetHostIds().stream()
                            .map(hostManager::getHost)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    Map<Host, CommandResult> execResults = executor.executeBatch(targetHosts, script);
                    boolean allSuccess = execResults.values().stream().allMatch(CommandResult::isSuccess);
                    result.setDetail(execResults.toString());
                    result.setSuccess(allSuccess);
                    break;

                case SEND_NOTIFICATION:
                    // 发送通知（邮件/企微）
                    sendNotification(step.getNotificationTarget(), "任务执行状态：" +  step.getName());
                    result.setSuccess(true);
                    break;

                case WAIT:
                    int waitSeconds = Integer.parseInt(step.getParameters().getOrDefault("seconds", "10"));
                    Thread.sleep(waitSeconds * 1000L);
                    result.setSuccess(true);
                    break;

                default:
                    result.setSuccess(false);
                    result.setError("未知的步骤类型：" + step.getType());
            }
        } catch (Exception e) {
            log.error("步骤执行异常：{}", step.getName(), e);
            result.setSuccess(false);
            result.setError(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        return result;
    }

    private void sendNotification(String target, String message) {
        // 邮件/企业微信通知逻辑
    }
}
