package kboyle.degenerate.persistence.converters;

import kboyle.oktane.core.prefix.Prefix;
import kboyle.oktane.core.prefix.StringPrefix;

import javax.persistence.AttributeConverter;

public class PrefixConverter implements AttributeConverter<Prefix, String> {
    @Override
    public String convertToDatabaseColumn(Prefix prefix) {
        return prefix.value().toString();
    }

    @Override
    public Prefix convertToEntityAttribute(String s) {
        return new StringPrefix(s);
    }
}
