package com.example.bicipucp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bicipucp.auth.AuthService;
import com.example.bicipucp.data.UsuarioRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarneIoTActivity extends AppCompatActivity {

    private static final int MAX_LADO = 1024;
    private static final int JPEG_QUALITY = 75;

    private View root;
    private ShapeableImageView imgFoto;
    private TextView tvNombre, tvCodigo, tvUrl;
    private MaterialButton btnSubir;
    private CircularProgressIndicator progress;

    private final UsuarioRepository repo = new UsuarioRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private String uid;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    procesarYSubir(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uid = AuthService.getUidActual();
        if (uid == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_carne);

        root = findViewById(R.id.carne_root);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        imgFoto = findViewById(R.id.img_foto);
        tvNombre = findViewById(R.id.tv_nombre);
        tvCodigo = findViewById(R.id.tv_codigo);
        tvUrl = findViewById(R.id.tv_url);
        btnSubir = findViewById(R.id.btn_subir_foto);
        progress = findViewById(R.id.progress);

        btnSubir.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        cargarPerfil();
    }

    private void cargarPerfil() {
        repo.obtenerUsuario(uid, snapshot -> {
            if (!snapshot.exists()) {
                Snackbar.make(root, "No se encontró tu perfil.", Snackbar.LENGTH_LONG).show();
                return;
            }
            tvNombre.setText(snapshot.getString("nombre_completo"));
            tvCodigo.setText(snapshot.getString("codigo_pucp"));

            String fotoUrl = snapshot.getString("foto_url");
            if (!TextUtils.isEmpty(fotoUrl)) {
                pintarFoto(fotoUrl);
                tvUrl.setText(fotoUrl);
            } else {
                tvUrl.setText(R.string.carne_url_vacia);
            }
        }, e -> Snackbar.make(root, "Error al leer perfil: " + e.getMessage(),
                Snackbar.LENGTH_LONG).show());
    }

    private void pintarFoto(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(imgFoto);
    }

    private void procesarYSubir(Uri uri) {
        mostrarCarga(true);
        executor.execute(() -> {
            try {
                byte[] jpeg = comprimir(uri);
                runOnUiThread(() -> subirAStorage(jpeg));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    mostrarCarga(false);
                    Snackbar.make(root, "No se pudo procesar la imagen.",
                            Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    /** Lee la imagen, la redimensiona a máx. 1024px en el lado mayor y la comprime a JPEG. */
    private byte[] comprimir(Uri uri) throws Exception {
        Bitmap original;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            original = BitmapFactory.decodeStream(in);
        }
        if (original == null) {
            throw new IllegalStateException("Bitmap nulo");
        }

        int ancho = original.getWidth();
        int alto = original.getHeight();
        int ladoMayor = Math.max(ancho, alto);

        Bitmap escalado = original;
        if (ladoMayor > MAX_LADO) {
            float escala = (float) MAX_LADO / ladoMayor;
            escalado = Bitmap.createScaledBitmap(original,
                    Math.round(ancho * escala), Math.round(alto * escala), true);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        escalado.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        return out.toByteArray();
    }

    private void subirAStorage(byte[] jpeg) {
        repo.subirFoto(uid, jpeg, downloadUri -> {
            String url = downloadUri.toString();
            Toast.makeText(this, url, Toast.LENGTH_LONG).show();
            repo.actualizarFotoUrl(uid, url, task -> {
                mostrarCarga(false);
                if (task.isSuccessful()) {
                    pintarFoto(url);
                    tvUrl.setText(url);
                    Snackbar.make(root, "Foto actualizada", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(root, "Foto subida, pero no se guardó la URL.",
                            Snackbar.LENGTH_LONG).show();
                }
            });
        }, e -> {
            mostrarCarga(false);
            Snackbar.make(root, "Error al subir la foto: " + e.getMessage(),
                    Snackbar.LENGTH_LONG).show();
        });
    }

    private void mostrarCarga(boolean cargando) {
        progress.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnSubir.setEnabled(!cargando);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
