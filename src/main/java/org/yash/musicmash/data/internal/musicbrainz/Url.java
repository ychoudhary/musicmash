package org.yash.musicmash.data.internal.musicbrainz;

import java.net.URL;
import lombok.Data;
import lombok.SneakyThrows;

@Data
public class Url {

  private String id;
  private String resource;

  @SneakyThrows
  public String getQID() {
    URL url = new URL(resource);
    var qId = url.getPath().split("/");
    return qId[qId.length - 1];
  }
}
