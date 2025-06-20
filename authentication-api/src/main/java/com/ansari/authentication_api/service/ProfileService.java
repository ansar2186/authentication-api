package com.ansari.authentication_api.service;

import com.ansari.authentication_api.Io.ProfileRequest;
import com.ansari.authentication_api.Io.ProfileResponse;

public interface ProfileService {
    public ProfileResponse createProfile(ProfileRequest request);

    public ProfileResponse getProfile(String email);
}
