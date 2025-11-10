package io.github.jaredmcc4.gtm.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String jwtToken;
    private String type = "Bearer";
    private Long expiresIn;
    private String refreshToken;
}
