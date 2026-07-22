package com.patlatarlagna.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String token;
    private String refreshToken;
    private Long id;
    private String email;
    private List<String> roles;
    private Boolean hasProfile;

    // Keep boolean-style accessor for tests and older callers that expect isHasProfile()
    public boolean isHasProfile() {
        return Boolean.TRUE.equals(this.hasProfile);
    }
}
