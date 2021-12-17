package org.yash.musicmash.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties("service")
@Data
public class AppConfiguration {


  @Data
  public static class Source {
    private String wikiDataURL;
    private String wikiPediaURL;
    private String musicBrainzURL;
    private String coverArtURL;

    private Integer connectTimeOutMs;
    private Integer readTimeoutMS;
    private Integer connectionRequestTimeoutMS;
    private Integer connectionPoolMaxTotal;
    private Integer connectionPoolMaxPerRoute;
  }

  private Source source;
}
