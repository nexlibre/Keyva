# Comprobación real desde Android

Estas comprobaciones completan las que no puede sustituir el emulador. No requieren computadora. Usá frases de prueba sin datos personales. No enviés claves por chat.

1. **Dictado real.** Activá Keyva, abrí un campo de texto normal y pulsá Voz. Permití el micrófono desde Ajustes IA si hace falta. Decí: «vos tenés tiempo mañana para hablar conmigo». Terminá, revisá la transcripción, insertala y probá Deshacer. Repetí una vez cancelando: el campo original debe quedar intacto.
2. **Idioma local.** Si es-NI no está disponible, elegí es-US en Ajustes IA o instalá el paquete de voz correspondiente. El proveedor en línea requiere confirmación; no debe activarse automáticamente tras un fallo local. Probá también con Wi-Fi/datos desactivados para comprobar si tu proveedor realmente ofrece reconocimiento local.
3. **Gemini real.** Revocá la clave expuesta anteriormente. Creá una nueva en https://aistudio.google.com/api-keys dentro de un proyecto sin facturación. Introducila únicamente en Ajustes IA, habilitá Gemini, guardá y pulsá Probar conexión. Anotá si aparece «Conexión correcta» o el error, sin incluir la clave.
4. **Calidad y reversión.** Escribí «vos tenes tiempo manana para hablar conmigo», seleccioná el texto y pulsá IA. Corregí, comprobá acentos y voseo y revisá que no invente datos ni cambie el tono. Editá algo en la vista previa antes de insertar; después pulsá Deshacer y comprobá que vuelve exactamente el texto anterior. Repetí cancelando y cambiando de app durante una solicitud.
5. **Teclado tradicional.** Probá letras, números, símbolos, español/inglés, sugerencias, autocorrección, portapapeles, emojis, altura y temas. Comprobá escritura en WhatsApp, Telegram, navegador y un editor, sin enviar los textos de prueba.
6. **Privacidad y errores.** En contraseñas/PIN, Voz e IA deben estar bloqueados. Con IA desactivada o sin internet debe poder escribirse normalmente. Probá cancelar una solicitud, borrar la clave y volver a escribir. No hace falta agotar la cuota: los errores HTTP 401/403/429 se cubren con respuestas simuladas en CI.

Para informar el resultado basta indicar versión de Android, si cada punto pasó, el mensaje de error si aparece y el modelo seleccionado. Nunca incluyás la clave. Un fallo deja la publicación pendiente; un resultado simulado no sustituye estas pruebas reales.

La firma permanente de publicación es una configuración separada: ver SIGNING.md. El APK de desarrollo usa una firma temporal y puede requerir desinstalar otro APK de desarrollo anterior; desinstalar elimina sus ajustes y la clave guardada. No es una actualización de producción con firma permanente.
