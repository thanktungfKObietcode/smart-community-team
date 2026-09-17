package vn.edu.crs.smartcommunity.identity.internal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.edu.crs.smartcommunity.common.error.ApiErrorResponse;
import vn.edu.crs.smartcommunity.identity.internal.service.IdentityAuthenticationService.InvalidCredentialsException;

@RestControllerAdvice
public class IdentityExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> invalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> inactiveAccount(DisabledException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse("Account is inactive"));
    }
}
