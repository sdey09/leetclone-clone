package leetcode.clone.leetcode.service.language;

abstract class AbstractLanguageStrategy implements LanguageExecutionStrategy {

    private static final String INPUT_PLACEHOLDER = "{{INPUT}}";

    @Override
    public String prepareCode(String code, String input) {
        return code.replace(INPUT_PLACEHOLDER, input);
    }

    /**
     * Compile-then-run languages: write the source into a unique temp dir (so concurrent
     * submissions don't overwrite each other), compile, run under `timeout`, then clean up.
     */
    protected String[] compileAndRun(String fileName, String code, String compileCommand,
                                     String runCommand, int timeoutSeconds) {
        return new String[]{"sh", "-c",
                "d=$(mktemp -d) && cd \"$d\" && " +
                        "echo '" + escapeCode(code) + "' > " + fileName + " && " +
                        compileCommand + " && " +
                        "timeout " + timeoutSeconds + " " + runCommand + "; " +
                        "rc=$?; rm -rf \"$d\"; exit $rc"};
    }

    private String escapeCode(String code) {
        return code.replace("'", "'\\''");
    }
}
