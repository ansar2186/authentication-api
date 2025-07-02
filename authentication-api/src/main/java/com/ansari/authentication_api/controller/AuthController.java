package com.ansari.authentication_api.controller;

import com.ansari.authentication_api.Io.AuthRequest;
import com.ansari.authentication_api.Io.AuthResponse;
import com.ansari.authentication_api.service.AppUserDetailsService;
import com.ansari.authentication_api.service.ProfileService;
import com.ansari.authentication_api.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ProfileService service;

    /**
     * Handles user login by authenticating the provided email and password.
     * <p>
     * If authentication is successful, generates a JWT token, sets it in an HTTP-only cookie,
     * and returns an {@link AuthResponse} containing the user's email and token.
     * <p>
     * In case of failure, returns an appropriate error response with status code:
     * <ul>
     *     <li><b>400 Bad Request</b>: if the credentials are invalid</li>
     *     <li><b>401 Unauthorized</b>: if the account is disabled or authentication fails</li>
     * </ul>
     *
     * @param request the login request containing email and password
     * @return a {@link ResponseEntity} containing the authentication result or error details
     */
    @Operation(
            summary = "Login user",
            description = "Authenticate user using email and password, return JWT token in cookie and response body."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials",
                    content = @Content(schema = @Schema(example = "{\"Error\": true, \"Message\": \"Invalid Email or Password\"}"))),
            @ApiResponse(responseCode = "401", description = "Account disabled or authentication failed",
                    content = @Content(schema = @Schema(example = "{\"Error\": true, \"Message\": \"Account is disabled\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authentication(request.getEmail(), request.getPassword());
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            String jwtToken = jwtUtil.generateToken(userDetails);
            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .secure(false) // ✅ allow over HTTP
                    .path("/")
                    .maxAge(3600)
                    .sameSite("Strict")
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(request.getEmail(), jwtToken));
        } catch (BadCredentialsException exception) {
            Map<String, Object> error = new HashMap<>();
            error.put("Error", true);
            error.put("Message", "Invalid Email or Password , Please try with valid email or Password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (DisabledException exception) {
            Map<String, Object> error = new HashMap<>();
            error.put("Error", true);
            error.put("Message", "Account is disabled");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception exception) {
            Map<String, Object> error = new HashMap<>();
            error.put("Error", true);
            error.put("Message", "Authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    private void authentication(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    /**
     * Checks if the current user is authenticated.
     *
     * @return {@code true} if the user is authenticated, otherwise {@code false}
     */
    @Operation(
            summary = "Check Authentication Status",
            description = "Returns true if a user is currently authenticated, false otherwise."
    )
    @ApiResponse(responseCode = "200", description = "Authenticated status")
    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(auth.getName() != null);
    }
}
