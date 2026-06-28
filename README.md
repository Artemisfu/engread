# EngRead

EngRead 是一个 Kotlin + Jetpack Compose 的 Android 英文阅读学习应用原型。

## 当前已实现

- 书架页：导入入口、空态、阅读进度、最近阅读时间、删除确认。
- 导入：支持 Android 文件选择器导入 TXT；支持常见无 DRM、PalmDOC 压缩 MOBI 的基础文本解析。
- 阅读器：滚动阅读、段落首个英文字符大写加粗、阅读进度按段落保存。
- 排版：字体、字号、浅色/纸张/深色主题设置。
- 查词：长按英文单词后底部弹出音标、中文释义和 TTS 播放入口。
- 段落翻译：已预留翻译交互和状态；当前为本地演示译文，后续可接正式翻译服务。
- 笔记本：勾选句子加入笔记、编辑、删除、导出 Markdown。
- 产品文档：见 [docs/USER_STORIES.md](docs/USER_STORIES.md)。

## 运行方式

本仓库是标准 Android Gradle 工程。当前项目目录下已经准备了一套本地调试工具链，位于 `.local-toolchain/`，该目录已被 `.gitignore` 忽略。

构建 debug APK：

```bash
./scripts/build_debug.sh
```

安装到已开启 USB 调试并授权的 Android 手机：

```bash
./scripts/install_debug.sh
```

## 已知限制

- MOBI 第一版只覆盖常见无 DRM 文本内容，复杂排版、图片、目录和 DRM 不在 MVP 范围。
- 翻译当前是演示降级，不会把内容发送到外部服务；正式翻译需要后续接入 API 并补充隐私说明。
- 本地词典只内置少量高频词，未收录词会显示降级提示。
