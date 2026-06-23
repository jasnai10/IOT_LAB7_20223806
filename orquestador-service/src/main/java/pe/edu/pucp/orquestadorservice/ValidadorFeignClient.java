package pe.edu.pucp.orquestadorservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pucp-validador-service")
public interface ValidadorFeignClient {

    @GetMapping("/validar/candado/{pin}")
    boolean validarCandado(@PathVariable("pin") String pin);
}