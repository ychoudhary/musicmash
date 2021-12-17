package org.yash.musicmash.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SlowResponseException extends SourceErrorException {

  private static final long serialVersionUID = 4711937276945859463L;

  public SlowResponseException(String message, Throwable cause, String url) {
    super(message, cause, url);
  }
}
