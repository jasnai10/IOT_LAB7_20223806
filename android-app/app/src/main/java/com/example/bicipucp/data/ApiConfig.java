package com.example.bicipucp.data;

/**
 * Configuración centralizada de la API del backend Spring Boot.
 * Para probar en un celular físico, cambia BASE_URL por la IP de tu PC
 * (ej. "http://192.168.1.10:8080/"). El emulador usa 10.0.2.2 como alias
 * de localhost de la máquina anfitriona.
 */
public final class ApiConfig {

    public static final String BASE_URL = "http://10.0.2.2:8080/";

    private ApiConfig() {
    }
}
