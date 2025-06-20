package com.ansari.authentication_api.controller;

import com.ansari.authentication_api.Io.ProfileRequest;
import com.ansari.authentication_api.Io.ProfileResponse;
import com.ansari.authentication_api.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
}
