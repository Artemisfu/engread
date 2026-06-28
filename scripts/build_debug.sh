#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export JAVA_HOME="$ROOT_DIR/.local-toolchain/jdk-17.0.19+10/Contents/Home"
export ANDROID_HOME="$ROOT_DIR/.local-toolchain/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ROOT_DIR/.local-toolchain/gradle-8.9/bin:$PATH"

cd "$ROOT_DIR"
gradle :app:assembleDebug
