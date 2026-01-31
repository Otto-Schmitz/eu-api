package eu.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private UUID id;
    private String label;
    private Boolean isPrimary;
    private String street;
    private String number;
    private String city;
    private String state;
    private String zip;
    private String country;
}
