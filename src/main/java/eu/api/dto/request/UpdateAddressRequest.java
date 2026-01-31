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
public class UpdateAddressRequest {

    @Pattern(regexp = "^(HOME|WORK|OTHER)?$", message = "Label must be HOME, WORK or OTHER")
    private String label;

    private Boolean isPrimary;

    @Size(max = 512)
    private String street;

    @Size(max = 64)
    private String number;

    @Size(max = 128)
    private String city;

    @Size(max = 128)
    private String state;

    @Size(max = 64)
    private String zip;

    @Size(max = 128)
    private String country;
}
