package com.ansari.authentication_api.service.impl;

import com.ansari.authentication_api.Io.ProfileRequest;
import com.ansari.authentication_api.Io.ProfileResponse;
import com.ansari.authentication_api.entity.UserEntity;
import com.ansari.authentication_api.exception.InvalidOtpException;
import com.ansari.authentication_api.exception.OtpExpiredException;
import com.ansari.authentication_api.exception.UserAlreadyExist;
import com.ansari.authentication_api.exception.UserNotFoundException;
import com.ansari.authentication_api.repository.UserRepository;
import com.ansari.authentication_api.service.EmailService;
import com.ansari.authentication_api.service.ProfileService;
import com.ansari.authentication_api.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final String SUBJECT = "Welcome Email";
    private final static String RESET_OTP_EMAIL = "Password reset otp email";
    private final static String ACCOUNT_VERIFY_OTP = "Account Verification OTP";

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

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + email));
        //generate 6 digits otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        //calculate expire time (current time +15 minutes in milliseconds)
        long expireTime = System.currentTimeMillis() + (15 * 60 * 100);
        existingEntity.setResetOtp(otp);
        existingEntity.setRestOtpExpireAt(expireTime);
        userRepository.save(existingEntity);

        try {
            String text = ("Hello " + existingEntity.getName() + ",\n\nYour otp for rest password is: " + otp + " user this otp to proceed with  resetting your password !\n\nRegards,\nAuthorization Team");
            emailService.sendEmailToUser(existingEntity.getEmail(), RESET_OTP_EMAIL, text);

        } catch (Exception exception) {
            throw new RuntimeException("Unable to send rest otp email");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) throws InvalidOtpException, OtpExpiredException {

        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with " + email));
        if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)) {
            throw new InvalidOtpException("Invalid OTP");
        }
        if (existingUser.getRestOtpExpireAt() < System.currentTimeMillis()) {
            throw new OtpExpiredException("OTP has been Expired");
        }
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setRestOtpExpireAt(0L);
        userRepository.save(existingUser);
    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + email));
        if (existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified()) {
            return;
        }

        //generate 6 digits otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        //calculate expire time (current time +24 hours minutes in milliseconds)
        long expireTime = System.currentTimeMillis() + (24 * 60 * 60 * 100);
        //update
        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpiredAt(expireTime);

        userRepository.save(existingUser);
        try {
            String text = ("Hello " + existingUser.getName() + ",\n\nYour otp is: " + otp + " Verify your account using this OTP !\n\nRegards,\nAuthorization Team");
            emailService.sendEmailToUser(existingUser.getEmail(), ACCOUNT_VERIFY_OTP, text);

        } catch (Exception exception) {
            throw new RuntimeException("Unable to send email");
        }
    }

    @Override
    public void verifyOtp(String email, String otp) throws InvalidOtpException, OtpExpiredException {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + email));
        if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)) {
            throw new InvalidOtpException("Invalid OTP");
        }
        if (existingUser.getVerifyOtpExpiredAt() < System.currentTimeMillis()) {
            throw new OtpExpiredException("OTP has been expired");
        }

        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpiredAt(0L);

        userRepository.save(existingUser);
    }
}
