package eu.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthInfoResponse {

    private String bloodType;
    private Integer allergyCount;
    private Integer medicationCount;
    private String medicalNotes;
}
