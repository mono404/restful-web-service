package com.mono.restfulwebservice.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// HTTP Status Code
// 2XX -> OK
// 4XX -> Client Error
// 5XX -> Server Error
@ResponseStatus(HttpStatus.NOT_FOUND) //4XX ->not found
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {

        super(message);

    }
}
