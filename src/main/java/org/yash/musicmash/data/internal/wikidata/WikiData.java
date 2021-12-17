package org.yash.musicmash.data.internal.wikidata;

import java.util.Map;
import lombok.Data;

@Data
public class WikiData {

  private String success;
  private Map<String, Entity> entities;


}



