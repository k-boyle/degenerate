package kboyle.degenerate.persistence.entities;

import kboyle.degenerate.persistence.converters.PatternConverter;
import kboyle.degenerate.wrapper.PatternWrapper;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "subscribed_feeds")
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class PersistedFeedSubscription {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    @NonNull
    private PersistedRssFeed feed;

    @ElementCollection(fetch = FetchType.EAGER)
    @Convert(converter = PatternConverter.class)
    @NonNull
    @EqualsAndHashCode.Exclude
    private Set<PatternWrapper> titleRegexes;

    @ElementCollection(fetch = FetchType.EAGER)
    @Convert(converter = PatternConverter.class)
    @NonNull
    @EqualsAndHashCode.Exclude
    private Set<PatternWrapper> descriptionRegexes;

    @NonNull
    private Long channelId;

    @ElementCollection(fetch = FetchType.EAGER)
    @NonNull
    @EqualsAndHashCode.Exclude
    private Set<String> lastGuids;

    @NonNull
    private Long guildId;
}
