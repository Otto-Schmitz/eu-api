package eu.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmergencyContactRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 128)
    private String relationship;

    @Size(max = 64)
    private String phone;

    private Integer priority;
}
