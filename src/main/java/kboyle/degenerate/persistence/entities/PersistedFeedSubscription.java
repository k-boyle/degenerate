package kboyle.degenerate.persistence.entities;

import kboyle.degenerate.persistence.converters.PatternConverter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.regex.Pattern;

@Entity
@Table(name = "subscribed_feeds")
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class PersistedFeedSubscription implements Serializable {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    @NonNull
    private PersistedRssFeed feed;

    @ElementCollection(fetch = FetchType.EAGER)
    @Convert(converter = PatternConverter.class)
    @NonNull
    private Set<Pattern> titleRegexes;

    @ElementCollection(fetch = FetchType.EAGER)
    @Convert(converter = PatternConverter.class)
    @NonNull
    private Set<Pattern> descriptionRegexes;

    @NonNull
    private Long channelId;

    @ElementCollection(fetch = FetchType.EAGER)
    @NonNull
    private Set<String> lastGuids;

    @NonNull
    private Long guildId;
}
