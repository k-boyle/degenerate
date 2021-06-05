package kboyle.degenerate.persistence.entities;

import kboyle.degenerate.persistence.converters.PrefixConverter;
import kboyle.oktane.core.prefix.Prefix;
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
    @Convert(converter = PrefixConverter.class)
    private Set<Prefix> prefixes;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Map<PersistedRssFeed, PersistedFeedSubscription> subscriptionByFeedUrl;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PersistedTwitterSubscription> twitterSubscriptions;
}
