# Innovatech Chile - Backend Spring Boot (EP1 Cloud Native I)

Backend de la tienda de alimentos para perritos, compuesto por **dos
microservicios** Java con Spring Boot que actuan como **OAuth2 Resource Server**:
validan los Access Token JWT emitidos por **Microsoft Entra External ID** antes
de exponer cualquier dato.

| Microservicio | Puerto | Responsabilidad |
|---|---|---|
| `ms-productos` | 8081 | Catalogo de productos (CRUD) |
| `ms-pedidos`   | 8082 | Pedidos de clientes |

## Requisitos
- Java 17 o superior
- Maven 3.9+ (o abrir el proyecto en IntelliJ / VS Code con soporte Maven)

## Configuracion

Editar `src/main/resources/application.properties` de **cada** microservicio y
reemplazar los valores marcados:

```properties
app.security.tenant-subdomain=innovatechcl
app.security.tenant-id=<DIRECTORY_TENANT_ID>
app.security.audiences=api://<BACKEND_CLIENT_ID>
app.cors.allowed-origins=http://localhost:5173
```

## Ejecutar

En una terminal por microservicio, desde su carpeta:

```bash
mvn spring-boot:run
```

El perfil por defecto es `demo`, que usa una base **H2 en memoria** y no
requiere instalar MySQL. Para usar la base de datos cloud:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=cloud
```

definiendo antes las variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

## Pruebas

```bash
mvn test
```

Las pruebas verifican los codigos 200 / 401 / 403 y la autorizacion por
scope y por rol, sin necesidad de contactar a Microsoft Entra ID.

## Endpoints

### ms-productos (8081)
| Metodo | Ruta | Proteccion |
|---|---|---|
| GET | `/api/health` | Publico |
| GET | `/api/me` | Token valido |
| GET | `/api/productos` | `SCOPE_Productos.Read` |
| GET | `/api/productos/{id}` | `SCOPE_Productos.Read` |
| POST | `/api/productos` | `SCOPE_Productos.Write` |
| PUT | `/api/productos/{id}` | `SCOPE_Productos.Write` |
| DELETE | `/api/productos/{id}` | `SCOPE_Productos.Write` |
| GET | `/api/productos/admin/resumen` | `ROLE_Admin` |

### ms-pedidos (8082)
| Metodo | Ruta | Proteccion |
|---|---|---|
| GET | `/api/health` | Publico |
| GET | `/api/pedidos` | `SCOPE_Productos.Read` |
| POST | `/api/pedidos` | `SCOPE_Productos.Write` |
| DELETE | `/api/pedidos/{id}` | `SCOPE_Productos.Write` |

## Que valida del token (Indicador 2 - EP1)

| Control | Implementacion |
|---|---|
| Firma | `NimbusJwtDecoder.withJwkSetUri(...)` descarga las claves publicas del IdP |
| Issuer | `JwtValidators.createDefaultWithIssuer(...)` |
| Audience | `AudienceValidator` (clase propia) |
| Expiracion | Validador por defecto de Spring Security |
| Scopes | `@PreAuthorize("hasAuthority('SCOPE_...')")` |
| Roles | `@PreAuthorize("hasRole('Admin')")` |
| 401 / 403 | Cadena de filtros + `ManejadorGlobalErrores` |

## Estructura

```
ms-productos/
├── pom.xml
└── src/main/java/cl/innovatech/productos/
    ├── MsProductosApplication.java
    ├── config/
    │   ├── SecurityConfig.java      <- Resource Server, CORS, scopes y roles
    │   └── AudienceValidator.java   <- valida el claim aud
    ├── controller/
    │   ├── ProductoController.java  <- CRUD protegido con @PreAuthorize
    │   ├── HealthController.java    <- endpoint publico
    │   └── MeController.java        <- muestra los claims del token
    ├── model/Producto.java          <- entidad JPA
    ├── repository/ProductoRepository.java
    ├── service/ProductoService.java
    └── exception/ManejadorGlobalErrores.java
```
