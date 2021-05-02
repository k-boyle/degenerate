package kboyle.degenerate.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "guilds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersistedGuild {
    @Id
    private long id;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> prefixes;

    @OneToMany(fetch = FetchType.EAGER)
    private Map<PersistedRssFeed, PersistedFeedSubscription> subscriptionByFeedUrl;

    @OneToMany(fetch = FetchType.EAGER)
    private Set<PersistedTwitterSubscription> twitterSubscriptions;
}
