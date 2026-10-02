package leetcode.clone.leetcode.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import leetcode.clone.leetcode.model.ExecutionRequest;
import leetcode.clone.leetcode.model.ExecutionResponse;
import leetcode.clone.leetcode.service.language.LanguageExecutionStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DockerExecutorService {

    private final DockerClient client;
    private final List<LanguageExecutionStrategy> strategyList;
    private static final int MAX_OUTPUT_BYTES = 64 * 1024;
    private static final long TIMEOUT_EXIT_CODE = 124; // exit code of coreutils/busybox `timeout`

    private Map<String, LanguageExecutionStrategy> strategies;

    @PostConstruct
    void indexStrategies() {
        strategies = strategyList.stream()
                .collect(Collectors.toMap(LanguageExecutionStrategy::language, Function.identity()));
    }

    public ExecutionResponse executeCode(ExecutionRequest request){
        long start = System.currentTimeMillis();

        LanguageExecutionStrategy strategy = strategies.get(request.getLanguage());
        if(strategy == null) throw new RuntimeException("Language not supported");
        ExecutionResponse response = new ExecutionResponse();
        response.setSuccess(true);

        List<ExecutionResponse.TestCaseResult> results = new ArrayList<>();
        for(ExecutionRequest.TestCase testCase : request.getTestCases()){
            ExecutionResponse.TestCaseResult result = executeTestCase(
                    strategy,
                    request.getCode(),
                    testCase,
                    request.getTimeoutSeconds()
            );
            results.add(result);
            if(!result.isPassed()) response.setSuccess(false);
        }

        response.setTestCaseResult(results);
        response.setExecutionTimeMs(System.currentTimeMillis() - start);
        return response;

    }

    private ExecutionResponse.TestCaseResult executeTestCase(
            LanguageExecutionStrategy strategy, String code,
            ExecutionRequest.TestCase testCase,
            int timeOutSeconds
    ){
        long startTime = System.currentTimeMillis();
        ExecutionResponse.TestCaseResult result = new ExecutionResponse.TestCaseResult();
        result.setInput(testCase.getInput());
        result.setExpectedOutputResult(testCase.getExpectedOutput());

        try {
            String fullCode = strategy.prepareCode(code, testCase.getInput());
            String[] command = strategy.buildCommand(fullCode, timeOutSeconds);
            ExecResult execResult = executeInContainer(strategy.containerName(), command, timeOutSeconds);

            if (execResult.exitCode() == TIMEOUT_EXIT_CODE) {
                throw new RuntimeException("Time limit exceeded (" + timeOutSeconds + "s)");
            }
            if (execResult.exitCode() != 0) {
                String stdErr = execResult.stdErr().trim();
                throw new RuntimeException(stdErr.isEmpty()
                        ? "Process exited with code " + execResult.exitCode()
                        : "Execution error: " + stdErr);
            }

            // exit code 0: stderr (compiler notes, warnings) is ignored, only stdout is compared
            String actualOutput = execResult.stdOut().trim();
            result.setActualOutput(actualOutput);
            boolean passed = actualOutput.equals(testCase.getExpectedOutput().trim());
            result.setPassed(passed);

        }catch (Exception e){
            result.setPassed(false);
            result.setError(e.getMessage());
        }

        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        return result;
    }

    private ExecResult executeInContainer(String containerName, String[] command, int timeOutSeconds) throws Exception {

        String execId = client.execCreateCmd(containerName)
                .withCmd(command)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .exec()
                .getId();

        LimitedOutputStream stdOut = new LimitedOutputStream(MAX_OUTPUT_BYTES);
        LimitedOutputStream stdErr = new LimitedOutputStream(MAX_OUTPUT_BYTES);

        // Grace period: the in-container `timeout` kills the process first, this is only a backstop
        boolean finished = client.execStartCmd(execId)
                .exec(new ExecStartResultCallback(stdOut, stdErr))
                .awaitCompletion(timeOutSeconds + 5L, TimeUnit.SECONDS);

        if (!finished) throw new RuntimeException("Time limit exceeded (" + timeOutSeconds + "s)");
        if (stdOut.isTruncated()) throw new RuntimeException("Output limit exceeded");

        Long exitCode = client.inspectExecCmd(execId).exec().getExitCodeLong();
        return new ExecResult(
                stdOut.toString(StandardCharsets.UTF_8),
                stdErr.toString(StandardCharsets.UTF_8),
                exitCode == null ? -1 : exitCode);
    }

    public boolean isContainerRunning(String language) {
        LanguageExecutionStrategy strategy = strategies.get(language.toLowerCase());
        if (strategy == null) {
            return false;
        }
        String containerName = strategy.containerName();

        try {
            client.inspectContainerCmd(containerName).exec();
            return true;
        } catch (DockerException e) {
            return false;
        }
    }

    private record ExecResult(String stdOut, String stdErr, long exitCode) {}
    private static class LimitedOutputStream extends ByteArrayOutputStream {
        private final int limit;
        private boolean truncated;

        LimitedOutputStream(int limit) {
            this.limit = limit;
        }

        boolean isTruncated() {
            return truncated;
        }

        @Override
        public synchronized void write(int b) {
            if (count >= limit) {
                truncated = true;
                return;
            }
            super.write(b);
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            int allowed = Math.min(len, limit - count);
            if (allowed < len) truncated = true;
            if (allowed > 0) super.write(b, off, allowed);
        }
    }

}
