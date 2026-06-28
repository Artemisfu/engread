#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export ANDROID_HOME="$ROOT_DIR/.local-toolchain/android-sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"

APK="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"

if [[ ! -f "$APK" ]]; then
  "$ROOT_DIR/scripts/build_debug.sh"
fi

adb start-server >/dev/null
DEVICES="$(adb devices | sed '1d' | grep -E '[[:space:]]device$' || true)"

if [[ -z "$DEVICES" ]]; then
  echo "没有检测到已授权的 Android 设备。"
  echo "请用 USB 连接手机，打开开发者选项和 USB 调试，并在手机上点允许。"
  adb devices -l
  exit 1
fi

adb install -r "$APK"
