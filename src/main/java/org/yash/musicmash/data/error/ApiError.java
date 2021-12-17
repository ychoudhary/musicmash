package org.yash.musicmash.data.error;

import lombok.Data;

@Data
public class ApiError {

  private String errorMessage;
  private String details;

  public ApiError(String message, String details) {
    super();
    this.errorMessage = message;
    this.details = details;
  }

  public ApiError(String message) {
    super();
    this.errorMessage = message;
  }
}
