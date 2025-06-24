package com.ansari.authentication_api.service;

import com.ansari.authentication_api.Io.ProfileRequest;
import com.ansari.authentication_api.Io.ProfileResponse;
import com.ansari.authentication_api.exception.InvalidOtpException;
import com.ansari.authentication_api.exception.OtpExpiredException;

public interface ProfileService {
    public ProfileResponse createProfile(ProfileRequest request);

    public ProfileResponse getProfile(String email);

    public void sendResetOtp(String email);

    public void resetPassword(String email, String otp, String newPassword) throws InvalidOtpException, OtpExpiredException;

    public void sendOtp(String email);

    public void verifyOtp(String email, String otp) throws InvalidOtpException, OtpExpiredException;

}
