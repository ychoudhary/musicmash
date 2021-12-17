package org.yash.musicmash.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.yash.musicmash.config.AppConfiguration;
import org.yash.musicmash.data.Album;
import org.yash.musicmash.data.MusicMash;
import org.yash.musicmash.exception.MusicMashServiceException;
import org.yash.musicmash.service.SourceService;
import org.yash.musicmash.utils.MusicMashConstants;

@Component
@Slf4j
public class CoverArtSourceServiceImpl implements SourceService {

  private RestTemplate restTemplate;
  private AppConfiguration config;


  @Autowired
  public CoverArtSourceServiceImpl(RestTemplate restTemplate,
      AppConfiguration config) {
    this.restTemplate = restTemplate;
    this.config = config;
  }

  @Override
  public MusicMash getDataFromSource(Map<String, Object> requestParams)
      throws MusicMashServiceException {
    MusicMash musicMash = (MusicMash) requestParams.get(MusicMashConstants.MUSIC_MASH);

    log.info("CoverArt: Getting image details for:{}", musicMash.getMbid());

    List<CompletableFuture<Album>> allFutures = new ArrayList<>();
//    List<Album> albumList = musicMash.getAlbums();
//
//    for (int index = 0; index < albumList.size(); index += 1) {
//      List<Album> subAlbumList = albumList.subList(index, Math.min(index + 1, albumList.size()));
//      subAlbumList.stream().forEach(album -> allFutures.add(callOtherService(album)));
//      CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
//    }
    musicMash.getAlbums().stream().forEach(album -> allFutures.add(callOtherService(album)));
    CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
    log.info("CoverArt: Returning with image details for:{}", musicMash.getMbid());
    return musicMash;
  }

  @Async
  public CompletableFuture<Album> callOtherService(Album album) {
    String coverArtReqURL = config.getSource().getCoverArtURL() + album.getId();
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    ResponseEntity<String> response = null;
    Instant startTime = Instant.now();
    try {
      HttpEntity<String> entity = new HttpEntity<>(headers);
      log.debug("Calling Music Brainz with URL. {}", coverArtReqURL);
      ResponseEntity<String> responseO = restTemplate.exchange(coverArtReqURL, HttpMethod.GET,
          entity,
          String.class);
      ObjectMapper mapper = new ObjectMapper();
      JsonNode actualObj = mapper.readTree(responseO.getBody());
      updateAlbumWithImages(album, actualObj);

    } catch (Exception ex) {
      log.error("CoverArt: Error While getting image for Title;{}", album.getTitle(), ex);
    } finally {
      log.info("CoverArt: Total time taken for Title:{} is {}ms", album.getTitle(),
          Duration.between(startTime, Instant.now()).toMillis());
    }
    return CompletableFuture.completedFuture(album);
  }

  private void updateAlbumWithImages(Album album, JsonNode responseObj) {
    String imageUrl = responseObj.get("images").get(0).get("image").toString();
    album.setImage(imageUrl);
  }
}
