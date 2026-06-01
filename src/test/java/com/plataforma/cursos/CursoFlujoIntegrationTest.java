package com.plataforma.cursos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CursoFlujoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generaArchivoPdfDelResumen() throws Exception {
        String inscripcion = """
                {
                  "estudianteNombre": "Maria Lopez",
                  "estudianteEmail": "maria@example.com",
                  "cursoIds": [1, 3]
                }
                """;

        String respuesta = mockMvc.perform(post("/api/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inscripcion))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(respuesta);
        long inscripcionId = json.get("inscripcionId").asLong();

        mockMvc.perform(get("/api/inscripciones/{id}/resumen/archivo", inscripcionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void contextLoadsYListaCursos() throws Exception {
        mockMvc.perform(get("/api/cursos"))
                .andExpect(status().isOk());
    }

    @Test
    void creaCursoYLuegoInscribe() throws Exception {
        String nuevoCurso = """
                {
                  "nombre": "Testing con JUnit",
                  "instructor": "Pedro Lopez",
                  "duracionHoras": 20,
                  "costo": 90.00
                }
                """;

        mockMvc.perform(post("/api/cursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoCurso))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Testing con JUnit"));

        String inscripcion = """
                {
                  "estudianteNombre": "Juan Perez",
                  "estudianteEmail": "juan@example.com",
                  "cursoIds": [1, 2]
                }
                """;

        mockMvc.perform(post("/api/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inscripcion))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inscripcionId").exists())
                .andExpect(jsonPath("$.cursos.length()").value(2))
                .andExpect(jsonPath("$.total").value(320.0));
    }
}
