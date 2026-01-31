package eu.api.service;

import eu.api.dto.request.UpdateProfileRequest;
import eu.api.dto.response.ProfileResponse;
import eu.api.entity.UserProfileEntity;
import eu.api.exception.NotFoundException;
import eu.api.crypto.CryptoService;
import eu.api.mapper.ProfileMapper;
import eu.api.repository.ProfileRepository;
import eu.api.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private CryptoService cryptoService;
    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private static final UUID USER_ID = UUID.randomUUID();

    @Nested
    @DisplayName("getProfile")
    class GetProfile {

        @Test
        void whenProfileExists_returnsResponseWithDecryptedWorkplace() {
            UserProfileEntity entity = UserProfileEntity.builder()
                    .userId(USER_ID)
                    .fullName("John")
                    .birthDate(LocalDate.of(1990, 1, 15))
                    .phone("+123")
                    .workplace("encrypted-workplace")
                    .build();
            entity.setId(UUID.randomUUID());
            when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));
            when(cryptoService.decrypt("encrypted-workplace")).thenReturn("Office");
            ProfileResponse expected = ProfileResponse.builder()
                    .fullName("John")
                    .birthDate(LocalDate.of(1990, 1, 15))
                    .phone("+123")
                    .workplace("Office")
                    .build();
            when(profileMapper.toResponse(eq(entity), eq("Office"))).thenReturn(expected);

            ProfileResponse response = profileService.getProfile(USER_ID);

            assertThat(response).isSameAs(expected);
            verify(profileRepository).findByUserId(USER_ID);
            verify(cryptoService).decrypt("encrypted-workplace");
        }

        @Test
        void whenProfileNotFound_throwsNotFoundException() {
            when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.getProfile(USER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Profile not found");
            verify(cryptoService, never()).decrypt(any());
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        void whenProfileExists_updatesAndReturnsResponseWithEncryptedWorkplace() {
            UserProfileEntity entity = UserProfileEntity.builder()
                    .userId(USER_ID)
                    .fullName("Old")
                    .build();
            entity.setId(UUID.randomUUID());
            when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(entity));
            when(profileRepository.save(any(UserProfileEntity.class))).thenAnswer(i -> i.getArgument(0));
            when(cryptoService.encrypt("New Office")).thenReturn("enc-new");
            when(cryptoService.decrypt("enc-new")).thenReturn("New Office");
            ProfileResponse expected = ProfileResponse.builder()
                    .fullName("Jane")
                    .birthDate(LocalDate.of(1995, 5, 20))
                    .phone("+456")
                    .workplace("New Office")
                    .build();
            when(profileMapper.toResponse(any(UserProfileEntity.class), eq("New Office"))).thenReturn(expected);

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .fullName("Jane")
                    .birthDate(LocalDate.of(1995, 5, 20))
                    .phone("+456")
                    .workplace("New Office")
                    .build();

            ProfileResponse response = profileService.updateProfile(USER_ID, request);

            assertThat(response).isSameAs(expected);
            verify(profileRepository).save(entity);
            assertThat(entity.getFullName()).isEqualTo("Jane");
            assertThat(entity.getBirthDate()).isEqualTo(LocalDate.of(1995, 5, 20));
            assertThat(entity.getPhone()).isEqualTo("+456");
            assertThat(entity.getWorkplace()).isEqualTo("enc-new");
            verify(cryptoService).encrypt("New Office");
        }

        @Test
        void whenProfileNotFound_throwsNotFoundException() {
            when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            UpdateProfileRequest request = UpdateProfileRequest.builder().fullName("X").build();

            assertThatThrownBy(() -> profileService.updateProfile(USER_ID, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Profile not found");
            verify(profileRepository, never()).save(any());
        }
    }
}
