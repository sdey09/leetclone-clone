package leetcode.clone.leetcode.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import leetcode.clone.leetcode.model.ExecutionRequest;
import leetcode.clone.leetcode.model.ExecutionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DockerExecutorService {

    private final DockerClient client;
    private static final Map<String, String> CONTAINER_MAP = new HashMap<>();

    static {
        CONTAINER_MAP.put("python", "python-executor");
        CONTAINER_MAP.put("java", "java-executor");
        CONTAINER_MAP.put("cpp", "cpp-executor");
        CONTAINER_MAP.put("javascript", "javascript-executor");
    }

    public ExecutionResponse executeCode(ExecutionRequest request){
        long start = System.currentTimeMillis();

        String language = request.getLanguage();
        String containerName = CONTAINER_MAP.get(language);

        if(containerName == null) throw new RuntimeException("Language not supported");
        ExecutionResponse response = new ExecutionResponse();
        response.setSuccess(true);

        List<ExecutionResponse.TestCaseResult> results = new ArrayList<>();
        for(ExecutionRequest.TestCase testCase : request.getTestCases()){
            ExecutionResponse.TestCaseResult result = executeTestCase(
                    containerName,
                    language,
                    request.getCode(),
                    testCase,
                    request.getTimeoutSeconds()
            );
            results.add(result);
            if(!result.isPassed()) result.setPassed(false);
        }

        response.setTestCaseResult(results);
        response.setExecutionTimeMs(System.currentTimeMillis() - start);
        return response;

    }

    private ExecutionResponse.TestCaseResult executeTestCase(
            String containerName, String language, String code,
            ExecutionRequest.TestCase testCase,
            int timeOutSeconds
    ){
        long startTime = System.currentTimeMillis();
        ExecutionResponse.TestCaseResult result = new ExecutionResponse.TestCaseResult();
        result.setInput(testCase.getInput());
        result.setExpectedOutputResult(testCase.getExpectedOutput());

        try {
            String fullCode = prepareCode(language, code, testCase.getInput());
            String[] command = buildExecutionCommand(language, fullCode);
            String output = executeInContainer(containerName, command, timeOutSeconds);

            String actualOutput = output.trim();
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

    private String executeInContainer(String containerName, String[] command, int timeOutSeconds) throws Exception {

        ExecCreateCmdResponse execCreateCmdResponse = client.execCreateCmd(containerName)
                .withCmd(command)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .exec();

        ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

        ExecStartResultCallback callback = new ExecStartResultCallback(stdOut, stdErr);

        client.execStartCmd(execCreateCmdResponse.getId())
                .exec(callback)
                .awaitCompletion(timeOutSeconds, TimeUnit.SECONDS);

        String errorOutput = stdErr.toString().trim();
        if(!errorOutput.isEmpty()) throw new RuntimeException("Execution error: " + errorOutput);

        return stdOut.toString();
    }

    private String prepareCode(String language, String code, String input) {
        return switch (language) {
            case "python" -> preparePythonCode(code, input);
            case "java" -> prepareJavaCode(code, input);
            case "cpp" -> prepareCppCode(code, input);
            case "javascript" -> prepareJavaScriptCode(code, input);
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    private String[] buildExecutionCommand(String language, String code) {
        switch (language) {
            case "python":
                return new String[]{"python3", "-c", code};
            case "java":
                // For Java, we need to save to file, compile, and run
                return new String[]{"sh", "-c",
                        "echo '" + escapeCode(code) + "' > Solution.java && " +
                                "javac Solution.java && " +
                                "java Solution"};
            case "cpp":
                return new String[]{"sh", "-c",
                        "echo '" + escapeCode(code) + "' > solution.cpp && " +
                                "g++ -o solution solution.cpp && " +
                                "./solution"};
            case "javascript":
                return new String[]{"node", "-e", code};
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }

    private String preparePythonCode(String code, String input) {
        // Replace placeholder with actual input
        return code.replace("{{INPUT}}", input);
    }

    private String prepareJavaCode(String code, String input) {
        return code.replace("{{INPUT}}", input);
    }

    private String prepareCppCode(String code, String input) {
        return code.replace("{{INPUT}}", input);
    }

    private String prepareJavaScriptCode(String code, String input) {
        return code.replace("{{INPUT}}", input);
    }

    private String escapeCode(String code) {
        return code.replace("'", "'\\''");
    }

    public boolean isContainerRunning(String language) {
        String containerName = CONTAINER_MAP.get(language.toLowerCase());
        if (containerName == null) {
            return false;
        }

        try {
            client.inspectContainerCmd(containerName).exec();
            return true;
        } catch (DockerException e) {
            return false;
        }
    }

}
