package org.yash.musicmash.data.internal.musicbrainz;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class MusicBrainz {

  private String id;
  private List<Relation> relations;

  @JsonProperty("release-groups")
  private List<ReleaseGroup> releaseGroups;


}



