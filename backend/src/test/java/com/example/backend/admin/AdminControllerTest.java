package com.example.backend.admin;

import com.example.backend.security.JwtService;
import com.example.backend.user.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenFor(String email, Role role) {
        return jwtService.generateToken(email, "Test User", role.name());
    }

    @Test
    void adminShouldCreateProfessional() throws Exception {
        CreateProfessionalRequest request = new CreateProfessionalRequest(
                "Dra. Ana",
                "ana.unique@example.com",
                "senha123",
                "Psicologia"
        );

        mockMvc.perform(post("/api/admin/professionals")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dra. Ana"))
                .andExpect(jsonPath("$.email").value("ana.unique@example.com"))
                .andExpect(jsonPath("$.specialty").value("Psicologia"));
    }

    @Test
    void clientShouldBeForbidden() throws Exception {
        CreateProfessionalRequest request = new CreateProfessionalRequest(
                "Dr. João",
                "joao.unique@example.com",
                "senha123",
                "Nutrição"
        );

        mockMvc.perform(post("/api/admin/professionals")
                        .header("Authorization", "Bearer " + tokenFor("cliente@example.com", Role.CLIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void professionalShouldBeForbidden() throws Exception {
        CreateProfessionalRequest request = new CreateProfessionalRequest(
                "Dra. Maria",
                "maria.unique@example.com",
                "senha123",
                "Fisioterapia"
        );

        mockMvc.perform(post("/api/admin/professionals")
                        .header("Authorization", "Bearer " + tokenFor("profissional@example.com", Role.PROFESSIONAL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedShouldBeUnauthorized() throws Exception {
        CreateProfessionalRequest request = new CreateProfessionalRequest(
                "Dr. Carlos",
                "carlos.unique@example.com",
                "senha123",
                "Cardiologia"
        );

        mockMvc.perform(post("/api/admin/professionals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnConflictForDuplicateEmail() throws Exception {
        CreateProfessionalRequest request = new CreateProfessionalRequest(
                "Dr. Duplicado",
                "duplicado.unique@example.com",
                "senha123",
                "Dermatologia"
        );

        mockMvc.perform(post("/api/admin/professionals")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/professionals")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        CreateProfessionalRequest request = new CreateProfessionalRequest(
                "",
                "invalid-email",
                "123",
                ""
        );

        mockMvc.perform(post("/api/admin/professionals")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
