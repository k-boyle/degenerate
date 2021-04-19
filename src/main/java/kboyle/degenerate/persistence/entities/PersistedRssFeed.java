package kboyle.degenerate.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "feeds")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersistedRssFeed {
    @Id
    private String url;
}
