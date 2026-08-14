# Cracker's Wither Storm Legacy

将 [Cracker's Wither Storm Mod](https://www.curseforge.com/minecraft/mc-mods/wither-storm-mod)（Forge 1.20.1，版本 4.2.1）完整移植到 Minecraft 1.12.2 / CleanroomLoader 的社区移植版。

模组 ID：`witherstormmod`
模组名称：`Cracker's Wither Storm Legacy`

## 重要：上游资源包要求（ARR 合规）

上游模组受 **All Rights Reserved** 协议保护，本移植版**不包含**上游的任何纹理、声音、模型、结构、语言文件或数据文件。

玩家必须自行下载并安装原版上游 JAR：

1. 下载 `witherstormmod-1.20.1-4.2.1-all.jar`（原版 Forge 1.20.1 模组文件）。
2. 将文件放入 Minecraft 的 `resourcepacks` 文件夹（与存档目录同级，例如 `.minecraft/resourcepacks/`）。
3. 启动游戏。

本移植模组在运行时把该 JAR 挂载为外部资源包和数据来源：

- 纹理、声音、模型、语言文件来自该 JAR；
- 腐化配方、成就、战利品表、结构、标签等数据也直接读取该 JAR；
- 启动时会校验 JAR 的清单版本必须为 `1.20.1-4.2.1`，缺失或版本不符会拒绝加载。

请勿把该 JAR 内的资源复制进本移植模组或随模组分发。

上游 JAR 内还带一个可选的 “CWSM Programmer Art” 资源包（`resourcepacks/programmer_art`）。
出于 ARR 合规，本移植不会自动解包或复制它；需要该资源包的玩家可自行从原 JAR
中把 `resourcepacks/programmer_art` 解压到游戏的 `resourcepacks` 文件夹。

## 环境要求

- Minecraft 1.12.2
- CleanroomLoader 0.6.6-alpha（Forge 1.12.2 分支）
- Future MC（本移植依赖其提供的 1.13+ 方块/物品/生物，例如枯萎玫瑰、钟等）
- JEI（可选；安装后显示超级信标配方分类）
- Java 25（构建工具链）

## 构建

```text
gradlew build
```

项目使用 Gradle 9.6.1 与 Cleanroom 自定义 Unimined 分支，编译目标为 Java 25。

构建产物：

```text
build/libs/WitherStormLegacy-1.0.0.jar
```

## 已移植内容概览

- 凋零风暴完整阶段、进化、装死/复活、分裂体与独立头、牵引光束、方块质量簇、吞噬与存档持久化
- 肠道维度、命令方块核心 19 阶段 Boss 战、结构模板网络
- 病化生物体系（转化/治愈、凋零病状态机、自定义 AI）
- 方块/物品/合成/酿造/铁砧/切石机配方，超级信标物品合成与实体召唤
- 成就、战利品表、标签与外部资源转换
- 管理命令 `/witherstormmod`、JEI 分类、Boss 主题、屏幕震动/白屏、节日彩蛋等表现

## 开发说明

- 上游资源通过 `UpstreamResourceArchive` / `UpstreamResourcePack` 在运行时读取，禁止把上游资源复制进 `src/main/resources`。
- 1.20.1 特有的系统（Brain 记忆/传感器、配方书、画变体、数据包生成器等）按“原版差异以 1.12.2 语义为准”处理。
- 完整移植状态与逐次改动记录见 `docs/operations-log.md`。

## 兼容性说明

- 本移植内置对 Future MC 0.2.6 的兼容垫片：其蜂巢生成器在 Java 17+ 上会因
  `sun.reflect.Reflection` 被移除而崩溃，垫片会让其直接走快速判定路径，因此
  可以在 Java 17+（含本仓库构建所用的 Java 25）上正常生成世界。
- Future MC 0.2.6 默认关闭十字弩注册。本移植通过 Mixin 恢复 Future MC
  自带的完整弩注册链（物品、三个弩专用附魔、模型、声音和事件），因此上游配方
  中引用 `minecraft:crossbow` 的召唤病化掠夺者配方运行时使用
  `futuremc:crossbow`；同时注册 1.12 标准弩合成配方，并保留末影珍珠弩兼容。
- 上游 1.20 的“远距离渲染器”已按 1.12 语义改为强制远距跟踪、空白区块实体
  保留、扩展投影和漏绘补绘；远距离雾、LOD、碎片分辨率、VBO 与异步缓冲开关
  均已接入对应渲染路径。
- 本移植提供与上游一致的配置界面（模组列表 → Config）：客户端/服务器全部
  选项、上游预设（Medium/Low/Ultra Low、Performance、Mass Destruction）与
  “刷新声音”按钮；并包含自定义主菜单全景、撕裂脉冲方块、耳鸣音效、
  天空氛围配色、病化覆盖层与“护身符”画作。
- 上游两个病化悬挂告示牌方块依赖 1.20 的悬挂告示牌系统，1.12 没有可对应的
  放置、方块状态和渲染机制，因此不注册；其余平台差异及等价映射见完成度审计。
