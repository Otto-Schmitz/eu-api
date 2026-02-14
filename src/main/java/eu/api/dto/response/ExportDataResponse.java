package eu.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExportDataResponse {
    private final ProfileResponse profile;
    private final HealthInfoResponse health;
    private final List<AllergyListItemResponse> allergies;
    private final List<MedicationListItemResponse> medications;
    private final List<EmergencyContactResponse> emergencyContacts;
    private final List<AddressResponse> addresses;
    private final String exportedAt;
}
