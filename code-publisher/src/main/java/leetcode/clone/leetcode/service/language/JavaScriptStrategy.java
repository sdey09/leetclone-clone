package leetcode.clone.leetcode.service.language;

import org.springframework.stereotype.Component;

@Component
public class JavaScriptStrategy extends AbstractLanguageStrategy {

    @Override
    public String language() {
        return "javascript";
    }

    @Override
    public String containerName() {
        return "javascript-executor";
    }

    @Override
    public String[] buildCommand(String code, int timeoutSeconds) {
        return new String[]{"timeout", String.valueOf(timeoutSeconds), "node", "-e", code};
    }
}
