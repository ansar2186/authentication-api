package com.ansari.authentication_api.Io;

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
public class ProfileRequest {
    @NotBlank(message = "{profile.name.notBlank}")
    @Size(min = 5, max = 15, message = "{profile.name.size}")
    private String name;
    @NotBlank(message = "{profile.email.notBlank}")
    @Email(message = "{profile.email.invalid}")
    private String email;
    @NotBlank(message = "{profile.password.notBlank}")
    private String password;
}
