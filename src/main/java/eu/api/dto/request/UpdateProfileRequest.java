package eu.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 255)
    private String fullName;

    private LocalDate birthDate;

    @Size(max = 64)
    private String phone;

    @Size(max = 255)
    private String workplace;
}
