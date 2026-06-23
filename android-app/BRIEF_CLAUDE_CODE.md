# BRIEF: Laboratorio IoT — App Android BiciPUCP

## Contexto del proyecto

Soy estudiante PUCP. Estoy desarrollando el Laboratorio 7 del curso de IoT. El tema es **BiciPUCP**: un sistema de bicicletas eléctricas con candados IoT para el campus universitario.

La app móvil que vas a construir es el cliente Android. El backend Spring Boot **ya está implementado y corriendo localmente** (no necesitas tocarlo). Solo necesito que construyas la app Android.

**Identidad del estudiante (úsala como datos por defecto cuando sea necesario):**
- Nombre: Jair Aguilera Inca
- Código PUCP: 20223806
- Correo: jair.aguilera@pucp.edu.pe

## Stack técnico y restricciones

- **Lenguaje:** Java (NO Kotlin)
- **UI:** XML tradicional con ConstraintLayout (NO Jetpack Compose)
- **Componentes UI:** **Material Components for Android (Material Design 3)**. Usa `com.google.android.material.*` para todos los inputs, botones y cards.
- **Build:** Gradle Groovy (build.gradle, NO .kts)
- **minSdk:** 34, **targetSdk:** 36, **compileSdk:** 36
- **applicationId:** `com.example.bicipucp`
- **namespace:** `com.example.bicipucp`
- **Hilos:** usa `ExecutorService` con `Executors.newSingleThreadExecutor()` y `runOnUiThread()` para volver al hilo principal. NO uses AsyncTask (deprecado) ni corrutinas (es Java).
- **Dependencias ya instaladas en build.gradle** (no las dupliques):
  - Firebase BOM 34.15.0 + Auth + Firestore + Storage
  - Retrofit 2.11.0 + converter-gson
  - Glide 4.16.0
  - AppCompat, Material, ConstraintLayout, Activity

## Configuración Firebase (ya hecha)

- El archivo `google-services.json` ya está en `app/`.
- En la consola: Authentication con Email/Password habilitado, Firestore creado, Storage creado (plan Blaze).
- **No agregues `FirebaseApp.initializeApp()` manual**, el plugin `google-services` lo hace automáticamente.

## Estructura de Firestore

Una sola colección: `usuarios`
- **ID del documento:** UID de Firebase Auth (NO el código PUCP)
- **Campos:**
  - `nombre_completo` (String)
  - `correo` (String)
  - `codigo_pucp` (String, 8 dígitos)
  - `pin_candado` (String, 4 dígitos)
  - `foto_url` (String, vacío al inicio)
  - `iot_auth_token` (String, lo devuelve el backend)
  - `desbloqueo_expira_en` (Number, lo devuelve el backend)
  - `timestamp_aprobacion` (String en formato ISO LocalDateTime, lo devuelve el backend)

## Backend Spring Boot (NO lo modificas, solo lo consumes)

URL base del backend (emulador Android Studio): `http://10.0.2.2:8080/`

> Importante: crea una clase `ApiConfig.java` con `public static final String BASE_URL = "http://10.0.2.2:8080/";` para centralizar esto. Cuando yo quiera cambiar a celular físico, modifico solo esa constante.

**Único endpoint que la app consume:**

```
POST http://10.0.2.2:8080/bici/solicitar-desbloqueo
Content-Type: application/json

Body de petición:
{
  "codigo": "20230145",
  "pin": "1234"
}

Respuesta 200 OK (aprobado):
{
  "status": "APROBADO",
  "iot_auth_token": "PUCP-BIKE-7f4a9b21",
  "desbloqueo_expira_en": 120,
  "timestamp_aprobacion": "2026-06-23T21:30:15"
}

Respuesta 400 Bad Request (rechazado):
{
  "mensaje": "El código de alumno no existe en la base de datos"
}
```

Usa **Retrofit** para esta llamada.

## AndroidManifest

El Manifest YA tiene:
- `<uses-permission android:name="android.permission.INTERNET" />`
- `<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />`
- `android:networkSecurityConfig="@xml/network_security_config"`
- `android:usesCleartextTraffic="true"`

Y existe `res/xml/network_security_config.xml` con cleartext permitido. **No los modifiques.**

Cada nueva Activity que crees, agrégala al `<application>` con `android:exported="false"`. La única Activity con `android:exported="true"` y filtro LAUNCHER debe ser **LoginActivity**.

## Estructura de carpetas

