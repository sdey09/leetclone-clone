package leetcode.clone.leetcode.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.HostConfig;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContainerManagementService {

    private final DockerClient client;
    private static final Map<String, ContainerConfig> CONTAINER_CONFIG_MAP = new HashMap<>();

    static {
        CONTAINER_CONFIG_MAP.put("python", new ContainerConfig(
                "python-executor",
                "python-executor:latest",
                512 * 1024 * 1024L, // 512MB
                1_000_000_000L // 1 CPU core
        ));
        CONTAINER_CONFIG_MAP.put("java", new ContainerConfig(
                "java-executor",
                "java-executor:latest",
                768 * 1024 * 1024L, // 768MB
                1_000_000_000L
        ));

        CONTAINER_CONFIG_MAP.put("cpp", new ContainerConfig(
                "cpp-executor",
                "cpp-executor:latest",
                512 * 1024 * 1024L,
                1_000_000_000L
        ));

        CONTAINER_CONFIG_MAP.put("javascript", new ContainerConfig(
                "javascript-executor",
                "javascript-executor:latest",
                512 * 1024 * 1024L,
                1_000_000_000L
        ));

        CONTAINER_CONFIG_MAP.put("go", new ContainerConfig(
                "go-executor",
                "go-executor:latest",
                512 * 1024 * 1024L,
                1_000_000_000L
        ));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeContainers() {
        log.info("Initializing language execution containers...");

        for (Map.Entry<String, ContainerConfig> entry : CONTAINER_CONFIG_MAP.entrySet()) {
            String language = entry.getKey();
            ContainerConfig config = entry.getValue();

            try {
                ensureContainerRunning(language, config);
                log.info("Container for {} is running: {}", language, config.containerName);
            } catch (Exception e) {
                log.error("Failed to start container for {}: {}", language, e.getMessage());
            }
        }
        log.info("Container initialization complete");
    }


    public void ensureContainerRunning(String language, ContainerConfig containerConfig){
        try {
            try {
                var containerInfo = client.inspectContainerCmd(containerConfig.containerName).exec();
                if(Boolean.TRUE.equals(containerInfo.getState().getRunning())){
                    log.info("Container {} is already running", containerConfig.containerName);
                    return;
                }else{
                    log.info("Container {} exists but not running, starting...", containerConfig.containerName);
                    client.startContainerCmd(containerConfig.containerName).exec();
                    return;
                }
            }catch (DockerException e){

            }

            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withMemory(containerConfig.memoryLimit)
                    .withMemorySwap(containerConfig.memoryLimit) // No swap
                    .withNanoCPUs(containerConfig.cpuLimit)
                    .withPidsLimit(100L) // Limit number of processes
                    .withNetworkMode("none") // No network access
                    .withReadonlyRootfs(false); // Allow writes to tmpfs

            CreateContainerResponse container = client.createContainerCmd(containerConfig.imageName)
                    .withName(containerConfig.containerName)
                    .withHostConfig(hostConfig)
                    .withTty(false)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            client.startContainerCmd(containerConfig.containerName).exec();
        }catch (Exception e){
            throw new RuntimeException("Failed to start container for " + language, e);
        }
    }


    @AllArgsConstructor
    public static class ContainerConfig {
        String containerName;
        String imageName;
        Long memoryLimit;
        Long cpuLimit;
    }
}
