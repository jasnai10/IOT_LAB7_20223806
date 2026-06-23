package pe.edu.pucp.pucpvalidadorservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/validar")
public class ValidadorController {

    /**
     * Valida que el código tenga exactamente 8 dígitos numéricos
     * y comience con el prefijo "20".
     * Ej: 20230145 -> true | 19984512 -> false
     */
    @GetMapping("/alumno/{codigo}")
    public boolean validarAlumno(@PathVariable String codigo) {
        if (codigo == null) return false;
        if (codigo.length() != 8) return false;
        if (!codigo.matches("\\d{8}")) return false;
        return codigo.startsWith("20");
    }

    /**
     * Valida que el PIN sea numérico de 4 dígitos
     * y NO contenga números repetidos de forma CONSECUTIVA.
     * Ej: 1234 o 1010 -> true | 1123 o 5555 -> false
     */
    @GetMapping("/candado/{pin}")
    public boolean validarCandado(@PathVariable String pin) {
        if (pin == null) return false;
        if (pin.length() != 4) return false;
        if (!pin.matches("\\d{4}")) return false;

        for (int i = 0; i < pin.length() - 1; i++) {
            if (pin.charAt(i) == pin.charAt(i + 1)) {
                return false;
            }
        }
        return true;
    }
}