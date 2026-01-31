package eu.api.service;

import eu.api.dto.request.CreateMedicationRequest;
import eu.api.dto.request.UpdateMedicationRequest;
import eu.api.dto.response.MedicationListItemResponse;

import java.util.List;
import java.util.UUID;

public interface MedicationService {

    List<MedicationListItemResponse> list(UUID userId, boolean includeNotes);

    MedicationListItemResponse create(UUID userId, CreateMedicationRequest request);

    MedicationListItemResponse update(UUID userId, UUID medicationId, UpdateMedicationRequest request);

    void delete(UUID userId, UUID medicationId);
}
