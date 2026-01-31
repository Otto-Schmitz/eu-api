package eu.api.controller;

import eu.api.dto.request.CreateAddressRequest;
import eu.api.dto.request.UpdateAddressRequest;
import eu.api.dto.response.AddressResponse;
import eu.api.security.CurrentUser;
import eu.api.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> list() {
        UUID userId = CurrentUser.getUserIdOrThrow();
        List<AddressResponse> response = addressService.list(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody CreateAddressRequest request) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        AddressResponse response = addressService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAddressRequest request) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        AddressResponse response = addressService.update(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID userId = CurrentUser.getUserIdOrThrow();
        addressService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