```
app/src/main/java/com/example/bicipucp/
├── MainActivity.java
├── LoginActivity.java
├── RegistroActivity.java
├── CarneIoTActivity.java
├── auth/
│   └── AuthService.java
├── data/
│   ├── ApiConfig.java
│   ├── ApiClient.java
│   ├── BiciApi.java
│   ├── SolicitudDesbloqueoRequest.java
│   ├── DesbloqueoResponse.java
│   ├── ErrorResponse.java
│   └── UsuarioRepository.java
└── model/
    └── Usuario.java
```

---

# Guía de diseño UX/UI

**Importante**: No copies literalmente las maquetas del enunciado del lab (riesgo de copia). Inspírate en ellas pero crea un diseño propio, limpio y moderno, basado en **Material Design 3**.

## Sistema de colores (define en `res/values/colors.xml`)

```xml
<!-- Marca / brand -->
<color name="brand_primary">#1E3A8A</color>        <!-- azul corporativo, distinto al de la maqueta -->
<color name="brand_primary_dark">#152A66</color>
<color name="brand_accent">#F97316</color>         <!-- naranja vibrante para acciones secundarias -->

<!-- Estado: ACTIVO (gracia) -->
<color name="estado_activo_fg">#047857</color>     <!-- verde esmeralda, distinto al maqueta -->
<color name="estado_activo_bg">#D1FAE5</color>
<color name="estado_activo_stroke">#10B981</color>

<!-- Estado: EXPIRADO -->
<color name="estado_expirado_fg">#B91C1C</color>   <!-- rojo profundo -->
<color name="estado_expirado_bg">#FEE2E2</color>
<color name="estado_expirado_stroke">#EF4444</color>

<!-- Neutros -->
<color name="surface">#FFFFFF</color>
<color name="surface_alt">#F9FAFB</color>
<color name="text_primary">#111827</color>
<color name="text_secondary">#6B7280</color>
<color name="divider">#E5E7EB</color>
```

> **Los colores de los estados (verde/rojo) son intencionalmente distintos a los del PDF**, manteniendo la semántica (verde = activo, rojo = expirado) pero con tonos más modernos (esmeralda en lugar de verde clásico, rojo borgoña en lugar de rojo bombero).

## Tema base (`res/values/themes.xml`)

Usa `Theme.Material3.DayNight.NoActionBar` como parent y aplica los `colorPrimary` y `colorSecondary` con `brand_primary` y `brand_accent`. Configura una `Toolbar` personalizada en cada activity en lugar del ActionBar nativo.

## Componentes Material Design a usar

| Elemento | Componente |
|---|---|
| Inputs de texto | `com.google.android.material.textfield.TextInputLayout` con estilo `Widget.Material3.TextInputLayout.OutlinedBox` + `TextInputEditText` interno |
| Botones primarios | `com.google.android.material.button.MaterialButton` (filled, `Widget.Material3.Button`) |
| Botones secundarios | `MaterialButton` estilo `Widget.Material3.Button.OutlinedButton` |
| Botones texto (links) | `MaterialButton` estilo `Widget.Material3.Button.TextButton` |
| Cards | `com.google.android.material.card.MaterialCardView` con `cardCornerRadius="16dp"` y `strokeWidth="1dp"` |
| Toolbar | `com.google.android.material.appbar.MaterialToolbar` |
| Progress | `com.google.android.material.progressindicator.CircularProgressIndicator` (indeterminado) |
| Snackbar | `com.google.android.material.snackbar.Snackbar` |
| Diálogos | `com.google.android.material.dialog.MaterialAlertDialogBuilder` |
| Chip de estado | `com.google.android.material.chip.Chip` |

## Reglas tipográficas

- Usa los `textAppearance` de Material 3:
  - Títulos grandes de pantalla: `?attr/textAppearanceHeadlineSmall`
  - Subtítulos: `?attr/textAppearanceTitleMedium`
  - Body: `?attr/textAppearanceBodyMedium`
  - Helper/labels: `?attr/textAppearanceLabelMedium`
- El número de la cuenta regresiva en MainActivity debe ser **muy grande** (entre 64sp y 96sp), bold, centrado, con `fontFeatureSettings="tnum"` (números tabulares para que no "salten" al cambiar).

## Espaciado consistente

- Padding lateral de pantalla: `24dp`.
- Separación vertical entre componentes: `16dp` (estándar), `24dp` (entre secciones).
- Padding interno de cards: `24dp`.
- Esquinas: `16dp` para cards, `12dp` para botones, `8dp` para inputs.

---

# Especificación funcional

## PANTALLA 1: LoginActivity (pantalla de inicio)

Es la primera pantalla que se abre cuando se lanza la app.

### UI

