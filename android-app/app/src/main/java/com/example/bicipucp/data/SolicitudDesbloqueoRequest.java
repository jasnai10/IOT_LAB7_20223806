package com.example.bicipucp.data;

/**
 * Cuerpo de la petición POST /bici/solicitar-desbloqueo.
 * Serializa a: { "codigo": "...", "pin": "..." }
 */
public class SolicitudDesbloqueoRequest {

    private final String codigo;
    private final String pin;

    public SolicitudDesbloqueoRequest(String codigo, String pin) {
        this.codigo = codigo;
        this.pin = pin;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getPin() {
        return pin;
    }
}
