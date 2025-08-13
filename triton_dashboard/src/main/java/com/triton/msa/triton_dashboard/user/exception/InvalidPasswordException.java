package com.triton.msa.triton_dashboard.user.exception;

import lombok.Getter;

@Getter
public class InvalidPasswordException extends RuntimeException {
  public InvalidPasswordException(String message) {
      super(message);
  }
}
