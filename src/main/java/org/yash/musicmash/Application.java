package org.yash.musicmash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.yash.musicmash.config.AppConfiguration;

@SpringBootApplication
@EnableConfigurationProperties({AppConfiguration.class})
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);

    // Uncomment this piece of code, to verify the RateLimit functionality
//    int i = 0;
//    IntStream.range(i, 10).parallel().forEach(t -> {
//      String response = new RestTemplate().getForObject(
//          "http://localhost:8080/musicmash/5b11f4ce-a62d-471e-81fc-a69a8278c7da",
//          String.class);
//      System.out.println(response);
//    });

  }

}
