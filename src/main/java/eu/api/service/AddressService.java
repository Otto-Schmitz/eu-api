package eu.api.service;

import eu.api.dto.request.CreateAddressRequest;
import eu.api.dto.request.UpdateAddressRequest;
import eu.api.dto.response.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    List<AddressResponse> list(UUID userId);

    AddressResponse create(UUID userId, CreateAddressRequest request);

    AddressResponse update(UUID userId, UUID addressId, UpdateAddressRequest request);

    void delete(UUID userId, UUID addressId);
}
