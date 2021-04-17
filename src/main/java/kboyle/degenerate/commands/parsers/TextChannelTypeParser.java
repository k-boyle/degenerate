package kboyle.degenerate.commands.parsers;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

public class TextChannelTypeParser extends DegenerateTypeParser<TextChannel> {
    @Override
    public Mono<TypeParserResult<TextChannel>> parse(DegenerateContext context, ReactiveCommand command, String input) {
        long id = -1;

        if (input.length() > 16 && input.charAt(0) == '<' && input.charAt(1) == '#') {
            var closingIndex = input.indexOf('>');

            if (closingIndex != -1 && closingIndex == input.length() - 1) {
                var strId = input.substring(2, closingIndex);
                try {
                    id = Long.parseLong(strId);
                } catch (NumberFormatException ignore) {
                }
            }
        }

        // java good (:
        var finalId = id;
        return context.guild()
            .flatMapMany(Guild::getChannels)
            .ofType(TextChannel.class)
            .filter(channel -> channel.getId().asLong() == finalId || channel.getName().equalsIgnoreCase(input))
            .next()
            .flatMap(this::monoSuccess)
            .switchIfEmpty(Mono.defer(() -> monoFailure("Failed to find channel matching %s, try mentioning it", input)));
    }
}
