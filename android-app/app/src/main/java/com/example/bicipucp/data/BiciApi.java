package com.example.bicipucp.data;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Contrato Retrofit del backend BiciPUCP.
 */
public interface BiciApi {

    @POST("bici/solicitar-desbloqueo")
    Call<DesbloqueoResponse> solicitarDesbloqueo(@Body SolicitudDesbloqueoRequest request);
}
