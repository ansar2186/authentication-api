package com.ansari.authentication_api.controller;

import com.ansari.authentication_api.Io.ProfileRequest;
import com.ansari.authentication_api.Io.ProfileResponse;
import com.ansari.authentication_api.Io.ResetPasswordRequest;
import com.ansari.authentication_api.exception.InvalidOtpException;
import com.ansari.authentication_api.exception.OtpExpiredException;
import com.ansari.authentication_api.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService service;

    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> createProfile(@Valid @RequestBody ProfileRequest request) {
        return new ResponseEntity<>(service.createProfile(request), HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return new ResponseEntity<>(service.getProfile(auth.getName()), HttpStatus.OK);
    }

    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam String email) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            service.sendResetOtp(email);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());

        }
    }

    @PostMapping("/rest-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            service.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        } catch (Exception | InvalidOtpException | OtpExpiredException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/send-otp")
    public void sendVerifyOtp() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            service.sendOtp(auth.getName());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public void verifyEmail(@RequestBody Map<String,Object> request){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(request.get("otp").toString()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Missing  OTP details");
        }
        try {
            service.verifyOtp(auth.getName(),request.get("otp").toString());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (InvalidOtpException | OtpExpiredException e) {
            throw new RuntimeException(e);
        }
    }
}
