package kboyle.degenerate.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "degenerate")
@Getter
@Setter
public class DegenerateConfig {
    private Discord discord;
    private Feed feed;

    @Getter
    @Setter
    public static class Discord {
        private String token;
    }

    @Getter
    @Setter
    public static class Feed {
        private Duration pollingRate;
        private boolean enabled;
    }
}
