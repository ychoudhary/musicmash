package org.yash.musicmash.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;


@Data
@EqualsAndHashCode(callSuper = true)
public class SourceErrorException extends MusicMashServiceException {

  private static final long serialVersionUID = 2114695523066634360L;
  private final HttpStatus statusCode;
  private final String url;

  public SourceErrorException(String message, Throwable cause, HttpStatus statusCode, String url,
      String additionalInformation) {
    super(message, cause, additionalInformation);
    this.statusCode = statusCode;
    this.url = url;
  }

  public SourceErrorException(String message, Throwable cause, String url) {
    super(message, cause);
    this.url = url;
    this.statusCode = null;
  }
}
