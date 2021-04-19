package kboyle.degenerate.persistence.converters;

import javax.persistence.AttributeConverter;
import java.util.regex.Pattern;

public class PatternConverter implements AttributeConverter<Pattern, String> {
    @Override
    public String convertToDatabaseColumn(Pattern pattern) {
        return pattern.pattern();
    }

    @Override
    public Pattern convertToEntityAttribute(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }
}
