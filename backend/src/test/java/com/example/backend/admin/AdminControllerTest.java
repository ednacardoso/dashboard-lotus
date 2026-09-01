package com.example.backend.admin;

import com.example.backend.security.JwtService;
import com.example.backend.user.Role;
import com.example.backend.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tokenFor(String email, Role role) {
        return jwtService.generateToken(email, "Test User", role.name());
    }

    @Test
    void adminShouldCreateProfessional() throws Exception {
        CreateProfessionalRequest request = new CreateProfessionalRequest(
                "Dra. Ana",
                "ana.unique@example.com",
                "Senha@123",
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
                "Senha@123",
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
                "Senha@123",
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
                "Senha@123",
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
                "Senha@123",
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

    @Test
    void adminShouldUpdateProfessional() throws Exception {
        CreateProfessionalRequest createRequest = new CreateProfessionalRequest(
                "Dr. Original",
                "original.unique@example.com",
                "Senha@123",
                "Psicologia"
        );

        MvcResult createResult = mockMvc.perform(post("/api/admin/professionals")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        UpdateProfessionalRequest updateRequest = new UpdateProfessionalRequest(
                "Dra. Atualizada",
                "atualizado.unique@example.com",
                "Nutrição"
        );

        mockMvc.perform(put("/api/admin/professionals/{id}", id)
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dra. Atualizada"))
                .andExpect(jsonPath("$.email").value("atualizado.unique@example.com"))
                .andExpect(jsonPath("$.specialty").value("Nutrição"));
    }

    @Test
    void adminShouldDeleteProfessional() throws Exception {
        CreateProfessionalRequest createRequest = new CreateProfessionalRequest(
                "Dr. Remover",
                "remover.unique@example.com",
                "Senha@123",
                "Psicologia"
        );

        MvcResult createResult = mockMvc.perform(post("/api/admin/professionals")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/admin/professionals/{id}", id)
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN)))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(id)).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingProfessional() throws Exception {
        UpdateProfessionalRequest updateRequest = new UpdateProfessionalRequest(
                "Nome",
                "nao.existe@example.com",
                "Psicologia"
        );

        mockMvc.perform(put("/api/admin/professionals/{id}", 99999L)
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profissional não encontrado"));
    }

    @Test
    void shouldReturnConflictWhenUpdatingToDuplicateEmail() throws Exception {
        CreateProfessionalRequest firstRequest = new CreateProfessionalRequest(
                "Dr. Primeiro",
                "primeiro.unique@example.com",
                "Senha@123",
                "Psicologia"
        );

        mockMvc.perform(post("/api/admin/professionals")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        CreateProfessionalRequest secondRequest = new CreateProfessionalRequest(
                "Dr. Segundo",
                "segundo.unique@example.com",
                "Senha@123",
                "Nutrição"
        );

        MvcResult createResult = mockMvc.perform(post("/api/admin/professionals")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        UpdateProfessionalRequest updateRequest = new UpdateProfessionalRequest(
                "Dr. Segundo",
                "primeiro.unique@example.com",
                "Nutrição"
        );

        mockMvc.perform(put("/api/admin/professionals/{id}", id)
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    @Test
    void adminShouldUpdateClient() throws Exception {
        CreateClientRequest createRequest = new CreateClientRequest(
                "Cliente Original",
                "cliente.original@example.com",
                "Senha@123"
        );

        MvcResult createResult = mockMvc.perform(post("/api/admin/clients")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        UpdateClientRequest updateRequest = new UpdateClientRequest(
                "Cliente Atualizado",
                "cliente.atualizado@example.com"
        );

        mockMvc.perform(put("/api/admin/clients/{id}", id)
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cliente Atualizado"))
                .andExpect(jsonPath("$.email").value("cliente.atualizado@example.com"));
    }

    @Test
    void adminShouldDeleteClient() throws Exception {
        CreateClientRequest createRequest = new CreateClientRequest(
                "Cliente Remover",
                "cliente.remover@example.com",
                "Senha@123"
        );

        MvcResult createResult = mockMvc.perform(post("/api/admin/clients")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/admin/clients/{id}", id)
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN)))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(id)).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingClient() throws Exception {
        UpdateClientRequest updateRequest = new UpdateClientRequest(
                "Nome",
                "nao.existe@example.com"
        );

        mockMvc.perform(put("/api/admin/clients/{id}", 99999L)
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado"));
    }

    @Test
    void shouldReturnConflictWhenUpdatingClientToDuplicateEmail() throws Exception {
        CreateClientRequest firstRequest = new CreateClientRequest(
                "Cliente Primeiro",
                "cliente.primeiro@example.com",
                "Senha@123"
        );

        mockMvc.perform(post("/api/admin/clients")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        CreateClientRequest secondRequest = new CreateClientRequest(
                "Cliente Segundo",
                "cliente.segundo@example.com",
                "Senha@123"
        );

        MvcResult createResult = mockMvc.perform(post("/api/admin/clients")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        UpdateClientRequest updateRequest = new UpdateClientRequest(
                "Cliente Segundo",
                "cliente.primeiro@example.com"
        );

        mockMvc.perform(put("/api/admin/clients/{id}", id)
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }
}
