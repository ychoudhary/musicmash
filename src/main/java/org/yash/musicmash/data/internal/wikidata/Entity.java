package org.yash.musicmash.data.internal.wikidata;

import java.util.Map;
import lombok.Data;

@Data
public class Entity {

  private String id;
  private String type;
  private Map<String, SiteLink> sitelinks;


}



