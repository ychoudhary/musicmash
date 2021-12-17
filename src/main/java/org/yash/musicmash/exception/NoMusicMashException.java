package org.yash.musicmash.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class NoMusicMashException extends MusicMashServiceException {

  private static final long serialVersionUID = 191744242539354578L;
  private final String id;

  public NoMusicMashException(String id, String errorMessage) {
    super(errorMessage);
    this.id = id;
  }
}

