package leetcode.clone.leetcode.service.language;

import org.springframework.stereotype.Component;

@Component
public class JavaStrategy extends AbstractLanguageStrategy {

    @Override
    public String language() {
        return "java";
    }

    @Override
    public String containerName() {
        return "java-executor";
    }

    @Override
    public String[] buildCommand(String code, int timeoutSeconds) {
        return compileAndRun("Solution.java", code, "javac Solution.java", "java Solution", timeoutSeconds);
    }
}
