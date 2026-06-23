package com.example.bicipucp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bicipucp.auth.AuthService;
import com.example.bicipucp.data.ApiClient;
import com.example.bicipucp.data.DesbloqueoResponse;
import com.example.bicipucp.data.ErrorResponse;
import com.example.bicipucp.data.SolicitudDesbloqueoRequest;
import com.example.bicipucp.data.UsuarioRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final long DEFAULT_VIDA_SEGUNDOS = 120;

    private View root;
    private MaterialCardView cardEstado;
    private Chip chipEstado;
    private android.widget.TextView tvContador, tvTitulo, tvSubtitulo;
    private MaterialButton btnNuevoDesbloqueo;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final UsuarioRepository repo = new UsuarioRepository();
    private final Gson gson = new Gson();

    private CountDownTimer timer;
    private String uid;
    private String codigoPucp;
    private long vidaSegundos = DEFAULT_VIDA_SEGUNDOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uid = AuthService.getUidActual();
        if (uid == null) {
            irALogin();
            return;
        }

        setContentView(R.layout.activity_main);

        root = findViewById(R.id.main);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cardEstado = findViewById(R.id.card_estado);
        chipEstado = findViewById(R.id.chip_estado);
        tvContador = findViewById(R.id.tv_contador);
        tvTitulo = findViewById(R.id.tv_titulo);
        tvSubtitulo = findViewById(R.id.tv_subtitulo);
        btnNuevoDesbloqueo = findViewById(R.id.btn_nuevo_desbloqueo);

        btnNuevoDesbloqueo.setOnClickListener(v -> mostrarDialogoPin());

        cargarEstadoDesdeFirestore();
    }

    private void cargarEstadoDesdeFirestore() {
        repo.obtenerUsuario(uid, snapshot -> {
            if (!snapshot.exists()) {
                Snackbar.make(root, "Tu perfil no existe. Inicia sesión nuevamente.",
                        Snackbar.LENGTH_LONG).show();
                AuthService.cerrarSesion();
                irALogin();
                return;
            }

            codigoPucp = snapshot.getString("codigo_pucp");
            Long expira = snapshot.getLong("desbloqueo_expira_en");
            vidaSegundos = (expira != null && expira > 0) ? expira : DEFAULT_VIDA_SEGUNDOS;
            String timestamp = snapshot.getString("timestamp_aprobacion");

            long restantes = calcularSegundosRestantes(timestamp);
            if (restantes > 0) {
                entrarEstadoActivo(restantes);
            } else {
                entrarEstadoExpirado();
            }
        }, e -> Snackbar.make(root, "Error al leer tu perfil: " + e.getMessage(),
                Snackbar.LENGTH_LONG).show());
    }

    private long calcularSegundosRestantes(String timestampAprobacion) {
        if (TextUtils.isEmpty(timestampAprobacion)) {
            return 0;
        }
        try {
            LocalDateTime aprobacion = LocalDateTime.parse(timestampAprobacion);
            long transcurridos = Duration.between(aprobacion, LocalDateTime.now()).getSeconds();
            return vidaSegundos - transcurridos;
        } catch (Exception e) {
            return 0;
        }
    }

    private void entrarEstadoActivo(long segundosRestantes) {
        aplicarColoresActivo();
        chipEstado.setText(R.string.chip_en_gracia);
        tvTitulo.setText(R.string.estado_activo_titulo);
        tvSubtitulo.setText(R.string.estado_activo_subtitulo);
        btnNuevoDesbloqueo.setVisibility(View.GONE);

        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(segundosRestantes * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long s = millisUntilFinished / 1000L;
                tvContador.setText(s + "s");
            }

            @Override
            public void onFinish() {
                entrarEstadoExpirado();
            }
        }.start();
    }

    private void entrarEstadoExpirado() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        aplicarColoresExpirado();
        chipEstado.setText(R.string.chip_expirado);
        tvContador.setText("00s");
        tvTitulo.setText(R.string.estado_expirado_titulo);
        tvSubtitulo.setText(R.string.estado_expirado_subtitulo);
        btnNuevoDesbloqueo.setVisibility(View.VISIBLE);
    }

    private void aplicarColoresActivo() {
        int fg = color(R.color.estado_activo_fg);
        cardEstado.setStrokeColor(color(R.color.estado_activo_stroke));
        cardEstado.setCardBackgroundColor(color(R.color.estado_activo_bg));
        chipEstado.setChipBackgroundColor(ColorStateList.valueOf(color(R.color.estado_activo_fg)));
        tvContador.setTextColor(fg);
        tvTitulo.setTextColor(fg);
    }

    private void aplicarColoresExpirado() {
        int fg = color(R.color.estado_expirado_fg);
        cardEstado.setStrokeColor(color(R.color.estado_expirado_stroke));
        cardEstado.setCardBackgroundColor(color(R.color.estado_expirado_bg));
        chipEstado.setChipBackgroundColor(ColorStateList.valueOf(color(R.color.estado_expirado_fg)));
        tvContador.setTextColor(fg);
        tvTitulo.setTextColor(fg);
    }

    private int color(int res) {
        return ContextCompat.getColor(this, res);
    }

    // ---------------------------------------------------------------------
    // Re-energización
    // ---------------------------------------------------------------------

    private void mostrarDialogoPin() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pin, null);
        TextInputLayout til = dialogView.findViewById(R.id.til_dialog_pin);
        TextInputEditText etPin = dialogView.findViewById(R.id.et_dialog_pin);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_pin_titulo)
                .setView(dialogView)
                .setNegativeButton(R.string.dialog_cancelar, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.dialog_confirmar, null)
                .create();

        dialog.show();
        // Validamos en un listener propio para no cerrar el diálogo si el PIN es inválido.
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String pin = etPin.getText() != null ? etPin.getText().toString().trim() : "";
                    if (pin.length() != 4) {
                        til.setError("El PIN debe tener 4 dígitos");
                        return;
                    }
                    dialog.dismiss();
                    reSolicitarDesbloqueo(pin);
                });
    }

    private void reSolicitarDesbloqueo(String pin) {
        Snackbar.make(root, "Re-energizando candado…", Snackbar.LENGTH_SHORT).show();
        executor.execute(() -> {
            try {
                Response<DesbloqueoResponse> response = ApiClient.getBiciApi()
                        .solicitarDesbloqueo(new SolicitudDesbloqueoRequest(codigoPucp, pin))
                        .execute();

                if (response.isSuccessful() && response.body() != null) {
                    DesbloqueoResponse data = response.body();
                    runOnUiThread(() -> persistirYReiniciar(data));
                } else {
                    final String mensaje = extraerMensajeError(response);
                    runOnUiThread(() -> Snackbar.make(root, mensaje, Snackbar.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Snackbar.make(root,
                        "No se pudo conectar con el servidor.", Snackbar.LENGTH_LONG).show());
            }
        });
    }

    private void persistirYReiniciar(DesbloqueoResponse data) {
        vidaSegundos = data.getDesbloqueoExpiraEn() > 0
                ? data.getDesbloqueoExpiraEn() : DEFAULT_VIDA_SEGUNDOS;
        repo.actualizarTimestamp(uid, data.getTimestampAprobacion(), data.getIotAuthToken(),
                data.getDesbloqueoExpiraEn(), task -> {
                    if (task.isSuccessful()) {
                        entrarEstadoActivo(vidaSegundos);
                        Snackbar.make(root, "Candado re-energizado", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(root, "Aprobado, pero no se pudo actualizar Firestore.",
                                Snackbar.LENGTH_LONG).show();
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

    // ---------------------------------------------------------------------
    // Menú
    // ---------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_carne) {
            startActivity(new Intent(this, CarneIoTActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            AuthService.cerrarSesion();
            irALogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void irALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        executor.shutdown();
    }
}
