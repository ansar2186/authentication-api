package com.ansari.authentication_api.util;

import com.ansari.authentication_api.Io.ProfileRequest;
import com.ansari.authentication_api.Io.ProfileResponse;
import com.ansari.authentication_api.entity.UserEntity;
import lombok.Builder;

import java.util.UUID;

public class UserUtil {

    public static UserEntity convertedToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .email(request.getEmail())
                .name(request.getName())
                .password(request.getPassword())
                .isAccountVerified(false)
                .restOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpiredAt(0L)
                .resetOtp(null)
                .build();
    }

    public static ProfileResponse convertToResponse(UserEntity user) {
        return ProfileResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .isAccountVerified(user.getIsAccountVerified())
                .build();
    }
}
