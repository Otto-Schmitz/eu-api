package eu.api.service;

import eu.api.dto.request.CreateAllergyRequest;
import eu.api.dto.request.UpdateAllergyRequest;
import eu.api.dto.response.AllergyListItemResponse;

import java.util.List;
import java.util.UUID;

public interface AllergyService {

    List<AllergyListItemResponse> list(UUID userId, boolean includeNotes);

    AllergyListItemResponse create(UUID userId, CreateAllergyRequest request);

    AllergyListItemResponse update(UUID userId, UUID allergyId, UpdateAllergyRequest request);

    void delete(UUID userId, UUID allergyId);
}
