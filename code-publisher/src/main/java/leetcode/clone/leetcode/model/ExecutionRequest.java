package leetcode.clone.leetcode.model;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ExecutionRequest {
    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Code is required")
    private String code;

    @NotNull(message = "Test cases are required")
    private List<TestCase> testCases;

    private Integer timeoutSeconds = 5; // Default 5 seconds

    @Getter
    @Setter
    public static class TestCase {
        private String input;
        private String expectedOutput;
    }
}
