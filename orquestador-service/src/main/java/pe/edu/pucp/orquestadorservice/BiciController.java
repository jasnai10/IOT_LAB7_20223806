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

    // URL del validador. Usamos "http://pucp-validador-service" gracias a Eureka:
    // RestTemplate resuelve el nombre del servicio al puerto/IP real.
    // (En este lab simplificado, también puede ir directo a localhost:8001.)
    private static final String VALIDADOR_URL = "http://localhost:8001";

    @PostMapping("/solicitar-desbloqueo")
    public ResponseEntity<?> solicitarDesbloqueo(@RequestBody SolicitudDesbloqueoDTO solicitud) {
        System.out.println("===> Petición recibida: codigo=" + solicitud.getCodigo()
                + ", pin=" + solicitud.getPin());

        // 1. Validar alumno via RestTemplate
        Boolean alumnoValido;
        try {
            String url = VALIDADOR_URL + "/validar/alumno/" + solicitud.getCodigo();
            alumnoValido = restTemplate.getForObject(url, Boolean.class);
            System.out.println("===> Validación alumno (RestTemplate): " + alumnoValido);
        } catch (Exception e) {
            System.out.println("===> Error al validar alumno: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "Error al contactar el servicio validador (alumno)")
            );
        }

        if (alumnoValido == null || !alumnoValido) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "El código de alumno no existe en la base de datos")
            );
        }

        // 2. Validar candado via Feign
        Boolean candadoValido;
        try {
            candadoValido = validadorFeignClient.validarCandado(solicitud.getPin());
            System.out.println("===> Validación candado (Feign): " + candadoValido);
        } catch (Exception e) {
            System.out.println("===> Error al validar candado: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "Error al contactar el servicio validador (candado)")
            );
        }

        if (candadoValido == null || !candadoValido) {
            return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "El PIN del candado IoT no es válido")
            );
        }

        // 3. Ambas validaciones OK → APROBADO
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", "APROBADO");
        respuesta.put("iot_auth_token", "PUCP-BIKE-" + UUID.randomUUID().toString().substring(0, 8));
        respuesta.put("desbloqueo_expira_en", 120);
        respuesta.put("timestamp_aprobacion",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        System.out.println("===> Respuesta APROBADO enviada");
        return ResponseEntity.ok(respuesta);
    }
}