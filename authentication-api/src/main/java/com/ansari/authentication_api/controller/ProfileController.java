package com.ansari.authentication_api.controller;

import com.ansari.authentication_api.Io.ProfileRequest;
import com.ansari.authentication_api.Io.ProfileResponse;
import com.ansari.authentication_api.Io.ResetPasswordRequest;
import com.ansari.authentication_api.exception.InvalidOtpException;
import com.ansari.authentication_api.exception.OtpExpiredException;
import com.ansari.authentication_api.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Profile", description = "APIs for user profile")
public class ProfileController {

    private final ProfileService service;

    @Operation(summary = "Register a new profile")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> createProfile(@Valid @RequestBody ProfileRequest request) {
        return new ResponseEntity<>(service.createProfile(request), HttpStatus.CREATED);
    }
    @Operation(summary = "Get the authenticated user's profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return new ResponseEntity<>(service.getProfile(auth.getName()), HttpStatus.OK);
    }
    @Operation(summary = "Send OTP for password reset")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP sent"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam String email) {
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            service.sendResetOtp(email);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());

        }
    }
    @Operation(summary = "Reset password using OTP")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "500", description = "Invalid or expired OTP")
    })
    @PostMapping("/rest-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            service.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        } catch (Exception | InvalidOtpException | OtpExpiredException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    @Operation(summary = "Send OTP for email verification")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/send-otp")
    public void sendVerifyOtp() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            service.sendOtp(auth.getName());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    @Operation(summary = "Verify user's email using OTP")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid OTP"),
            @ApiResponse(responseCode = "500", description = "OTP expired or internal error")
    })

    @PostMapping("/verify-otp")
    public void verifyEmail(@RequestBody Map<String, Object> request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (request.get("otp").toString() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing  OTP details");
        }
        try {
            service.verifyOtp(auth.getName(), request.get("otp").toString());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (InvalidOtpException | OtpExpiredException e) {
            throw new RuntimeException(e);
        }
    }
}
