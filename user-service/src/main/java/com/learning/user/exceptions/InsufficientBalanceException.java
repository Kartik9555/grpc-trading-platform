package com.learning.user.exceptions;

public class InsufficientBalanceException extends RuntimeException {

  private static final String MESSAGE = "User [id=%d] does not have enough fund to complete the transaction";

    public InsufficientBalanceException(Long userId) {
        super(MESSAGE.formatted(userId));
    }
}
