# EngRead 虚拟机测试报告

测试时间：2026-06-28

## 环境

- macOS ARM64
- 本地工具链：`.local-toolchain/`
- JDK：Temurin 17.0.19
- Gradle：8.9
- Android Emulator：36.6.11
- AVD：`EngRead_API35`
- 系统镜像：Android 15 / API 35 / Google APIs / arm64-v8a
- APK：`app/build/outputs/apk/debug/app-debug.apk`

## 结果

`./scripts/build_debug.sh` 构建成功，APK 安装到虚拟机成功。

端到端测试通过：

- 空书架页面显示正常。
- Android 文件选择器导入 TXT 成功。
- 阅读器显示 TXT 正文，按段落拆分，段首首字母视觉加粗大写。
- 段落翻译入口可用，能显示演示译文和正式翻译服务提示。
- 长按单词 `book` 能弹出底部词义卡，显示音标 `/bʊk/` 和释义“书；预订”。
- 阅读设置可打开，字体切换为 `SERIF`、主题切换为 `DARK` 后写入本地设置文件。
- 摘句弹窗可按句子拆分，勾选句子后保存笔记。
- 笔记本页面可显示书名、原句、段落位置和用户笔记。
- Markdown 导出成功，生成 `/sdcard/Download/engread-notes.md`。
- 应用重启后书架、书籍、笔记和进度数据仍存在。
- 基础 MOBI 导入路径通过：使用最小无 DRM MOBI 样本，解析出正文并进入阅读器，书籍类型为 `MOBI`。
- `adb logcat` 未发现 `FATAL EXCEPTION` 或 `E/AndroidRuntime` 崩溃日志。

## 测试文件

- TXT：`/sdcard/Download/engread_sample.txt`
- MOBI：`/sdcard/Download/engread_mobi_sample.mobi`
- 导出笔记：`/sdcard/Download/engread-notes.md`

## 未覆盖

- 真实商业 MOBI/复杂排版/DRM 文件。
- 在线翻译服务，因为当前版本只有演示翻译。
- 大文件压力测试。
- 横竖屏旋转和多窗口恢复。
