package org.yash.musicmash.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class MusicMashServiceException extends Exception {


  private static final long serialVersionUID = -4732646724649456325L;
  private final String additionalInformation;

  protected MusicMashServiceException(
      String message, Throwable cause, String additionalInformation) {
    super(message, cause);
    this.additionalInformation = additionalInformation;
  }

  protected MusicMashServiceException(String message, Throwable cause) {
    super(message, cause);
    this.additionalInformation = null;
  }

  protected MusicMashServiceException(String message) {
    super(message);
    this.additionalInformation = null;
  }
}
