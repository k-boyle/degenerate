package kboyle.degenerate.commands.parsers;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

public abstract class DegenerateTypeParser<T> implements TypeParser<T> {
    @Override
    public Mono<TypeParserResult<T>> parse(CommandContext context, Command command, String input) {
        return parse((DegenerateContext) context, command, input);
    }

    public abstract Mono<TypeParserResult<T>> parse(DegenerateContext context, Command command, String input);
}
