package kboyle.degenerate.configuration;

import com.apptastic.rssreader.RssReader;
import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.signature.TwitterCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DegenerateConfiguration {
    @Bean
    public RssReader rssReader() {
        return new RssReader();
    }

    @Bean
    public TwitterClient twitterClient(DegenerateConfig degenerateConfig) {
        var twitter = degenerateConfig.getTwitter();
        return new TwitterClient(
            TwitterCredentials.builder()
                .accessToken(twitter.getAccessToken())
                .accessTokenSecret(twitter.getAccessTokenSecret())
                .apiKey(twitter.getApiKey())
                .apiSecretKey(twitter.getApiSecret())
                .build()
        );
    }
}
