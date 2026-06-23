package com.example.bicipucp.data;

import com.google.gson.annotations.SerializedName;

/**
 * Respuesta 200 OK del orquestador cuando el desbloqueo es aprobado.
 * <pre>
 * {
 *   "status": "APROBADO",
 *   "iot_auth_token": "PUCP-BIKE-7f4a9b21",
 *   "desbloqueo_expira_en": 120,
 *   "timestamp_aprobacion": "2026-06-23T21:30:15"
 * }
 * </pre>
 */
public class DesbloqueoResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("iot_auth_token")
    private String iotAuthToken;

    @SerializedName("desbloqueo_expira_en")
    private long desbloqueoExpiraEn;

    @SerializedName("timestamp_aprobacion")
    private String timestampAprobacion;

    public String getStatus() {
        return status;
    }

    public String getIotAuthToken() {
        return iotAuthToken;
    }

    public long getDesbloqueoExpiraEn() {
        return desbloqueoExpiraEn;
    }

    public String getTimestampAprobacion() {
        return timestampAprobacion;
    }
}
