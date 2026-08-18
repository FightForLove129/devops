package com.pain.devops;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.pain.devops.entity.CommandResult;
import com.pain.devops.entity.Host;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommandExecutor {
    private static final int DEFAULT_TIMEOUT = 60000; // 60秒超时

    // 单机执行
    public CommandResult execute(Host host, String command) {
        return execute(host, command, DEFAULT_TIMEOUT);
    }

    public CommandResult execute(Host host, String command, int timeout) {
        log.info("执行命令 [{}@{}:{}]：{}",host.getUsername(), host.getIp(), host.getPort(), command);

        try {
            JSch jSch = new JSch();
            Session session = jSch.getSession(host.getUsername(), host.getIp(), host.getPort());
            session.setPassword(host.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(timeout);

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(System.err);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayOutputStream error = new ByteArrayOutputStream();
            channel.setOutputStream(output);
            channel.setErrStream(error);

            channel.connect(timeout);

            // 等待命令执行完成
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            int exitCode = channel.getExitStatus();
            channel.disconnect();
            session.disconnect();

            CommandResult result = new CommandResult();
            result.setExitCode(exitCode);
            result.setOutput(output.toString("UTF-8"));
            result.setError(output.toString("UTF-8"));

            log.info("命令执行完成，退出码：{}", exitCode);
            return result;

        } catch (Exception e) {
            log.error("命令执行失败,e");
            CommandResult result = new CommandResult();
            result.setExitCode(-1);
            result.setError(e.getMessage());
            return result;
        }
    }

    public Map<Host, CommandResult> executeBatch(List<Host> hosts, String command) {
        Map<Host, CommandResult> results = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = hosts.stream()
            .map(host -> CompletableFuture.runAsync(() -> {
            CommandResult result = execute(host, command);
            results.put(host, result);
        })).collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return results;
    }

    public void executeStream(Host host, String command, Consumer<String> outputConsumer) {
        try {
            JSch jSch = new JSch();
            Session session = jSch.getSession(host.getUsername(), host.getIp(), host.getPort());
            session.setPassword(host.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            // 实时读取输出
            InputStream inputStream = channel.getInputStream();
            channel.connect();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputConsumer.accept(line);
                }
            }

            int exitCode = channel.getExitStatus();
            channel.disconnect();
            session.disconnect();

            if (exitCode != 0) {
                throw new RuntimeException("命令执行失败，退出码：" + exitCode);
            }
        } catch (Exception e) {
            log.error("流式执行失败,e");
            throw new RuntimeException(e);
        }
    }

}
