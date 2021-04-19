package kboyle.degenerate.commands.modules;

import com.apptastic.rssreader.RssReader;
import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.commands.preconditions.RequireBotOwner;
import kboyle.degenerate.persistence.dao.PersistedRssFeedRepository;
import kboyle.degenerate.persistence.entities.PersistedRssFeed;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.module.annotations.Name;
import kboyle.oktane.reactive.module.annotations.Require;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Aliases({"feed", "f"})
@Require(precondition = RequireBotOwner.class)
@Name("Owner Feed")
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
                        repo.save(new PersistedRssFeed(url));
                        return embed("Added feed");
                    })
            );
    }

//    @Aliases({"refresh", "force"})
//    public Mono<CommandResult> refreshFeed(PersistedRssFeed feed) {
//        feed.getLastGuids().clear();
//        repo.save(feed);
//        return embed("Refreshed feed");
//    }

    @Aliases({"remove", "rm", "r"})
    public Mono<CommandResult> removeFeed(PersistedRssFeed feed) {
        repo.delete(feed);
        return embed("Feed deleted");
    }

    @Aliases("preview")
    public Mono<CommandResult> previewFeed(String url) throws IOException {
        var feed = rssReader.read("https://rss.app/feeds/3DWLkKnzgKpxUjJG.xml");
        return nop();
    }

//    @Aliases({"fetch"})
//    public Mono<CommandResult> fetchFeed(PersistedRssFeed feed) {
//        return Mono.fromFuture(rssReader.readAsync(feed.getUrl()))
//            .flatMap(items -> {
//                var guids = items.filter(item -> item.getGuid().isPresent())
//                    .map(item -> item.getGuid().get())
//                    .collect(Collectors.toSet());
//                feed.getLastGuids().addAll(guids);
//                repo.save(feed);
//                return embed("Fetched feed");
//            });
//    }
}
