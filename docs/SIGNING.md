# Publication signing

Development APKs use a debug certificate. They are installable but are not the permanent publication identity. No private signing key or Gemini key has been committed.

The prepared `Publish verified Keyva beta` workflow uses four encrypted GitHub Actions secrets:

- KEYVA_KEYSTORE_BASE64: base64 of the permanent Android keystore.
- KEYVA_STORE_PASSWORD: keystore password.
- KEYVA_KEY_ALIAS: signing alias.
- KEYVA_KEY_PASSWORD: private-key password.

The permanent keystore must be backed up privately and reused for every update. Never generate a disposable key on each CI run, publish the keystore as a public artifact, or put passwords in workflow source. Existing development installations signed with a debug certificate cannot be upgraded in place to a differently signed production APK.

The currently available GitHub connector does not expose secret provisioning. The workflow therefore refuses publication if these secrets are absent. This is a configuration requirement, not a claim that a permanent release key already exists or is configured.

The release workflow also checks the real-service acceptance record and a successful CI run for the accepted source commit. Only documentation may change after acceptance. It builds the optimized release, signs it, verifies its identity and non-debuggable flag, tests installation/default-IME activation, then publishes the APK, SHA256 and public signature information as a GitHub prerelease. No private material is uploaded to a release.
