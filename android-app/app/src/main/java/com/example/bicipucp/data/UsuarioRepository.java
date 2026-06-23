package com.example.bicipucp.data;

import androidx.annotation.NonNull;

import com.example.bicipucp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

/**
 * Centraliza las operaciones de Firestore (perfil del usuario) y de
 * Firebase Storage (foto de la credencial).
 */
public final class UsuarioRepository {

    private static final String COLECCION = "usuarios";
    private static final String CARPETA_FOTOS = "credenciales_bicipucp";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private DocumentReference docUsuario(String uid) {
        return db.collection(COLECCION).document(uid);
    }

    /** Crea o reemplaza el documento del usuario. */
    public void guardarUsuario(String uid, Usuario usuario,
                               @NonNull OnCompleteListener<Void> listener) {
        docUsuario(uid).set(usuario).addOnCompleteListener(listener);
    }

    /** Lee el documento del usuario. */
    public void obtenerUsuario(String uid, @NonNull OnSuccessListener<DocumentSnapshot> onSuccess,
                               @NonNull OnFailureListener onFailure) {
        docUsuario(uid).get().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    /** Actualiza los datos devueltos por el orquestador tras un nuevo desbloqueo. */
    public void actualizarTimestamp(String uid, String timestamp, String token,
                                    long expiraEn, @NonNull OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("timestamp_aprobacion", timestamp);
        updates.put("iot_auth_token", token);
        updates.put("desbloqueo_expira_en", expiraEn);
        docUsuario(uid).update(updates).addOnCompleteListener(listener);
    }

    /** Actualiza únicamente la URL de la foto de credencial. */
    public void actualizarFotoUrl(String uid, String url,
                                  @NonNull OnCompleteListener<Void> listener) {
        docUsuario(uid).update("foto_url", url).addOnCompleteListener(listener);
    }

    /**
     * Sube la imagen (ya comprimida) a Storage en la ruta exacta
     * {@code credenciales_bicipucp/<uid>.jpg} y devuelve la URL pública de descarga.
     */
    public void subirFoto(String uid, byte[] jpeg,
                          @NonNull OnSuccessListener<Uri> onSuccess,
                          @NonNull OnFailureListener onFailure) {
        StorageReference ref = storage.getReference()
                .child(CARPETA_FOTOS + "/" + uid + ".jpg");
        ref.putBytes(jpeg)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
