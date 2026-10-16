# Keyva — teclado Android con dictado y corrección opcional

**Beta 1.0.0 en desarrollo. Todavía no hay una release verificada ni APK de beta publicado.**

[Compilaciones y APK de desarrollo (Actions)](https://github.com/nexlibre/Keyva/actions/workflows/keyva-ci.yml) · [Releases](https://github.com/nexlibre/Keyva/releases) · [Pruebas y pendientes](docs/VALIDATION.md)

Derivado independiente de [HeliBoard](https://github.com/HeliBorg/HeliBoard), con historial, licencia GPL-3.0 y créditos conservados. Android mínimo: **12 (API 31)**. Identidad: `org.nexlibre.keyva`. No modifica el proyecto original ni incorpora código de SwiftKey/Typeless.

## Implementación inicial
- Teclado, diccionarios, sugerencias, idiomas, portapapeles, emojis, temas y personalización de HeliBoard conservados. No se incluye biblioteca propietaria de gestos.
- Barra Voz / IA / Deshacer / Ajustes IA. Los accesos tradicionales de HeliBoard continúan disponibles.
- Dictado Android: prefiere SpeechRecognizer local, si está disponible. El proveedor remoto requiere confirmación. Vista previa, terminar, cancelar e insertar; sin archivos de audio.
- Corrección solo por acción explícita sobre texto seleccionado. Cinco modos, respeto al voseo y tono informal; instrucciones y contenido separados en la solicitud oficial Gemini REST.
- Campo sensible/privado bloqueado antes de leer o enviar texto; resultados ligados a la conexión y sesión. Vista previa y revisión humana son necesarias: la IA puede equivocarse o seguir instrucciones maliciosas pese a las defensas.
- Clave AES-GCM con Android Keystore en almacenamiento de credenciales; backup desactivado, sin claves en código ni logs del módulo. IA desactivada por defecto.
- Red fuera del hilo principal, límites de longitud/tiempo, cancelación, errores de clave/cuota y conservación del original.

## Configurar desde Android
1. Instalar una compilación disponible y activar Keyva como teclado en ajustes de Android. Los APK de desarrollo usan firma de depuración, no una firma estable de publicación.
2. Abrir un campo de texto y pulsar **Ajustes IA**. Revocar la clave publicada anteriormente y crear una nueva en [Google AI Studio](https://aistudio.google.com/api-keys), usando un proyecto sin facturación.
3. Introducir la nueva clave únicamente en la app, elegir un modelo disponible en la cuota gratuita de tu cuenta y habilitar Gemini. Guardar y probar conexión; la prueba envía solo «hola».
4. Seleccionar el texto y pulsar IA. Solo **Corregir con Gemini** envía la vista previa. Revisar antes de insertar. Deshacer restaura el último reemplazo si el campo no cambió.
5. Para dictado, permitir micrófono desde Ajustes IA, volver al campo y pulsar Voz. El idioma inicial es es-NI; la disponibilidad local depende del dispositivo y paquetes instalados.

La escritura convencional funciona sin internet. Esta variante añade permisos INTERNET y RECORD_AUDIO: **la garantía de HeliBoard de no tener permiso de internet ya no aplica a Keyva**. El reconocimiento remoto usa el proveedor de Android; Gemini recibe solo texto confirmado. El nivel gratuito de Google puede usar contenido para mejorar productos; evitá datos privados. Cuotas, modelos y disponibilidad cambian. Keyva no activa facturación, pero tampoco puede impedir cargos si se usa una clave de un proyecto con facturación. Consultar [precios](https://ai.google.dev/gemini-api/docs/pricing) y [límites oficiales](https://ai.google.dev/gemini-api/docs/rate-limits).

## Estado y licencia
La compilación no demuestra que el dictado, Gemini o todas las funciones tradicionales funcionen en un teléfono. [VALIDATION.md](docs/VALIDATION.md) separa automatización y pruebas pendientes. La release Beta 1.0.0 está retenida hasta completar la aceptación. Pendientes: prueba real con clave nueva, micrófono, regresiones de teclado y firma estable de publicación. Enviar audio directamente a Gemini y un tema Liquid Glass se posponen.

Ver [README original y créditos](docs/UPSTREAM_README.md), [GPL-3.0](LICENSE), [Apache-2.0](LICENSE-Apache-2.0) y [CC-BY-SA-4.0](LICENSE-CC-BY-SA-4.0). Las modificaciones Keyva se desarrollaron con asistencia de IA; no se enviaron contribuciones automáticas al proyecto original.
