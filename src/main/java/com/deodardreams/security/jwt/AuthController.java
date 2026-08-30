package com.deodardreams.security.jwt;

import com.deodardreams.dto.request.AdminLoginRequestDto;
import com.deodardreams.dto.response.LoginResponseDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody AdminLoginRequestDto requestDto) {

        log.info("Login attempt for email={}", requestDto.getEmail());

        // Delegates the actual "is this password correct" check to Spring Security's
        // existing machinery — the same DaoAuthenticationProvider/CustomUserDetailsService
        // /PasswordEncoder chain you already built. Throws an exception automatically
        // if the credentials are wrong or the account is disabled.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword())
        );

        // Extracts the role Spring Security resolved during authentication,
        // so the token carries the SAME role information your app already trusts.
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                // Spring Security's authorities are internally prefixed with "ROLE_" (e.g. "ROLE_SUPER_ADMIN"),
                // but our JwtService/JwtAuthenticationFilter add that same prefix back themselves when
                // reconstructing authorities from the token — so we strip it here to store the clean role
                // name ("SUPER_ADMIN") and avoid ending up with a broken double prefix ("ROLE_ROLE_SUPER_ADMIN").
                .map(auth -> auth.replace("ROLE_", ""))
                .orElseThrow();

        String token = jwtService.generateToken(requestDto.getEmail(), role);

        log.info("Login successful for email={}", requestDto.getEmail());

        return ResponseEntity.ok(new LoginResponseDto(token, "Bearer"));
    }
}
