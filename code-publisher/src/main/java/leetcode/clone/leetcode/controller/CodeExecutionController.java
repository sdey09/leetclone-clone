package leetcode.clone.leetcode.controller;

import jakarta.validation.Valid;
import leetcode.clone.leetcode.model.ExecutionRequest;
import leetcode.clone.leetcode.model.ExecutionResponse;
import leetcode.clone.leetcode.service.DockerExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/execute")
@RequiredArgsConstructor
public class CodeExecutionController {

    private final DockerExecutorService executorService;

    /**
     * Execute code endpoint
     */
    @PostMapping
    public ResponseEntity<ExecutionResponse> executeCode(@Valid @RequestBody ExecutionRequest request) {
        ExecutionResponse response = executorService.executeCode(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check for specific language container
     */
    @GetMapping("/health/{language}")
    public ResponseEntity<String> checkContainerHealth(@PathVariable String language) {
        boolean isRunning = executorService.isContainerRunning(language);

        if (isRunning) {
            return ResponseEntity.ok("Container for " + language + " is running");
        } else {
            return ResponseEntity.status(503).body("Container for " + language + " is not running");
        }
    }
}