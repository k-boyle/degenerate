package kboyle.degenerate.persistence.converters;

import kboyle.degenerate.wrapper.PatternWrapper;

import javax.persistence.AttributeConverter;

public class PatternConverter implements AttributeConverter<PatternWrapper, String> {
    @Override
    public String convertToDatabaseColumn(PatternWrapper pattern) {
        return pattern.regex();
    }

    @Override
    public PatternWrapper convertToEntityAttribute(String regex) {
        return new PatternWrapper(regex);
    }
}
