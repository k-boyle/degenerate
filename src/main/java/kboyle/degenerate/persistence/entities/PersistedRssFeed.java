package kboyle.degenerate.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "feeds")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersistedRssFeed {
    @Id
    private String url;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> lastGuids;
}
