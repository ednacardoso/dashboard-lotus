package com.example.backend.room;

import com.example.backend.security.JwtService;
import com.example.backend.user.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoomRepository roomRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tokenFor(String email, Role role) {
        return jwtService.generateToken(email, "Test User", role.name());
    }

    @Test
    void adminShouldUpdateRoom() throws Exception {
        RoomRequest createRequest = new RoomRequest(
                "Sala Original",
                "Descrição original",
                5,
                new BigDecimal("500.00")
        );

        MvcResult createResult = mockMvc.perform(post("/api/admin/rooms")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        RoomRequest updateRequest = new RoomRequest(
                "Sala Atualizada",
                "Descrição atualizada",
                10,
                new BigDecimal("750.00")
        );

        mockMvc.perform(put("/api/admin/rooms/{id}", id)
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sala Atualizada"))
                .andExpect(jsonPath("$.description").value("Descrição atualizada"))
                .andExpect(jsonPath("$.capacity").value(10))
                .andExpect(jsonPath("$.monthlyPrice").value(750.00));
    }

    @Test
    void adminShouldDeleteRoom() throws Exception {
        RoomRequest createRequest = new RoomRequest(
                "Sala Remover",
                "Descrição",
                3,
                new BigDecimal("300.00")
        );

        MvcResult createResult = mockMvc.perform(post("/api/admin/rooms")
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/admin/rooms/{id}", id)
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN)))
                .andExpect(status().isNoContent());

        assertThat(roomRepository.findById(id)).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingRoom() throws Exception {
        mockMvc.perform(delete("/api/admin/rooms/{id}", 99999L)
                        .header("Authorization", "Bearer " + tokenFor("admin@example.com", Role.ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sala não encontrada"));
    }
}
