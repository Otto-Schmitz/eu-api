package eu.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicationRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 128)
    private String dosage;

    @Size(max = 128)
    private String frequency;

    @Size(max = 2048)
    private String notes;

    private Boolean active;
    private String startedAt;
    private String stoppedAt;
}
