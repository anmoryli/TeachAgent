package com.anmory.teachagent.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author Anmory
 * @description 在 Docker 容器中运行 Java 代码并返回输出
 * @date 2025-05-28 下午8:37
 */
@Service
public class JudgePrommingService {
    private final DockerClient dockerClient;

    public JudgePrommingService() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();
        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public String judge(String code) throws IOException, InterruptedException {
        // 生成临时目录和文件
        String submitId = UUID.randomUUID().toString();
        Path tempDir = Paths.get("/tmp", submitId);
        Files.createDirectories(tempDir);
        Path codeFile = tempDir.resolve("Main.java");

        CreateContainerResponse container = null;
        try {
            // 保存代码到临时文件
            Files.write(codeFile, code.getBytes());

            // 创建 Docker 容器
            container = dockerClient.createContainerCmd("openjdk:21")
                    .withCmd("sh", "-c", "javac /code/Main.java && java -cp /code Main")
                    .withHostConfig(HostConfig.newHostConfig()
                            .withMemory(512 * 1024 * 1024L)
                            .withCpuQuota(1000000L))
                    .withVolumes(new com.github.dockerjava.api.model.Volume("/code"))
                    .withBinds(new com.github.dockerjava.api.model.Bind(tempDir.toString(), new com.github.dockerjava.api.model.Volume("/code")))
                    .withTty(true)
                    .exec();

            if (container == null) {
                return "RE: Container creation failed, please check Docker configuration";
            }

            // 启动容器
            dockerClient.startContainerCmd(container.getId()).exec();

            // 设置超时
            long startTime = System.currentTimeMillis();
            long timeout = 20000L;

            // 等待容器运行完成
            dockerClient.waitContainerCmd(container.getId())
                    .exec(new com.github.dockerjava.core.command.WaitContainerResultCallback())
                    .awaitStatusCode();

            // 检查超时
            if (System.currentTimeMillis() - startTime > timeout) {
                dockerClient.stopContainerCmd(container.getId()).exec();
                return "TLE";
            }

            // 获取容器日志
            StringBuilder output = new StringBuilder();
            LogContainerResultCallback callback = new LogContainerResultCallback() {
                @Override
                public void onNext(com.github.dockerjava.api.model.Frame item) {
                    output.append(new String(item.getPayload()));
                }
            };
            dockerClient.logContainerCmd(container.getId())
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTailAll()
                    .exec(callback)
                    .awaitCompletion();

            String result = output.toString().trim();

            // 检查错误
            if (result.contains("error:") || result.contains("Exception in thread")) {
                return "RE: " + result;
            }

            return result;

        } catch (Exception e) {
            return "RE: " + e.getMessage();
        } finally {
            // 清理容器和临时文件（参考之前的递归清理逻辑）
            if (container != null) {
                try {
                    dockerClient.stopContainerCmd(container.getId()).exec();
                    dockerClient.removeContainerCmd(container.getId()).exec();
                } catch (Exception e) {
                    System.err.println("Error cleaning up container: " + e.getMessage());
                }
            }
            try {
                if (Files.exists(tempDir)) {
                    Files.walk(tempDir)
                            .sorted((a, b) -> b.compareTo(a))
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    System.err.println("Failed to delete: " + path + ", error: " + e.getMessage());
                                }
                            });
                }
            } catch (IOException e) {
                System.err.println("Error cleaning up temp directory: " + e.getMessage());
            }
        }
    }
}