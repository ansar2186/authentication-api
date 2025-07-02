package com.ansari.authentication_api.Io;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to reset the user's password using an OTP") // this is swagger doc
public class ResetPasswordRequest {
    @NotBlank(message = " New Password is required")
    @Schema(description = "New password to be set for the user", example = "StrongPassword@123")
    private String newPassword;
    @NotBlank(message = "Otp is required")
    @Schema(description = "OTP sent to the user's email for verification", example = "123456")
    private String otp;
    @NotBlank(message = "email is required")
    @Schema(description = "Email address of the user to reset password for", example = "user@example.com")
    private String email;

}
