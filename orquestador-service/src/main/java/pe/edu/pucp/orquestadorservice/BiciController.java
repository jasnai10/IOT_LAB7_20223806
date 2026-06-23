package pe.edu.pucp.orquestadorservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/bici")
public class BiciController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ValidadorFeignClient validadorFeignClient;

    private static final String VALIDADOR_URL = "http://localhost:8001";

    @PostMapping("/solicitar-desbloqueo")
    public ResponseEntity<?> solicitarDesbloqueo(@RequestBody SolicitudDesbloqueoDTO solicitud) {
        System.out.println("===> Petición recibida: codigo=" + solicitud.getCodigo()
                + ", pin=" + solicitud.getPin());

        String codigo = solicitud.getCodigo();
        String pin = solicitud.getPin();

        // ---------- 1. Validar alumno via RestTemplate ----------
        Boolean alumnoValido;
        try {
            String url = VALIDADOR_URL + "/validar/alumno/" + codigo;
            alumnoValido = restTemplate.getForObject(url, Boolean.class);
            System.out.println("===> Validación alumno (RestTemplate): " + alumnoValido);
        } catch (Exception e) {
            System.out.println("===> Error al validar alumno: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "No se pudo contactar al servicio validador. Intenta nuevamente.")
            );
        }

        if (alumnoValido == null || !alumnoValido) {
            String razon = explicarRazonAlumno(codigo);
            System.out.println("===> Alumno RECHAZADO: " + razon);
            return ResponseEntity.badRequest().body(Map.of("mensaje", razon));
        }

        // ---------- 2. Validar candado via Feign ----------
        Boolean candadoValido;
        try {
            candadoValido = validadorFeignClient.validarCandado(pin);
            System.out.println("===> Validación candado (Feign): " + candadoValido);
        } catch (Exception e) {
            System.out.println("===> Error al validar candado: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "No se pudo contactar al servicio validador. Intenta nuevamente.")
            );
        }

        if (candadoValido == null || !candadoValido) {
            String razon = explicarRazonCandado(pin);
            System.out.println("===> Candado RECHAZADO: " + razon);
            return ResponseEntity.badRequest().body(Map.of("mensaje", razon));
        }

        // ---------- 3. Ambas validaciones OK → APROBADO ----------
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", "APROBADO");
        respuesta.put("iot_auth_token", "PUCP-BIKE-" + UUID.randomUUID().toString().substring(0, 8));
        respuesta.put("desbloqueo_expira_en", 120);
        respuesta.put("timestamp_aprobacion",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        System.out.println("===> Respuesta APROBADO enviada: " + respuesta);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Explica el motivo exacto por el cual el código de alumno fue rechazado.
     * Reglas (idénticas a las del validador): 8 dígitos numéricos, prefijo "20".
     */
    private String explicarRazonAlumno(String codigo) {
        if (codigo == null || codigo.isEmpty()) {
            return "El código de alumno está vacío.";
        }
        if (codigo.length() != 8) {
            return "El código de alumno debe tener exactamente 8 dígitos (ingresaste " + codigo.length() + ").";
        }
        if (!codigo.matches("\\d{8}")) {
            return "El código de alumno solo puede contener dígitos numéricos.";
        }
        if (!codigo.startsWith("20")) {
            return "El código de alumno debe comenzar con el prefijo '20' (los códigos que no inician con 20 no están registrados en la base de datos).";
        }
        return "El código de alumno no cumple las reglas de validación.";
    }

    /**
     * Explica el motivo exacto por el cual el PIN del candado fue rechazado.
     * Reglas (idénticas a las del validador): 4 dígitos numéricos, sin repeticiones consecutivas.
     */
    private String explicarRazonCandado(String pin) {
        if (pin == null || pin.isEmpty()) {
            return "El PIN del candado IoT está vacío.";
        }
        if (pin.length() != 4) {
            return "El PIN del candado IoT debe tener exactamente 4 dígitos (ingresaste " + pin.length() + ").";
        }
        if (!pin.matches("\\d{4}")) {
            return "El PIN del candado IoT solo puede contener dígitos numéricos.";
        }
        for (int i = 0; i < pin.length() - 1; i++) {
            if (pin.charAt(i) == pin.charAt(i + 1)) {
                return "El PIN del candado IoT no debe contener dígitos repetidos de forma consecutiva (encontrado: '"
                        + pin.charAt(i) + pin.charAt(i + 1) + "' en la posición " + (i + 1) + ").";
            }
        }
        return "El PIN del candado IoT no cumple las reglas de validación.";
    }
}