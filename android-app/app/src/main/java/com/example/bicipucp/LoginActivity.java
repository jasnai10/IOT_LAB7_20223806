package com.example.bicipucp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bicipucp.auth.AuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilCorreo, tilPassword;
    private TextInputEditText etCorreo, etPassword;
    private MaterialButton btnIngresar;
    private CircularProgressIndicator progress;
    private View root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si ya hay sesión activa, saltar directo a MainActivity.
        if (AuthService.estaLogueado()) {
            irAMain();
            return;
        }

        setContentView(R.layout.activity_login);

        root = findViewById(R.id.login_root);
        tilCorreo = findViewById(R.id.til_correo);
        tilPassword = findViewById(R.id.til_password);
        etCorreo = findViewById(R.id.et_correo);
        etPassword = findViewById(R.id.et_password);
        btnIngresar = findViewById(R.id.btn_ingresar);
        progress = findViewById(R.id.progress);

        btnIngresar.setOnClickListener(v -> intentarLogin());
        findViewById(R.id.btn_ir_registro).setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void intentarLogin() {
        tilCorreo.setError(null);
        tilPassword.setError(null);

        String correo = etCorreo.getText() != null ? etCorreo.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean valido = true;
        if (TextUtils.isEmpty(correo)) {
            tilCorreo.setError("Ingresa tu correo");
            valido = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Ingresa tu contraseña");
            valido = false;
        }
        if (!valido) {
            return;
        }

        mostrarCarga(true);
        AuthService.iniciarSesion(correo, password, task -> {
            mostrarCarga(false);
            if (task.isSuccessful()) {
                irAMain();
            } else {
                String msg = task.getException() != null
                        ? task.getException().getMessage()
                        : "No se pudo iniciar sesión";
                Snackbar.make(root, msg, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarCarga(boolean cargando) {
        progress.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnIngresar.setEnabled(!cargando);
    }

    private void irAMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
