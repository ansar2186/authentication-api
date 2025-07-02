package com.ansari.authentication_api.Io;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request for user authentication (login)") // this is swagger doc
public class AuthRequest {
    @Schema(description = "User's email address", example = "user@example.com", required = true)
    private String email;
    @Schema(description = "User's password", example = "StrongPassword@123", required = true)
    private String password;
}
