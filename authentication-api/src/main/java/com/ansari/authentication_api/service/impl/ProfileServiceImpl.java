package com.ansari.authentication_api.service.impl;

import com.ansari.authentication_api.Io.ProfileRequest;
import com.ansari.authentication_api.Io.ProfileResponse;
import com.ansari.authentication_api.entity.UserEntity;
import com.ansari.authentication_api.exception.UserAlreadyExist;
import com.ansari.authentication_api.exception.UserNotFoundException;
import com.ansari.authentication_api.repository.UserRepository;
import com.ansari.authentication_api.service.EmailService;
import com.ansari.authentication_api.service.ProfileService;
import com.ansari.authentication_api.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final String SUBJECT = "Welcome Email";

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {

        UserEntity userEntity = UserUtil.convertedToUserEntity(request);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExist("User already exits with email " + request.getEmail() + " Please try with new Email ");
        }
        UserEntity saveUser = userRepository.save(userEntity);

        String text = ("Hello " + saveUser.getName() + ",\n\nThanks for registering with us !\n\nRegards,\nAuthorization Team");
        emailService.sendEmailToUser(saveUser.getEmail(), SUBJECT, text);

        return UserUtil.convertToResponse(saveUser);
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return UserUtil.convertToResponse(userEntity);
    }
}
