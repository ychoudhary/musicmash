package org.yash.musicmash.data.internal.musicbrainz;


import lombok.Data;

@Data
public class Relation {

  private String type;
  private Url url;

}
