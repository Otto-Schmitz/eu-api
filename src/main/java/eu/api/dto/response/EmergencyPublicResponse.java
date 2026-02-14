package eu.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EmergencyPublicResponse {
    private final String name;
    private final String bloodType;
    private final String phone;
    private final List<EmergencyAllergyItem> allergies;
    private final List<EmergencyContactItem> emergencyContacts;
    private final List<EmergencyMedicationItem> medications;

    @Getter
    @Builder
    public static class EmergencyAllergyItem {
        private final String name;
        private final String severity;
    }

    @Getter
    @Builder
    public static class EmergencyContactItem {
        private final String name;
        private final String relationship;
        private final String phone;
        private final int priority;
    }

    @Getter
    @Builder
    public static class EmergencyMedicationItem {
        private final String name;
        private final String dosage;
        private final String frequency;
    }
}
