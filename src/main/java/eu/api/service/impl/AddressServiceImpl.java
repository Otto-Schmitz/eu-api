package eu.api.service.impl;

import eu.api.dto.request.CreateAddressRequest;
import eu.api.dto.request.UpdateAddressRequest;
import eu.api.dto.response.AddressResponse;
import eu.api.entity.AddressEntity;
import eu.api.exception.ForbiddenException;
import eu.api.exception.NotFoundException;
import eu.api.crypto.CryptoService;
import eu.api.repository.AddressRepository;
import eu.api.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final CryptoService cryptoService;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> list(UUID userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse create(UUID userId, CreateAddressRequest request) {
        boolean primary = Boolean.TRUE.equals(request.getIsPrimary());
        if (primary) {
            clearOtherPrimaries(userId);
        }
        AddressEntity entity = AddressEntity.builder()
                .userId(userId)
                .label(request.getLabel().trim().toUpperCase())
                .isPrimary(primary)
                .street(encryptOrNull(request.getStreet()))
                .number(encryptOrNull(request.getNumber()))
                .city(trimOrNull(request.getCity()))
                .state(trimOrNull(request.getState()))
                .zip(encryptOrNull(request.getZip()))
                .country(trimOrNull(request.getCountry()))
                .build();
        entity = addressRepository.save(entity);
        return toResponse(entity);
    }

    @Override
    @Transactional
    public AddressResponse update(UUID userId, UUID addressId, UpdateAddressRequest request) {
        AddressEntity entity = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new ForbiddenException("Not your address");
        }
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            clearOtherPrimaries(userId);
            entity.setIsPrimary(true);
        } else if (request.getIsPrimary() != null && !request.getIsPrimary()) {
            entity.setIsPrimary(false);
        }
        if (request.getLabel() != null && !request.getLabel().isBlank()) {
            entity.setLabel(request.getLabel().trim().toUpperCase());
        }
        if (request.getStreet() != null) {
            entity.setStreet(encryptOrNull(request.getStreet()));
        }
        if (request.getNumber() != null) {
            entity.setNumber(encryptOrNull(request.getNumber()));
        }
        if (request.getCity() != null) {
            entity.setCity(trimOrNull(request.getCity()));
        }
        if (request.getState() != null) {
            entity.setState(trimOrNull(request.getState()));
        }
        if (request.getZip() != null) {
            entity.setZip(encryptOrNull(request.getZip()));
        }
        if (request.getCountry() != null) {
            entity.setCountry(trimOrNull(request.getCountry()));
        }
        entity = addressRepository.save(entity);
        return toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID addressId) {
        AddressEntity entity = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new ForbiddenException("Not your address");
        }
        entity.setDeletedAt(Instant.now());
        addressRepository.save(entity);
    }

    private void clearOtherPrimaries(UUID userId) {
        addressRepository.findByUserId(userId).stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsPrimary()))
                .forEach(a -> {
                    a.setIsPrimary(false);
                    addressRepository.save(a);
                });
    }

    private AddressResponse toResponse(AddressEntity entity) {
        return AddressResponse.builder()
                .id(entity.getId())
                .label(entity.getLabel())
                .isPrimary(Boolean.TRUE.equals(entity.getIsPrimary()))
                .street(decryptOrNull(entity.getStreet()))
                .number(decryptOrNull(entity.getNumber()))
                .city(entity.getCity())
                .state(entity.getState())
                .zip(decryptOrNull(entity.getZip()))
                .country(entity.getCountry())
                .build();
    }

    private String encryptOrNull(String plain) {
        if (plain == null || plain.trim().isEmpty()) {
            return null;
        }
        return cryptoService.encrypt(plain.trim());
    }

    private String decryptOrNull(String encrypted) {
        return Optional.ofNullable(encrypted).filter(s -> !s.isBlank())
                .map(cryptoService::decrypt).orElse(null);
    }

    private String trimOrNull(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        return s.trim();
    }
}
