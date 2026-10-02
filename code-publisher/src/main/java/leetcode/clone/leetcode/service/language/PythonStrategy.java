package leetcode.clone.leetcode.service.language;

import org.springframework.stereotype.Component;

@Component
public class PythonStrategy extends AbstractLanguageStrategy {

    @Override
    public String language() {
        return "python";
    }

    @Override
    public String containerName() {
        return "python-executor";
    }

    @Override
    public String[] buildCommand(String code, int timeoutSeconds) {
        return new String[]{"timeout", String.valueOf(timeoutSeconds), "python3", "-c", code};
    }
}
