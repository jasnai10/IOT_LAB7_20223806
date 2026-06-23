package com.example.bicipucp.data;

import com.google.gson.annotations.SerializedName;

/**
 * Respuesta 400 Bad Request del orquestador cuando una validación falla.
 * <pre>{ "mensaje": "El código de alumno no existe en la base de datos" }</pre>
 */
public class ErrorResponse {

    @SerializedName("mensaje")
    private String mensaje;

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
