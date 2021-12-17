package org.yash.musicmash.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SourceRequestRateTooLarge extends MusicMashServiceException {

  private static final long serialVersionUID = 3505090292307420454L;

  public SourceRequestRateTooLarge(String message, Throwable cause) {
    super(message, cause);
  }
}
