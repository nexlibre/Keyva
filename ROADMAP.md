# Keyva Beta 1.0.0 — hoja de ruta técnica

Base: HeliBorg/HeliBoard (GPL-3.0). Conservar atribución, licencia y código fuente. Repositorio de destino: nexlibre/Keyva. Android mínimo: API 31 (Android 12). Costo: cero; sin facturación.

## Orden de implementación
1. Importar íntegramente HeliBoard preservando fuentes, recursos, licencia y créditos; comprobar submódulos y dependencias; asignar identidad y applicationId propios sin perder funcionalidad.
2. Compilar con Gradle y publicar artefacto CI; validar IME, idiomas, diccionarios, portapapeles, temas y modo offline.
3. Implementar dictado con Android SpeechRecognizer (comprobar disponibilidad local y permiso RECORD_AUDIO); controles de iniciar, detener, cancelar y confirmar, sin retener audio.
4. Integrar módulo Gemini opt-in con clave introducida por el usuario, almacenamiento seguro, pruebas de conectividad, errores 401/403/429, cancelación y vista previa; nunca incluir secretos en APK ni GitHub. No enviar campos sensibles; prevenir filtración por logs.
5. Modos de corrección básica, completa, dictado y preservar estilo; respetar español nicaragüense y voseo; no inventar contenido; deshacer exactamente.
6. Barra de herramientas, contraste, AMOLED y accesibilidad; medir latencia, batería y estabilidad.
7. CI con compilación, pruebas unitarias, auditoría de dependencias y pruebas instrumentadas cuando haya emulador; documentar claramente qué se probó en dispositivo.
8. Solo tras validaciones, publicar release Beta 1.0.0 con APK, checksums, firma y enlace visible en README.

## Condiciones de seguridad
HeliBoard es offline y no solicita INTERNET: la variante Keyva debe explicar claramente el cambio de privacidad antes de añadir permiso de red. No incluir bibliotecas propietarias de escritura gestual sin permiso de redistribución. No subir API keys. La clave compartida anteriormente se considera comprometida y debe revocarse. No prometer cuota gratuita ilimitada.

## Criterio de publicación
APK instalable y teclado usable; dictado y corrección Gemini comprobados; modo sin conexión preservado; pruebas de campos sensibles y reversión superadas. Si faltan pruebas reales, no etiquetar como beta verificada.