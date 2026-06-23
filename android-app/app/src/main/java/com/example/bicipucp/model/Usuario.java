package com.example.bicipucp.model;

import com.google.firebase.firestore.PropertyName;

/**
 * Perfil del usuario almacenado en Firestore (colección "usuarios",
 * ID del documento = UID de Firebase Auth).
 *
 * Los nombres de los campos en el documento usan snake_case; se mapean
 * con {@link PropertyName} para mantener convención Java en el código.
 */
public class Usuario {

    private String nombreCompleto;
    private String correo;
    private String codigoPucp;
    private String pinCandado;
    private String fotoUrl;
    private String iotAuthToken;
    private long desbloqueoExpiraEn;
    private String timestampAprobacion;

    /** Constructor vacío requerido por Firestore para deserializar. */
    public Usuario() {
    }

    public Usuario(String nombreCompleto, String correo, String codigoPucp, String pinCandado,
                   String fotoUrl, String iotAuthToken, long desbloqueoExpiraEn,
                   String timestampAprobacion) {
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.codigoPucp = codigoPucp;
        this.pinCandado = pinCandado;
        this.fotoUrl = fotoUrl;
        this.iotAuthToken = iotAuthToken;
        this.desbloqueoExpiraEn = desbloqueoExpiraEn;
        this.timestampAprobacion = timestampAprobacion;
    }

    @PropertyName("nombre_completo")
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    @PropertyName("nombre_completo")
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    @PropertyName("correo")
    public String getCorreo() {
        return correo;
    }

    @PropertyName("correo")
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    @PropertyName("codigo_pucp")
    public String getCodigoPucp() {
        return codigoPucp;
    }

    @PropertyName("codigo_pucp")
    public void setCodigoPucp(String codigoPucp) {
        this.codigoPucp = codigoPucp;
    }

    @PropertyName("pin_candado")
    public String getPinCandado() {
        return pinCandado;
    }

    @PropertyName("pin_candado")
    public void setPinCandado(String pinCandado) {
        this.pinCandado = pinCandado;
    }

    @PropertyName("foto_url")
    public String getFotoUrl() {
        return fotoUrl;
    }

    @PropertyName("foto_url")
    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    @PropertyName("iot_auth_token")
    public String getIotAuthToken() {
        return iotAuthToken;
    }

    @PropertyName("iot_auth_token")
    public void setIotAuthToken(String iotAuthToken) {
        this.iotAuthToken = iotAuthToken;
    }

    @PropertyName("desbloqueo_expira_en")
    public long getDesbloqueoExpiraEn() {
        return desbloqueoExpiraEn;
    }

    @PropertyName("desbloqueo_expira_en")
    public void setDesbloqueoExpiraEn(long desbloqueoExpiraEn) {
        this.desbloqueoExpiraEn = desbloqueoExpiraEn;
    }

    @PropertyName("timestamp_aprobacion")
    public String getTimestampAprobacion() {
        return timestampAprobacion;
    }

    @PropertyName("timestamp_aprobacion")
    public void setTimestampAprobacion(String timestampAprobacion) {
        this.timestampAprobacion = timestampAprobacion;
    }
}
