package org.yash.musicmash.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Album {

  private String title;
  private String id;
  private String image;
}
