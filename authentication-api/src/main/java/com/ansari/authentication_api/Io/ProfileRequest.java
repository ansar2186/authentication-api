package com.ansari.authentication_api.Io;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for creating a new user profile") //this is for swagger doc
public class ProfileRequest {
    @NotBlank(message = "{profile.name.notBlank}")
    @Size(min = 5, max = 15, message = "{profile.name.size}")
    @Schema(description = "Name of the user", example = "Ansar Ansari", minLength = 5, maxLength = 15)
    private String name;
    @NotBlank(message = "{profile.email.notBlank}")
    @Email(message = "{profile.email.invalid}")
    @Schema(description = "Email address of the user", example = "humza@example.com", format = "email")
    private String email;
    @NotBlank(message = "{profile.password.notBlank}")
    @Schema(description = "Password for the user account", example = "Pass@1234", format = "password")
    private String password;
}
