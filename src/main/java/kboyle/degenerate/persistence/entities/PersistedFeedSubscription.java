package kboyle.degenerate.persistence.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

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
    @NonNull
    private Set<String> titleRegexes;

    @ElementCollection(fetch = FetchType.EAGER)
    @NonNull
    private Set<String> descriptionRegexes;

    @NonNull
    private Long channelId;
}
