package com.ansari.authentication_api.Io;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
