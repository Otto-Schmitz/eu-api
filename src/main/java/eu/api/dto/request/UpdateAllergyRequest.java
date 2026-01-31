package eu.api.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAllergyRequest {

    @Size(max = 255)
    private String name;

    @Pattern(regexp = "^(LOW|MEDIUM|HIGH)?$", message = "Severity must be LOW, MEDIUM or HIGH")
    private String severity;

    @Size(max = 2048)
    private String notes;
}
