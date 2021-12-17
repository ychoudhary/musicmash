package org.yash.musicmash.data.internal.musicbrainz;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReleaseGroup {

  private String id;
  private String title;
  
  @JsonProperty("primary-type")
  private String primaryType;

}
