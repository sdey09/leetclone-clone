package leetcode.clone.leetcode.service.language;

import org.springframework.stereotype.Component;

@Component
public class CppStrategy extends AbstractLanguageStrategy {

    @Override
    public String language() {
        return "cpp";
    }

    @Override
    public String containerName() {
        return "cpp-executor";
    }

    @Override
    public String[] buildCommand(String code, int timeoutSeconds) {
        return compileAndRun("solution.cpp", code, "g++ -o solution solution.cpp", "./solution", timeoutSeconds);
    }
}
