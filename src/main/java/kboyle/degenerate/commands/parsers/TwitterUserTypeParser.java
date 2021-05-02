package kboyle.degenerate.commands.parsers;

import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.dto.user.UserV2;
import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

public class TwitterUserTypeParser extends DegenerateTypeParser<UserV2> {
    @Override
    public Mono<TypeParserResult<UserV2>> parse(DegenerateContext commandContext, Command command, String input) {
        var user = commandContext.beanProvider().getBean(TwitterClient.class)
            .getUserFromUserName(input);
        return (user.getData() == null
            ? failure("Failed to find a Twitter user with username %s", input)
            : success(user))
            .mono();
    }
}
