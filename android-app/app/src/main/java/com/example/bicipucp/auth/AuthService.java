package com.example.bicipucp.auth;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Capa de seguridad de la app. Centraliza el acceso a Firebase Authentication.
 */
public final class AuthService {

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    private AuthService() {
    }

    public static void registrarUsuario(String email, String password,
                                        @NonNull OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public static void iniciarSesion(String email, String password,
                                     @NonNull OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public static void cerrarSesion() {
        auth.signOut();
    }

    public static String getUidActual() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static boolean estaLogueado() {
        return auth.getCurrentUser() != null;
    }
}
