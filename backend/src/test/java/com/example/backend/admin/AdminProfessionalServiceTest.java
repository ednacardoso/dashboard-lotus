package com.example.backend.admin;

import com.example.backend.user.Role;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProfessionalServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminProfessionalService adminProfessionalService;

    @Test
    void shouldCreateProfessionalWithEncryptedPasswordAndRole() {
        CreateProfessionalRequest request = new CreateProfessionalRequest(
                "Dra. Ana",
                "ana@example.com",
                "Senha@123",
                "Psicologia"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        User savedUser = User.builder()
                .id(1L)
                .name(request.name())
                .email(request.email())
                .password("encoded-password")
                .role(Role.PROFESSIONAL)
                .specialty(request.specialty())
                .build();

        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(savedUser);

        CreateUserResponse response = adminProfessionalService.createProfessional(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Dra. Ana");
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.role()).isEqualTo("PROFESSIONAL");
        assertThat(response.specialty()).isEqualTo("Psicologia");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User captured = userCaptor.getValue();
        assertThat(captured.getRole()).isEqualTo(Role.PROFESSIONAL);
        assertThat(captured.getPassword()).isEqualTo("encoded-password");
        assertThat(captured.getSpecialty()).isEqualTo("Psicologia");
    }

    @Test
    void shouldRejectCreationWhenEmailAlreadyExists() {
        CreateProfessionalRequest request = new CreateProfessionalRequest(
                "Dr. João",
                "joao@example.com",
                "senha123",
                "Nutrição"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> adminProfessionalService.createProfessional(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("E-mail já cadastrado");
    }
}
