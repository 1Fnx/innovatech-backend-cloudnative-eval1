package cl.innovatech.pedidos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("El endpoint publico /api/health responde 200 sin token")
    void healthEsPublico() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicio").value("ms-pedidos"));
    }

    @Test
    @DisplayName("GET /api/pedidos sin token responde 401")
    void listarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token con scope de lectura puede listar pedidos (200)")
    void listarConScopeDevuelve200() throws Exception {
        mockMvc.perform(get("/api/pedidos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_Productos.Read"))))
                .andExpect(status().isOk());
    }
}
