package com.ansari.authentication_api.service.impl;

import com.ansari.authentication_api.Io.ProfileRequest;
import com.ansari.authentication_api.Io.ProfileResponse;
import com.ansari.authentication_api.entity.UserEntity;
import com.ansari.authentication_api.exception.*;
import com.ansari.authentication_api.repository.UserRepository;
import com.ansari.authentication_api.service.EmailService;
import com.ansari.authentication_api.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProfileServiceImplTest {

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private UserEntity testUser;
    private ProfileRequest profileRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profileRequest = new ProfileRequest("Test", "test@example.com", "password");
        testUser = new UserEntity();
        testUser.setName("Test");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded-password");
    }

    @Test
    void createProfile_success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any())).thenReturn(testUser);

        ProfileResponse response = profileService.createProfile(profileRequest);

        assertEquals("test@example.com", response.getEmail());
        verify(emailService).sendEmailToUser(eq("test@example.com"), anyString(), contains("Thanks for registering"));
    }

    @Test
    void createProfile_userAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        assertThrows(UserAlreadyExist.class, () -> profileService.createProfile(profileRequest));
    }

    @Test
    void getProfile_success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        ProfileResponse response = profileService.getProfile("test@example.com");
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void getProfile_userNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> profileService.getProfile("test@example.com"));
    }

    @Test
    void sendResetOtp_success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        profileService.sendResetOtp("test@example.com");
        verify(userRepository).save(any());
        verify(emailService).sendEmailToUser(eq("test@example.com"), eq("Password reset otp email"), contains("Your otp for rest password is"));
    }

    @Test
    void sendResetOtp_userNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> profileService.sendResetOtp("unknown@example.com"));
    }

    @Test
    void resetPassword_success() {
        testUser.setResetOtp("123456");
        testUser.setRestOtpExpireAt(System.currentTimeMillis() + 100000);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("new-password");

        assertDoesNotThrow(() -> profileService.resetPassword("test@example.com", "123456", "newPassword"));
    }

    @Test
    void resetPassword_invalidOtp() {
        testUser.setResetOtp("654321");
        testUser.setRestOtpExpireAt(System.currentTimeMillis() + 100000);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(InvalidOtpException.class, () -> profileService.resetPassword("test@example.com", "000000", "newPassword"));
    }

    @Test
    void resetPassword_expiredOtp() {
        testUser.setResetOtp("123456");
        testUser.setRestOtpExpireAt(System.currentTimeMillis() - 1000);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(OtpExpiredException.class, () -> profileService.resetPassword("test@example.com", "123456", "newPassword"));
    }

    @Test
    void sendOtp_success() {
        testUser.setIsAccountVerified(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        profileService.sendOtp("test@example.com");
        verify(userRepository).save(any());
        verify(emailService).sendEmailToUser(eq("test@example.com"), eq("Account Verification OTP"), contains("Your otp is"));
    }

    @Test
    void sendOtp_alreadyVerified() {
        testUser.setIsAccountVerified(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        profileService.sendOtp("test@example.com");
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendEmailToUser(any(), any(), any());
    }

    @Test
    void verifyOtp_success() {
        testUser.setVerifyOtp("111111");
        testUser.setVerifyOtpExpiredAt(System.currentTimeMillis() + 10000);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        assertDoesNotThrow(() -> profileService.verifyOtp("test@example.com", "111111"));
        verify(userRepository).save(any());
    }

    @Test
    void verifyOtp_invalid() {
        testUser.setVerifyOtp("999999");
        testUser.setVerifyOtpExpiredAt(System.currentTimeMillis() + 10000);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        assertThrows(InvalidOtpException.class, () -> profileService.verifyOtp("test@example.com", "123456"));
    }

    @Test
    void verifyOtp_expired() {
        testUser.setVerifyOtp("123456");
        testUser.setVerifyOtpExpiredAt(System.currentTimeMillis() - 1);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        assertThrows(OtpExpiredException.class, () -> profileService.verifyOtp("test@example.com", "123456"));
    }
}
