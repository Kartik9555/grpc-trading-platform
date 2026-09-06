package com.learning.user.service.advice;

import com.learning.user.exceptions.InsufficientBalanceException;
import com.learning.user.exceptions.InsufficientSharesException;
import com.learning.user.exceptions.UnknownTickerException;
import com.learning.user.exceptions.UnknownUserException;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class ServiceExceptionHandler {

    @GrpcExceptionHandler(value = UnknownTickerException.class)
    public Status handleInvalidArguments(UnknownTickerException exception) {
        return Status.INVALID_ARGUMENT
                .withDescription(exception.getMessage())
                .withCause(exception);
    }


    @GrpcExceptionHandler(value = UnknownUserException.class)
    public Status handleUnknownUser(UnknownUserException exception) {
        return Status.NOT_FOUND
                .withDescription(exception.getMessage())
                .withCause(exception);
    }

    @GrpcExceptionHandler(value = {InsufficientBalanceException.class, InsufficientSharesException.class})
    public Status handlePreconditionFailures(Exception exception) {
        return Status.FAILED_PRECONDITION
                .withDescription(exception.getMessage())
                .withCause(exception);
    }


}