- `MaterialToolbar` superior con título "BiciPUCP" y fondo `brand_primary`, texto blanco.
- Contenido scrolleable centrado:
  - `TextView` headline: "Bienvenido"
  - `TextView` subtitle: "Inicia sesión con tu cuenta PUCP"
  - `TextInputLayout` "Correo PUCP" → `TextInputEditText` con `inputType="textEmailAddress"`.
  - `TextInputLayout` "Contraseña" → `TextInputEditText` con `inputType="textPassword"` y `endIconMode="password_toggle"`.
  - `MaterialButton` filled, ancho completo, texto "INGRESAR".
  - `CircularProgressIndicator` (inicialmente `visibility="gone"`).
  - `MaterialButton` text style: "¿No tienes cuenta? Regístrate" → abre RegistroActivity.

### Lógica

- Al pulsar INGRESAR:
  1. Validar que ambos campos no estén vacíos (mostrar error en el `TextInputLayout` con `setError(...)`).
  2. Mostrar progress, deshabilitar botón.
  3. Llamar a `FirebaseAuth.signInWithEmailAndPassword(...)`.
  4. Si éxito → `startActivity(MainActivity)` + `finish()`.
  5. Si falla → Snackbar con mensaje, ocultar progress, rehabilitar botón.
- **Al abrir LoginActivity:** si `FirebaseAuth.getInstance().getCurrentUser() != null`, saltar directo a MainActivity sin mostrar el formulario.

## PANTALLA 2: RegistroActivity

### UI

- `MaterialToolbar` con flecha atrás y título "Crear cuenta".
- `ScrollView` para que entre en pantallas pequeñas.
- `MaterialCardView` con padding 24dp que contenga:
  - `TextView` título: "Afiliación de Campus IoT"
  - `TextView` subtítulo: "Ingresa tus credenciales de alumno"
  - 5 `TextInputLayout` outlined consecutivos, separados 12dp:
    - Nombre Completo (`inputType="textPersonName|textCapWords"`)
    - Correo PUCP (`inputType="textEmailAddress"`)
    - Contraseña (`inputType="textPassword"`, `endIconMode="password_toggle"`)
    - Código PUCP (`inputType="number"`, `helperText="8 dígitos"`, `counterEnabled="true"`, `counterMaxLength="8"`)
    - PIN Candado IoT (`inputType="numberPassword"`, `helperText="4 dígitos"`, `counterEnabled="true"`, `counterMaxLength="4"`, `endIconMode="password_toggle"`)
  - Bloque de progreso (inicialmente gone):
    - `CircularProgressIndicator` pequeño + `TextView` "Validando en Spring Boot..."
  - `MaterialButton` filled ancho completo, texto "VALIDAR Y REGISTRAR"

### Lógica

Al pulsar VALIDAR Y REGISTRAR:

1. Validar localmente:
   - Todos los campos no vacíos
   - Código exactamente 8 dígitos
   - PIN exactamente 4 dígitos
   - Si falla → mostrar error en el `TextInputLayout` correspondiente. NO envíes nada al backend.

2. Deshabilitar botón, mostrar bloque de progreso.

3. En un `ExecutorService`:
   - POST con Retrofit a `http://10.0.2.2:8080/bici/solicitar-desbloqueo` con `{"codigo": ..., "pin": ...}`.

4. Manejo de respuesta (con `runOnUiThread`):

   - **Si HTTP 200 OK:**
     a) `FirebaseAuth.createUserWithEmailAndPassword(...)`.
     b) Obtener el UID.
     c) Guardar en Firestore `usuarios/{uid}` con TODOS los campos (nombre, correo, codigo, pin, foto_url="", iot_auth_token, desbloqueo_expira_en, timestamp_aprobacion).
     d) Ocultar progress, Snackbar "Registro exitoso", `startActivity(MainActivity)` + `finish()`.

   - **Si HTTP 400 o falla de red:**
     a) Parsear JSON de error y extraer atributo `mensaje`.
     b) Ocultar progress, rehabilitar botón.
     c) **NO** crear nada en Firebase Auth ni Firestore.
     d) Snackbar con el mensaje **exacto** del servidor.

## PANTALLA 3: MainActivity (centro de control)

Pantalla más compleja. Máquina de estados visual basada en tiempo.

### UI

