package org.yash.musicmash.data;

import java.util.List;
import lombok.Data;

@Data
public class MusicMash {

  private String mbid;
  private String description;
  private List<Album> albums;

}
