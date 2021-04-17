package kboyle.degenerate.commands.modules;

import com.apptastic.rssreader.RssReader;
import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.commands.preconditions.RequireBotOwner;
import kboyle.degenerate.persistence.dao.PersistedRssFeedRepository;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.module.annotations.Require;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Aliases({"feed", "f"})
@Require(precondition = RequireBotOwner.class)
public class OwnerFeedModule extends DegenerateModule {
    private final RssReader rssReader;
    private final PersistedRssFeedRepository repo;

    public OwnerFeedModule(RssReader rssReader, PersistedRssFeedRepository repo) {
        this.rssReader = rssReader;
        this.repo = repo;
    }

    @Aliases({"add", "a"})
    public Mono<CommandResult> addFeed(String url) {
        return Mono.justOrEmpty(repo.findById(url))
            .flatMap(feed -> embed("Feed was already added"))
            .switchIfEmpty(
                Mono.fromFuture(() -> rssReader.readAsync(url))
                    .flatMap(items -> {
                        var guids = items.filter(item -> item.getGuid().isPresent())
                            .map(item -> item.getGuid().get())
                            .collect(Collectors.toSet());
                        repo.save(new PersistedRssFeed(url, guids));
                        return embed("Added feed");
                    })
            );
    }

    @Aliases({"refresh", "force"})
    public Mono<CommandResult> refreshFeed(PersistedRssFeed feed) {
        feed.getLastGuids().clear();
        repo.save(feed);
        return embed("Refreshed feed");
    }

    @Aliases({"remove", "rm", "r"})
    public Mono<CommandResult> removeFeed(PersistedRssFeed feed) {
        repo.delete(feed);
        return embed("Feed deleted");
    }

    @Aliases({"fetch"})
    public Mono<CommandResult> fetchFeed(PersistedRssFeed feed) {
        return Mono.fromFuture(rssReader.readAsync(feed.getUrl()))
            .flatMap(items -> {
                var guids = items.filter(item -> item.getGuid().isPresent())
                    .map(item -> item.getGuid().get())
                    .collect(Collectors.toSet());
                feed.getLastGuids().addAll(guids);
                repo.save(feed);
                return embed("Fetched feed");
            });
    }
}
