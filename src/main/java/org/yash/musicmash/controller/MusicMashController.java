package org.yash.musicmash.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.yash.musicmash.config.AppConfiguration;
import org.yash.musicmash.data.MusicMash;
import org.yash.musicmash.data.error.ApiError;
import org.yash.musicmash.exception.MusicMashServiceException;
import org.yash.musicmash.exception.NoMusicMashException;
import org.yash.musicmash.exception.SourceRequestRateTooLarge;
import org.yash.musicmash.service.SourceService;
import org.yash.musicmash.utils.MusicMashConstants;

@Data
@RestController
@Slf4j
class MusicMashController {

  AppConfiguration config;
  SourceService musicBrainzSourceServiceImpl;

  SourceService coverArtSourceServiceImpl;

  @Autowired
  MusicMashController(AppConfiguration config, SourceService musicBrainzSourceServiceImpl,
      SourceService coverArtSourceServiceImpl) {
    this.config = config;
    this.musicBrainzSourceServiceImpl = musicBrainzSourceServiceImpl;
    this.coverArtSourceServiceImpl = coverArtSourceServiceImpl;
  }

  @GetMapping("/musicmash/{mbId}")
  MusicMash getMusicById(@PathVariable String mbId) throws MusicMashServiceException {

    try {
      // Get the MusicBrainz Details along with Wiki descriptions
      MusicMash response = this.musicBrainzSourceServiceImpl.getDataFromSource(
          Map.of(MusicMashConstants.MBID, mbId));

      // Now Call the Cover Art Service, to update the Albums List to get Image Details
      response = this.coverArtSourceServiceImpl.getDataFromSource(
          Map.of(MusicMashConstants.MUSIC_MASH, response));

      return response;
    } catch (RequestNotPermitted e) {
      throw new SourceRequestRateTooLarge(
          "Request rate to eSales reached from just for you endpoint!", e);
    }
  }


  @ExceptionHandler(NoMusicMashException.class)
  public final ResponseEntity<ApiError> handleNoRecommendations(
      NoMusicMashException ex, WebRequest request) {
    log.error("No recommendations for {}", ex.getId(), ex);
    ApiError errorDetails =
        new ApiError(
            ex.getId(), request.getDescription(false));
    return prepareErrorResponse(errorDetails, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(SourceRequestRateTooLarge.class)
  public final ResponseEntity<ApiError> handleEsalesRequestRateTooLarge(
      SourceRequestRateTooLarge ex, WebRequest request) {
    log.error("Error: Too many requests to eSales.", ex);
    ApiError errorDetails =
        new ApiError(
            MusicMashConstants.ERROR_MESSAGE_REQUEST_RATE_TOO_LARGE, request.getDescription(false));
    return prepareErrorResponse(errorDetails, HttpStatus.SERVICE_UNAVAILABLE);
  }

  private ResponseEntity<ApiError> prepareErrorResponse(
      ApiError errorDetails, HttpStatus httpStatus) {

    MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>(1);
    return new ResponseEntity<>(errorDetails, headerMap, httpStatus);
  }

}