- `MaterialToolbar` con título "BiciPUCP" y menú overflow (3 puntos), fondo `brand_primary`.
- Pantalla con padding 24dp.
- `MaterialCardView` central con:
  - `strokeWidth="2dp"` y `strokeColor` dinámico (verde o rojo).
  - `cardBackgroundColor` dinámico (verde claro o rojo claro).
  - `cardCornerRadius="20dp"`.
  - Contenido:
    - `Chip` arriba: texto "EN GRACIA" o "EXPIRADO", color de fondo coherente.
    - `TextView` enorme: cuenta regresiva con formato `"XXs"`, tamaño 80sp, bold, color `estado_activo_fg` o `estado_expirado_fg`.
    - `TextView` headline pequeña: "Candado IoT energizado" / "Candado vuelto a trabar".
    - `TextView` subtítulo: "Retire la bicicleta de la estación" / "Tiempo de gracia expirado".
    - `MaterialButton` filled, texto "SOLICITAR NUEVO DESBLOQUEO", visible solo en estado EXPIRADO.

### Menú overflow

- Crea `res/menu/main_menu.xml` con un solo item:
  - id: `action_carne`, title: "Mi Carné IoT", icon opcional `ic_account`.
- En `onCreateOptionsMenu` infla el menú.
- En `onOptionsItemSelected`: si es `action_carne`, abrir `CarneIoTActivity`.
- También agrega un item "Cerrar sesión" (id: `action_logout`) que llame a `FirebaseAuth.signOut()` y vuelva a LoginActivity.

### Lógica de la máquina de estados

1. **`onCreate`:**
   - Obtener UID del usuario actual. Si null → ir a Login.
   - Leer documento `usuarios/{uid}` de Firestore.
   - Parsear `timestamp_aprobacion` (String ISO) a `LocalDateTime` (`java.time.*`, requiere `compileSdk` ≥ 26; sí lo tienes).
   - Calcular `segundos_restantes = 120 - secondsBetween(timestamp_aprobacion, ahora)`.

2. **Si `segundos_restantes > 0`:** entrar a **Estado Activo**:
   - Aplicar estilos verdes a la card (fondo, stroke, chip).
   - Texto de chip: "EN GRACIA".
   - Iniciar `CountDownTimer(segundos_restantes * 1000, 1000)`:
     - `onTick`: actualizar TextView con `(millisUntilFinished/1000) + "s"`.
     - `onFinish`: llamar a `entrarEstadoExpirado()`.
   - Botón [SOLICITAR NUEVO DESBLOQUEO]: `visibility="gone"`.

3. **Si `segundos_restantes <= 0`:** entrar a **Estado Expirado**:
   - Aplicar estilos rojos a la card.
   - Texto de chip: "EXPIRADO".
   - TextView muestra "00s".
   - Botón [SOLICITAR NUEVO DESBLOQUEO]: `visibility="visible"`.

4. **Botón SOLICITAR NUEVO DESBLOQUEO:**
   - `MaterialAlertDialogBuilder` con título "Confirma tu PIN" y `TextInputLayout` interno con un `TextInputEditText` (`inputType="numberPassword"`, maxLength=4).
   - Botón "Confirmar":
     a) Toma el código PUCP guardado en Firestore + el PIN ingresado.
     b) Mostrar progress.
     c) POST a `/bici/solicitar-desbloqueo`.
     d) Si 200 OK: actualizar Firestore con nuevo `timestamp_aprobacion`, `iot_auth_token` y `desbloqueo_expira_en`. Reiniciar máquina de estados (volver a Activo con 120s).
     e) Si 400: Snackbar con mensaje del servidor.
   - Botón "Cancelar": cierra diálogo.

5. **Lifecycle:** guarda la referencia del `CountDownTimer` y cancélalo en `onDestroy()` para evitar leaks.

## PANTALLA 4: CarneIoTActivity (Mi Carné IoT)

### UI

- `MaterialToolbar` con flecha atrás y título "Mi Carné IoT", fondo `brand_primary`.
- Pantalla con padding 24dp.
- `MaterialCardView` "credencial":
  - `cardCornerRadius="20dp"`, `strokeWidth="1dp"`, `strokeColor="@color/divider"`.
  - Header del card con fondo `brand_primary` (puede ser otro `View` o un layout con elevación 0):
    - `TextView` blanco centrado: "CREDENCIAL VIRTUAL PUCP".
  - Cuerpo del card:
    - `ShapeableImageView` circular de 140dp (con `shapeAppearance` circular) para la foto del usuario.
    - `MaterialButton` con `app:icon="@drawable/ic_photo_camera"`, texto "SUBIR FOTO", color `brand_accent`, ancho wrap, debajo de la imagen.
    - Separador (`View` con `divider` de 1dp).
    - Sección de datos en dos filas con `TextView` label arriba (gris pequeño "ALUMNO") y valor abajo (negro mediano).
    - Sección código con `Chip` verde "HABILITADO" al costado.
