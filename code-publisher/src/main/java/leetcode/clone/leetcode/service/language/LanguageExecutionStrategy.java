package leetcode.clone.leetcode.service.language;

/**
 * How to run submitted code for one language inside its executor container.
 */
public interface LanguageExecutionStrategy {

    /** Language key as sent in the request, e.g. "python". */
    String language();

    /** Name of the running container that executes this language. */
    String containerName();

    /** Injects the test case input into the submitted code. */
    String prepareCode(String code, String input);

    /** Builds the command run in the container; it must be killed after {@code timeoutSeconds}. */
    String[] buildCommand(String code, int timeoutSeconds);
}
