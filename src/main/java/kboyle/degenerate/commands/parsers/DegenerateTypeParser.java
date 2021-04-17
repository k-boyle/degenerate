package kboyle.degenerate.commands.parsers;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.parsers.ReactiveTypeParser;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

public abstract class DegenerateTypeParser<T> implements ReactiveTypeParser<T> {
    @Override
    public Mono<TypeParserResult<T>> parse(CommandContext context, ReactiveCommand command, String input) {
        return parse((DegenerateContext) context, command, input);
    }

    public abstract Mono<TypeParserResult<T>> parse(DegenerateContext context, ReactiveCommand command, String input);
}
