package leetcode.clone.leetcode.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class ExecutionResponse {

    private boolean success;
    private String message;
    private List<TestCaseResult> testCaseResult;
    private long executionTimeMs;
    private String error;

    public ExecutionResponse(boolean success, String message){
        this.success = success;
        this.message = message;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class TestCaseResult{
        private boolean passed;
        private String input;
        private String expectedOutputResult;
        private String actualOutput;
        private String error;
        private long executionTimeMs;

    }

}
