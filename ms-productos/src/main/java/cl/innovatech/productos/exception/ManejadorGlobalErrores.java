package cl.innovatech.productos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centraliza el formato de las respuestas de error de la API,
 * de modo que el cliente reciba siempre un JSON coherente.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    private Map<String, Object> cuerpo(HttpStatus estado, String mensaje) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("timestamp", LocalDateTime.now().toString());
        respuesta.put("status", estado.value());
        respuesta.put("error", estado.getReasonPhrase());
        respuesta.put("mensaje", mensaje);
        return respuesta;
    }

    /** 403 - el token es valido pero no tiene el scope o rol exigido. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accesoDenegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(cuerpo(HttpStatus.FORBIDDEN,
                        "Token valido, pero no cuenta con el permiso necesario para esta operacion."));
    }

    /** 404 - el recurso solicitado no existe. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(cuerpo(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /** 400 - datos de entrada invalidos. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));

        Map<String, Object> respuesta = cuerpo(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos.");
        respuesta.put("campos", errores);
        return ResponseEntity.badRequest().body(respuesta);
    }
}
