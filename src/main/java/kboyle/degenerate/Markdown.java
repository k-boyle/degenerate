package kboyle.degenerate;

// does d4j support this or am i dumb
public enum Markdown {
    ITALIC("*"),
    BOLD("**"),
    UNDERLINE("__"),
    STRIKE_THROUGH("~~"),
    CODE("`"),
    CODE_BLOCK("```%s\n", "\n```"),
    QUOTE("> ", ""),
    MULTILINE_QUOTE(">>> ", ""),
    URL("[%s](%s)", "")
    ;

    private final String left;
    private final String right;

    Markdown(String left) {
        this.left = left;
        this.right = left;
    }

    Markdown(String left, String right) {
        this.left = left;
        this.right = right;
    }

    public String format(String input, Object... args) {
        return String.format(left, args) + input + right;
    }
}
