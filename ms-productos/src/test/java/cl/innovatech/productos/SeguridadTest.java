package cl.innovatech.productos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de seguridad del microservicio (Indicador 2 - EP1).
 *
 * Verifican que:
 *   - El endpoint publico responde sin token.
 *   - Los endpoints protegidos responden 401 sin token.
 *   - Un token sin el scope exigido recibe 403.
 *   - Un token con el scope correcto recibe 200.
 *   - La autorizacion por rol funciona.
 */
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
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    @DisplayName("GET /api/productos sin token responde 401")
    void listarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/productos sin token responde 401")
    void crearSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Test\",\"precio\":100,\"stock\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/me sin token responde 401")
    void meSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token con scope Productos.Read puede listar (200)")
    void listarConScopeCorrectoDevuelve200() throws Exception {
        mockMvc.perform(get("/api/productos")
                        .with(jwt().authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "SCOPE_Productos.Read"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Token SIN el scope de escritura recibe 403 al crear")
    void crearSinScopeEscrituraDevuelve403() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .with(jwt().authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "SCOPE_Productos.Read")))
                        .contentType("application/json")
                        .content("{\"nombre\":\"Test\",\"precio\":100,\"stock\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Token con scope Productos.Write puede crear (201)")
    void crearConScopeEscrituraDevuelve201() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .with(jwt().authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "SCOPE_Productos.Write")))
                        .contentType("application/json")
                        .content("{\"nombre\":\"Collar reflectante\",\"descripcion\":\"Collar de seguridad\",\"precio\":6990,\"stock\":15}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Collar reflectante"));
    }

    @Test
    @DisplayName("Endpoint de rol Admin rechaza a quien no tiene el rol (403)")
    void resumenSinRolAdminDevuelve403() throws Exception {
        mockMvc.perform(get("/api/productos/admin/resumen")
                        .with(jwt().authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "SCOPE_Productos.Read"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Endpoint de rol Admin acepta a quien si tiene el rol (200)")
    void resumenConRolAdminDevuelve200() throws Exception {
        mockMvc.perform(get("/api/productos/admin/resumen")
                        .with(jwt().authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "ROLE_Admin"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Datos invalidos con token valido responden 400")
    void datosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .with(jwt().authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "SCOPE_Productos.Write")))
                        .contentType("application/json")
                        .content("{\"descripcion\":\"sin nombre ni precio\"}"))
                .andExpect(status().isBadRequest());
    }
}
