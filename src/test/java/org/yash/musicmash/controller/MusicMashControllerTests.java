/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yash.musicmash.controller;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.yash.musicmash.config.AppConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class MusicMashControllerTests {

  @Autowired
  AppConfiguration appConfiguration;
  @MockBean
  RestTemplate restTemplate;

  private JsonNode mockMusicBrainzResponse;
  private JsonNode mockCoverArtResponse;
  private JsonNode mockWikiDataResponse;
  private JsonNode mockWikiPediaResponse;

  @Autowired
  private MockMvc mockMvc;
  @Mock
  private ResponseEntity<String> mockResponseEntity;


  private void setUp() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mockMusicBrainzResponse = mapper.readTree(
        new File("src/test/resources/musicbrainz-response.json"));

    mockWikiDataResponse = mapper.readTree(
        new File("src/test/resources/wikidata-response.json"));

    mockWikiPediaResponse = mapper.readTree(
        new File("src/test/resources/wikipedia-response.json"));

    mockCoverArtResponse = mapper.readTree(
        new File("src/test/resources/coverart-response.json"));

    when(mockResponseEntity.getBody()).thenReturn(mockMusicBrainzResponse.toString())
        .thenReturn(mockWikiDataResponse.toString()).thenReturn(mockWikiPediaResponse.toString())
        .thenReturn(mockCoverArtResponse.toString());
  }

  @Test
  public void validMusicMash() throws Exception {
    // The Setup is only required for this Test, else Before annonation can also be used.
    setUp();
    when(restTemplate.exchange(
        "http://www.musicbrainz-dummy.org/ws/2/artist/5b11f4ce-a62d-471e-81fc-a69a8278c7da?fmt=json&inc=url-rels+release-groups",
        HttpMethod.GET,
        createMockEntity(), String.class)).thenReturn(
        mockResponseEntity);
    when(restTemplate.exchange(
        "http://www.wiki-dummy.org/w/api.php?format=json&ids=Q11649&action=wbgetentities&props=sitelinks",
        HttpMethod.GET, createMockEntity(), String.class)).thenReturn(
        mockResponseEntity);
    when(restTemplate.exchange(
        "http://www.wikipedia-dummy.org/w/api.php?format=json&titles=Nirvana (band)&action=query&prop=extracts&exintro=true",
        HttpMethod.GET, createMockEntity(), String.class)).thenReturn(
        mockResponseEntity);

    when(restTemplate.exchange(
        contains("http://www.coverart-dummy.org/release-group"),
        eq(HttpMethod.GET), eq(createMockEntity()), eq(String.class))).thenReturn(
        mockResponseEntity);

    this.mockMvc.perform(get("/musicmash/5b11f4ce-a62d-471e-81fc-a69a8278c7da")).andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mbid").value("5b11f4ce-a62d-471e-81fc-a69a8278c7da"));
  }

  private HttpEntity<String> createMockEntity() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    HttpEntity<String> entity = new HttpEntity<>(headers);
    return entity;
  }


  @Test
  public void errorMessage() throws Exception {

    when(restTemplate.exchange(
        "http://www.musicbrainz-dummy.org/ws/2/artist/xxxx?fmt=json&inc=url-rels+release-groups",
        HttpMethod.GET,
        createMockEntity(), String.class)).thenThrow(
        new HttpServerErrorException(HttpStatus.BAD_REQUEST));

    this.mockMvc.perform(get("/musicmash/xxxx"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorMessage").value("NOT A VALID MBID."));
  }

}


