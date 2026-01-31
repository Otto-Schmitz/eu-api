package eu.api.service;

import eu.api.dto.request.CreateEmergencyContactRequest;
import eu.api.dto.request.UpdateEmergencyContactRequest;
import eu.api.dto.response.EmergencyContactResponse;

import java.util.List;
import java.util.UUID;

public interface EmergencyContactService {

    List<EmergencyContactResponse> list(UUID userId);

    EmergencyContactResponse create(UUID userId, CreateEmergencyContactRequest request);

    EmergencyContactResponse update(UUID userId, UUID contactId, UpdateEmergencyContactRequest request);

    void delete(UUID userId, UUID contactId);
}