- `MaterialCardView` secundaria abajo:
  - `TextView` label: "URL Firebase Storage (DB):".
  - `TextView` con `textIsSelectable="true"` y `singleLine="false"` mostrando la URL completa.
- `CircularProgressIndicator` (inicialmente gone) overlay arriba del card.

### Lógica

1. **`onCreate`:**
   - Obtener UID. Si null → ir a Login.
   - Leer `usuarios/{uid}` de Firestore.
   - Llenar `nombre_completo` y `codigo_pucp` en los TextViews.
   - Si `foto_url` no vacío: cargar con Glide en el ShapeableImageView y mostrar URL en el TextView. Usa `placeholder` y `error` para Glide.
   - Si `foto_url` vacío: dejar drawable placeholder genérico (`ic_person_placeholder`).

2. **Botón SUBIR FOTO:**
   - `ActivityResultLauncher` con `ActivityResultContracts.PickVisualMedia()` (API moderna para galería, perfecto con minSdk 34).
   - Al seleccionar:
     a) Mostrar progress, deshabilitar botón.
     b) Comprimir: leer `Uri` → `Bitmap`, redimensionar a max 1024px en el lado más largo, JPEG quality 75 → `byte[]`.
     c) Subir a Firebase Storage en ruta EXACTA: `credenciales_bicipucp/{uid}.jpg`.
     d) Obtener URL con `getDownloadUrl()`.
     e) Cuando llegue:
        - Toast con la URL.
        - Actualizar `foto_url` en Firestore.
        - Pintar imagen en el ShapeableImageView con Glide.
        - Mostrar URL en el TextView.
        - Ocultar progress, rehabilitar botón.
     f) Si falla: Snackbar con error, ocultar progress.

3. **Persistencia (P2 punto 5):** el paso 1 ya cubre esto. Al reabrir, lee `foto_url` y descarga.

4. **Flecha atrás:** vuelve a MainActivity.

## Clase `AuthService`

Centralizadora, métodos estáticos o singleton:
- `registrarUsuario(email, password, OnCompleteListener)`
- `iniciarSesion(email, password, OnCompleteListener)`
- `cerrarSesion()`
- `getUidActual()`
- `estaLogueado()`

## Clase `UsuarioRepository`

Centraliza operaciones de Firestore y Storage:
- `guardarUsuario(Usuario, OnCompleteListener)`
- `obtenerUsuario(uid, OnSuccessListener)`
- `actualizarTimestamp(uid, timestamp, token, expira_en, OnCompleteListener)`
- `actualizarFotoUrl(uid, url, OnCompleteListener)`
- `subirFoto(uid, byte[], OnSuccessListener<Uri>)` (devuelve la URL pública)

---

# Orden sugerido de implementación

1. `ApiConfig`, `ApiClient`, `BiciApi`, DTOs (`SolicitudDesbloqueoRequest`, `DesbloqueoResponse`, `ErrorResponse`).
2. `Usuario.java`, `UsuarioRepository`, `AuthService`.
3. Sistema de colores y theme (Material 3).
4. `LoginActivity` (XML + Java).
5. `RegistroActivity` (XML + Java).
6. `MainActivity` (XML + Java, con máquina de estados).
7. `menu/main_menu.xml`.
8. `CarneIoTActivity` (XML + Java).
9. Verificar `AndroidManifest` con las 4 Activities, LoginActivity como LAUNCHER, las otras con `exported="false"`.

# Manejo de errores y casos borde

- Toda llamada de red con try/catch y manejo de `IOException`.
- Toda escritura a Firestore/Storage con `addOnFailureListener` + Snackbar.
- Si en MainActivity el documento del usuario no existe en Firestore, redirigir a LoginActivity con mensaje.
- Validar `getCurrentUser() != null` al inicio de MainActivity y CarneIoTActivity; si null, redirigir a Login.

# Lo que NO debes hacer

- NO modifiques `build.gradle` (ya está configurado).
- NO modifiques `AndroidManifest.xml` salvo para agregar las nuevas Activities.
- NO agregues bibliotecas adicionales sin avisarme primero.
- NO uses Kotlin, Compose ni Material 3 experimental.
- NO uses AsyncTask (deprecado).
- NO toques el `google-services.json`.
- NO toques los proyectos de Spring Boot que están en carpetas hermanas a esta.
- NO copies literalmente las maquetas del enunciado del lab; toma la inspiración pero diseña con tu propio criterio Material 3 y los colores definidos aquí.

---

**Empieza por el paso 1 del orden sugerido. Confírmame cuando termines cada paso antes de pasar al siguiente, para que yo pueda probar el flujo incrementalmente.**
