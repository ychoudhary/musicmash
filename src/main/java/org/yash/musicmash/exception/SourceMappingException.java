package org.yash.musicmash.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SourceMappingException extends MusicMashServiceException {

  private static final long serialVersionUID = 1931629477067563630L;

  public SourceMappingException(String message, Throwable cause, String responseBody) {
    super(message, cause, responseBody);
  }
}
