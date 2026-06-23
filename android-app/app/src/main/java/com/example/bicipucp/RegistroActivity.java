package com.example.bicipucp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bicipucp.auth.AuthService;
import com.example.bicipucp.data.ApiClient;
import com.example.bicipucp.data.DesbloqueoResponse;
import com.example.bicipucp.data.ErrorResponse;
import com.example.bicipucp.data.SolicitudDesbloqueoRequest;
import com.example.bicipucp.data.UsuarioRepository;
import com.example.bicipucp.model.Usuario;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class RegistroActivity extends AppCompatActivity {

    private TextInputLayout tilNombre, tilCorreo, tilPassword, tilCodigo, tilPin;
    private TextInputEditText etNombre, etCorreo, etPassword, etCodigo, etPin;
    private MaterialButton btnRegistrar;
    private View bloqueProgreso, root;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final UsuarioRepository repo = new UsuarioRepository();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        root = findViewById(R.id.registro_root);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilNombre = findViewById(R.id.til_nombre);
        tilCorreo = findViewById(R.id.til_correo);
        tilPassword = findViewById(R.id.til_password);
        tilCodigo = findViewById(R.id.til_codigo);
        tilPin = findViewById(R.id.til_pin);
        etNombre = findViewById(R.id.et_nombre);
        etCorreo = findViewById(R.id.et_correo);
        etPassword = findViewById(R.id.et_password);
        etCodigo = findViewById(R.id.et_codigo);
        etPin = findViewById(R.id.et_pin);
        btnRegistrar = findViewById(R.id.btn_validar_registrar);
        bloqueProgreso = findViewById(R.id.bloque_progreso);

        btnRegistrar.setOnClickListener(v -> validarYRegistrar());
    }

    private void validarYRegistrar() {
        tilNombre.setError(null);
        tilCorreo.setError(null);
        tilPassword.setError(null);
        tilCodigo.setError(null);
        tilPin.setError(null);

        String nombre = texto(etNombre);
        String correo = texto(etCorreo);
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String codigo = texto(etCodigo);
        String pin = texto(etPin);

        boolean valido = true;
        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError("Campo obligatorio");
            valido = false;
        }
        if (TextUtils.isEmpty(correo)) {
            tilCorreo.setError("Campo obligatorio");
            valido = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Campo obligatorio");
            valido = false;
        }
        if (codigo.length() != 8) {
            tilCodigo.setError("Debe tener exactamente 8 dígitos");
            valido = false;
        }
        if (pin.length() != 4) {
            tilPin.setError("Debe tener exactamente 4 dígitos");
            valido = false;
        }
        if (!valido) {
            return;
        }

        mostrarCarga(true);
        solicitarDesbloqueo(nombre, correo, password, codigo, pin);
    }

    /** Llamada de red en hilo de fondo (Retrofit síncrono dentro del ExecutorService). */
    private void solicitarDesbloqueo(String nombre, String correo, String password,
                                     String codigo, String pin) {
        executor.execute(() -> {
            try {
                Response<DesbloqueoResponse> response = ApiClient.getBiciApi()
                        .solicitarDesbloqueo(new SolicitudDesbloqueoRequest(codigo, pin))
                        .execute();

                if (response.isSuccessful() && response.body() != null) {
                    DesbloqueoResponse data = response.body();
                    runOnUiThread(() -> registrarEnFirebase(nombre, correo, password, codigo, pin, data));
                } else {
                    final String mensaje = extraerMensajeError(response);
                    runOnUiThread(() -> mostrarError(mensaje));
                }
            } catch (Exception e) {
                final String mensaje = "No se pudo conectar con el servidor. Verifica que el backend esté activo.";
                runOnUiThread(() -> mostrarError(mensaje));
            }
        });
    }

    private String extraerMensajeError(Response<DesbloqueoResponse> response) {
        try {
            if (response.errorBody() != null) {
                String json = response.errorBody().string();
                ErrorResponse error = gson.fromJson(json, ErrorResponse.class);
                if (error != null && !TextUtils.isEmpty(error.getMensaje())) {
                    return error.getMensaje();
                }
            }
        } catch (Exception ignored) {
        }
        return "La validación fue rechazada por el servidor.";
    }

    /** Solo se ejecuta cuando el backend respondió 200 OK. */
    private void registrarEnFirebase(String nombre, String correo, String password,
                                     String codigo, String pin, DesbloqueoResponse data) {
        AuthService.registrarUsuario(correo, password, authTask -> {
            if (!authTask.isSuccessful()) {
                String msg = authTask.getException() != null
                        ? authTask.getException().getMessage()
                        : "No se pudo crear la cuenta";
                mostrarError(msg);
                return;
            }

            String uid = AuthService.getUidActual();
            Usuario usuario = new Usuario(
                    nombre, correo, codigo, pin, "",
                    data.getIotAuthToken(), data.getDesbloqueoExpiraEn(),
                    data.getTimestampAprobacion());

            repo.guardarUsuario(uid, usuario, saveTask -> {
                mostrarCarga(false);
                if (saveTask.isSuccessful()) {
                    Snackbar.make(root, "Registro exitoso", Snackbar.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finishAffinity();
                } else {
                    String msg = saveTask.getException() != null
                            ? saveTask.getException().getMessage()
                            : "No se pudo guardar el perfil";
                    Snackbar.make(root, msg, Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    private void mostrarError(String mensaje) {
        mostrarCarga(false);
        Snackbar.make(root, mensaje, Snackbar.LENGTH_LONG).show();
    }

    private void mostrarCarga(boolean cargando) {
        bloqueProgreso.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnRegistrar.setEnabled(!cargando);
    }

    private String texto(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
