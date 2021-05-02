package kboyle.degenerate.persistence.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersistedTwitterSubscription {
    @Id
    private String id;

    @EqualsAndHashCode.Exclude
    private Long channelId;

    @ElementCollection(fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    private Set<String> lastIds;

    @ManyToOne(fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PersistedGuild guild;
}
