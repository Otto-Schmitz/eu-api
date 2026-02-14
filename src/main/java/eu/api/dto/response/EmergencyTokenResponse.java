package eu.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmergencyTokenResponse {
    private final String token;
    private final boolean active;
}
