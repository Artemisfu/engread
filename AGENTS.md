# AGENTS.md

## Android Testing

- Do not install testing builds over the production package.
- Production package: `com.engread.app`.
- Testing package: use the debug build with `applicationIdSuffix = ".test"`, package `com.engread.app.test`, and app label `EngRead Test`.
- For device testing, build/install the debug APK (`app/build/outputs/apk/debug/app-debug.apk`) unless the user explicitly asks for a production install.
- Release APKs may be built for packaging, but should not be installed during routine testing.
