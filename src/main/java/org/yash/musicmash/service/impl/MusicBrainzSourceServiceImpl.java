package org.yash.musicmash.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.yash.musicmash.config.AppConfiguration;
import org.yash.musicmash.data.Album;
import org.yash.musicmash.data.MusicMash;
import org.yash.musicmash.data.internal.musicbrainz.MusicBrainz;
import org.yash.musicmash.data.internal.musicbrainz.Relation;
import org.yash.musicmash.data.internal.musicbrainz.ReleaseGroup;
import org.yash.musicmash.data.internal.wikidata.SiteLink;
import org.yash.musicmash.data.internal.wikidata.WikiData;
import org.yash.musicmash.data.internal.wikipedia.WikiPedia;
import org.yash.musicmash.exception.MusicMashServiceException;
import org.yash.musicmash.exception.NoMusicMashException;
import org.yash.musicmash.exception.SlowResponseException;
import org.yash.musicmash.exception.SourceErrorException;
import org.yash.musicmash.exception.SourceMappingException;
import org.yash.musicmash.exception.UncaughtSourceErrorException;
import org.yash.musicmash.service.SourceService;
import org.yash.musicmash.utils.MusicMashConstants;

/**
 * The MusicBrainzSourceService gets the data from musicbrainz web api and merge the details from
 * wikipedia APIs.
 * <p>
 * As musicbrainz API supports only few requests per host, we have implemented the RateLimit
 * feature, which will force only specific number of requests to be served.
 */
@Component
@Slf4j
public class MusicBrainzSourceServiceImpl implements SourceService {

  private RestTemplate restTemplate;
  private AppConfiguration config;
  private ObjectMapper mapper;


  @Autowired
  public MusicBrainzSourceServiceImpl(RestTemplate restTemplate,
      AppConfiguration config) {
    this.restTemplate = restTemplate;
    this.config = config;
    initMapper();
  }

  private void initMapper() {
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    SimpleModule module = new SimpleModule();
    mapper.registerModule(module);
  }

  @RateLimiter(name = "musicbrainz")
  @Override
  public MusicMash getDataFromSource(Map<String, Object> requestParams)
      throws MusicMashServiceException {
    String mbid = (String) requestParams.get(MusicMashConstants.MBID);
    MusicMash musicMash = new MusicMash();

    String response = callApi(createMusicBrainzSourceUrl(mbid));
    MusicBrainz musicBrainz = mapMusicBrainzValues(response);
    if (musicBrainz != null && !CollectionUtils.isEmpty(musicBrainz.getRelations())) {
      // Update the mbid and Albums List
      musicMash.setMbid(musicBrainz.getId());
      musicMash.setAlbums(createAlbumDetails(musicBrainz.getReleaseGroups()));

      Relation wikiDataRel = musicBrainz.getRelations().stream()
          .filter(rel -> "wikidata".equals(rel.getType())).findFirst().orElse(null);
      if (wikiDataRel != null) {
        var qId = wikiDataRel.getUrl().getQID();
        var wikiDataQueryUrl = createWikiDataUrl(qId);
        String wikiResponse = callApi(wikiDataQueryUrl);
        WikiData wikiData = mapWikiDataValues(wikiResponse);
        SiteLink engSiteLink = wikiData.getEntities().get(qId).getSitelinks().get("enwiki");
        if (engSiteLink != null) {
          var wikiPediaQueryUrl = createWikiPediaUrl(engSiteLink.getTitle());
          var wikiPediaResposne = callApi(wikiPediaQueryUrl);
          var wikiPedia = mapWikiPediaValues(wikiPediaResposne);
          var page = wikiPedia.getQuery().getPages().values().stream().findFirst().orElse(null);
          if (page != null) {
            musicMash.setDescription(page.getExtract());
          }
        }
      }
    }

    return musicMash;
  }

  private List<Album> createAlbumDetails(List<ReleaseGroup> releaseGroups) {
    return releaseGroups.stream()
        .filter(releaseGroup -> "Album".equals(releaseGroup.getPrimaryType()))
        .map(relG -> new Album(relG.getTitle(), relG.getId(), null)).collect(
            Collectors.toList());
  }


  private String callApi(String url) throws MusicMashServiceException {

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    ResponseEntity<String> response = null;

    try {
      HttpEntity<String> entity = new HttpEntity<>(headers);
      log.debug("Calling Music Brainz with URL. {}", url);
      response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      return response.getBody();
    } catch (HttpStatusCodeException e) {
      if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
        throw new NoMusicMashException(MusicMashConstants.ERROR_MESSAGE_NOT_VALID_MBID,
            e.getMessage());
      } else if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
        throw new NoMusicMashException(MusicMashConstants.ERROR_MESSAGE_NO_MBID_FOUND,
            e.getMessage());
      } else {
        throw new SourceErrorException(
            "There was an error from eSales with error code " + e.getStatusCode(),
            e, e.getStatusCode(), url, e.getResponseBodyAsString());
      }
    } catch (ResourceAccessException e) {
      throw new SlowResponseException("The connection with eSales took too long", e, url);
    } catch (RestClientException e) {
      throw new SourceErrorException("There was an error from eSales", e, url);
    } catch (Exception e) {
      throw new UncaughtSourceErrorException("There was an uncaught error from eSales", e, url);
    }
  }

  private MusicBrainz mapMusicBrainzValues(String res) throws SourceMappingException {
    try {
      return mapper.readValue(res, MusicBrainz.class);
    } catch (Exception e) {
      throw new SourceMappingException(e.getMessage(), e, res);
    }
  }


  private WikiData mapWikiDataValues(String res) throws SourceMappingException {
    try {
      return mapper.readValue(res, WikiData.class);
    } catch (Exception e) {
      throw new SourceMappingException(e.getMessage(), e, res);
    }
  }


  private WikiPedia mapWikiPediaValues(String wikiPediaResposne) throws SourceMappingException {
    try {
      return mapper.readValue(wikiPediaResposne, WikiPedia.class);
    } catch (Exception e) {
      throw new SourceMappingException(e.getMessage(), e, wikiPediaResposne);
    }
  }


  private String createMusicBrainzSourceUrl(String mbid) {
    var uriComponentsBuilder =
        UriComponentsBuilder.fromHttpUrl(
                config.getSource().getMusicBrainzURL()).path("ws/2/artist/")
            .path(mbid).queryParam("fmt", "json")
            .queryParam("inc", "url-rels+release-groups");

    return uriComponentsBuilder.build(false).encode().toUriString();
  }


  private String createWikiDataUrl(String qid) {
    var uriComponentsBuilder =
        UriComponentsBuilder.fromHttpUrl(
                config.getSource().getWikiDataURL())
            .queryParam("format", "json").queryParam("ids", qid)
            .queryParam("action", "wbgetentities").queryParam("props", "sitelinks");

    return uriComponentsBuilder.build(false).encode().toUriString();
  }

  private String createWikiPediaUrl(String title) {
    var uriComponentsBuilder =
        UriComponentsBuilder.fromHttpUrl(
                config.getSource().getWikiPediaURL())
            .queryParam("format", "json").queryParam("titles", title)
            .queryParam("action", "query").queryParam("prop", "extracts")
            .queryParam("exintro", "true");

    return uriComponentsBuilder.build(false).toUriString();
  }
}
