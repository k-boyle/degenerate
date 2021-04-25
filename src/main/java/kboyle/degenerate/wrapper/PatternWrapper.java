package kboyle.degenerate.wrapper;

import java.util.regex.Pattern;

public class PatternWrapper {
    private final Pattern pattern;

    public PatternWrapper(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }

    public boolean match(String input) {
        return pattern.matcher(input).find();
    }

    public String regex() {
        return pattern.pattern();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PatternWrapper otherWrapper && otherWrapper.pattern.pattern().equals(pattern.pattern())
            || o instanceof Pattern otherPattern && otherPattern.pattern().equals(pattern.pattern());
    }

    @Override
    public int hashCode() {
        return pattern.pattern().hashCode();
    }
}
