# 龙咒异闻录 / Dragon Curse Chronicles

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/19136644525lxy/Dragon-Curse-Chronicles/blob/main/LICENSE.md)
[![GitHub](https://img.shields.io/badge/GitHub-源码仓库-blue)](https://github.com/19136644525lxy/Dragon-Curse-Chronicles)
[![CurseForge](https://img.shields.io/badge/CurseForge-下载页-orange)](https://www.curseforge.com/minecraft/mc-mods/dragon-curse-chronicles)
[![Modrinth](https://img.shields.io/badge/Modrinth-下载页-blue)](https://modrinth.com/mod/dragon-curse-chronicles)
[![Platform](https://img.shields.io/badge/平台-Forge%20%7C%20Fabric%20%7C%20NeoForge-darkgreen)](#平台支持)
[![Version](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%201.21.1-blue)](#平台支持)

> 灵感来源于经典动画《成龙历险记》的 Minecraft 模组，还原十二生肖符咒的力量，并引入始源之力附魔、老爹的河豚干、自定义粒子渲染 API 等扩展玩法。

Jump to the English introduction: [README_en.md](https://github.com/19136644525lxy/Dragon-Curse-Chronicles/blob/main/README_en.md)

---

## 平台支持

| 加载器 | Minecraft 版本 | 模组版本 | 状态 |
|---|---|---|---|
| **Minecraft Forge** | 1.20.1 | `0.2.4-rc-9` | ✅ 已发布，功能完整 |
| **Fabric** | 1.20.1 | `1.0.6-1.20.1Fabric` | ✅ 已发布，功能完整 |
| **NeoForge** | 1.21.1 | `0.1.0-1.21.1NeoForge` | ✅ 已发布，功能完整 |

> 三平台功能对等。每个平台均需要同时安装 **主模组 + DC Render API 前置 + Kotlin 前置**（见下方"安装方法"）。

---

## 功能特性

- **十二符咒系统**：还原鼠、牛、虎（WIP）、兔、龙、蛇、马、羊、猴（WIP）、鸡、狗、猪 十二符咒的能力，每个符咒都有独立粒子与音效
- **始源之力附魔**：10 级递进式附魔，1-5 级通过附魔台/村民交易获取，6-10 级通过 4 合 1 合成升级；盔甲穿戴解锁反弹、减伤、能量护盾、生命再生、免疫击退、始源光环 6 层效果
- **始源光环**：半径 10 格范围，可通过自定义按键（默认无绑定）开关，向周边实体持续施加始源终结伤害
- **老爹的河豚干**：右键即发绿色激光，附带双螺旋环绕粒子带与渐变色，发射后 5 秒冷却；对 Draconic Guardian 等 BOSS 具备五重击杀链绕过机制
- **自定义粒子 API（DC Render API）**：自研粒子调度框架，支持对象池、分帧发送、LOD 距离衰减、渐进式射出等优化，绕过客户端粒子模组的激进剔除
- **符咒提取器与唐扇魔方**：从动物身上提取符咒之力；唐扇魔方通过流星雨事件的宝箱概率获取（35%）
- **流星雨事件**：主世界午夜随机触发（30%），陨石坑中生成宝箱，内含唐扇魔方、符咒基等稀有物品；也可通过 `/meteorshower start` 指令手动触发
- **羊符咒灵魂出窍**：隐身、夜视、飞行、穿墙、无敌一体化；退出灵魂模式回到原身体位置
- **符咒之力合成**：所有符咒、符咒基、始源之力附魔书升级均有完整的数据包配方

---

## 符咒详解

### ✅ 已实现

| 符咒 | 能力 | 使用方法 |
|---|---|---|
| 🐭 **鼠符咒** | 将特定方块转化为对应生物 | 手持对目标方块右键 |
| 🐮 **牛符咒** | 攻击力（力量 III）+ 防御力（抗性 III）+ 移动速度（速度 II） | 手持右键激活，3 分钟持续 |
| 🐇 **兔符咒** | 移速提升 + 5 格瞬移；先激活鸡符咒漂浮后再激活兔符咒可获得**鸡兔之力（创造飞行）** | 手持右键激活 |
| 🐉 **龙符咒** | 发射带火焰粒子轨道的龙焰火球，造成范围爆炸伤害 | 手持右键，1 秒冷却 |
| 🐍 **蛇符咒** | 增强版隐身 II + 自定义蛇之力图标 | 手持右键，5 分钟持续 |
| 🐎 **马符咒** | 瞬间满血 + 清除中毒、凋零、虚弱、缓慢、失明、饥饿、飘浮、发光、不祥之兆、黑暗 10 种负面效果 | 手持右键，1 分钟马之力 |
| 🐑 **羊符咒** | 灵魂出窍：隐身 + 夜视 + 飞行 + 穿墙 + 无敌；身体坐标被记录，结束时回到原地 | 手持右键切换 |
| 🐔 **鸡符咒** | 缓降 / 漂浮二选一；漂浮生效后可与兔符咒组合触发鸡兔之力 | 右键释放，**Shift + 右键**切换模式 |
| 🐶 **狗符咒** | 生命恢复 + 清除 10 种负面效果 + 伤害吸收（30 秒永续） | 手持右键 |
| 🐷 **猪符咒** | 发射高能激光，对直线目标造成 99 点伤害并点燃，附带自定义粒子轨道 | 手持右键，2 秒冷却 |

### 🔧 开发中（WIP）

- 🐯 **虎符咒**：善恶分离，生成玩家的善/恶人格分身
- 🐒 **猴符咒**：变形之力，把目标生物随机变形为其他生物

---

## 始源之力附魔（Origin Power）

| 项目 | 详情 |
|---|---|
| **获取渠道** | 1-5 级：附魔台 / 村民交易（等级越高概率越低）；6-10 级：4 本 N 级合成 1 本 N+1 级 |
| **最大等级** | 10 |
| **适用物品** | 任意盔甲（头盔、胸甲、护腿、靴子）；非盔甲位仅附魔不生效 |
| **始源终结伤害** | 由 `Uncle's Dried Puffer Fish` 触发，走自定义伤害类型 `origin_end`，绕过大部分伤害减免、效果与无敌 |

### 盔甲效果阶梯（按 4 件盔甲总等级解锁）

| 总等级阈值 | 解锁效果 |
|---|---|
| 1+ | **反弹**：对攻击者反弹部分伤害（随等级提升） |
| 5+ | **减伤**：按等级梯度减伤，最高层减伤显著 |
| 10+ | **能量护盾**：周期吸收伤害 |
| 15+ | **生命再生**：周期性回血 |
| 20+ | **免疫击退**：不再被击退 |
| 25+ | **始源光环**：对半径 10 格范围内的生物持续施加始源伤害，按键可开关 |

> 始源光环按键默认无绑定，需在"控制→按键绑定"中手动设置；切换状态通过网络同步到服务端并持久化。

---

## 老爹的河豚干（Uncle's Dried Puffer Fish）

- **触发方式**：右键即发射绿色激光（无需蓄力），发射后进入 **5 秒冷却**
- **伤害机制**：始源终结伤害类型，对 Draconic Guardian 等 boss 执行专用击杀链（破盾 → 破头 → 秒杀）
- **视觉表现**：绿色主束 + 两条螺旋反向环绕粒子带（绿→青 与 紫→粉渐变），共 1200 个粒子对象入队，按近→远渐进式射出，搭配对象池与 LOD 优化
- **音效**：施法瞬间播放老爹"Madgaq"音效，命中后烟花爆破 + 火焰环境音

---

## 物品与事件

### 符咒之力提取器
- **用法**：主手拿提取器，副手拿符咒基，对动物右键提取
- **概率**：默认 10%，可在配置文件中调整
- **次数限制**：鸡/龙/猪 100 次，其余 10 次（硬编码）
- **获取方式**：合成

### 唐扇魔方（Cube of Tang Shan）
- **用法**：提取羊符咒，步骤与提取器相同，对羊右键
- **获取方式**：流星雨事件宝箱（35% 概率）

### 流星雨事件
- **触发时机**：主世界午夜 00:00，每次 30% 概率
- **手动触发**：OP 玩家执行 `/meteorshower start`
- **奖励**：陨石坑内生成宝箱，含唐扇魔方、符咒基等稀有物品

---

## 安装方法

> ⚠ 三平台均要求同时安装 **三件套**：主模组 + DC Render API 前置 + Kotlin 前置，缺一不可。

### Forge 1.20.1
1. 安装 Minecraft Forge 1.20.1（47.x 及以上）
2. 下载并安装以下 3 份 jar（均为 Forge 版）：
   - **Kotlin 前置**：`Kotlin-for-Forge-*-1.20.1.jar`（由 thedarkcolour 维护，CurseForge/Modrinth 可下载）
   - **渲染前置（DC Render API）**：`dcrapi-X.X.X-1.20.1Forge.jar`
   - **主模组**：`dcc-0.2.4-rc-9.jar`
3. 将三份 jar 同时放入 `mods/` 目录
4. 启动游戏

### Fabric 1.20.1
1. 安装 Fabric Loader 0.16.13+ 与 Fabric API 0.92.11+1.20.1
2. 下载并安装以下 3 份 jar（均为 Fabric 版）：
   - **Kotlin 前置**：Fabric 语言适配器 `fabric-language-kotlin-*`（CurseForge/Modrinth 可下载）
   - **渲染前置（DC Render API）**：`dcrapi-X.X.X-1.20.1Fabric.jar`
   - **主模组**：`dcc-1.0.6-1.20.1Fabric.jar`
3. 将三份 jar 同时放入 `mods/` 目录
4. 启动游戏

### NeoForge 1.21.1
1. 安装 NeoForge 21.1.248+（对应 Minecraft 1.21.1）
2. 下载并安装以下 3 份 jar（均为 NeoForge 版）：
   - **Kotlin 前置**：`Kotlin-for-Forge-*-1.21.1-NeoForge.jar`（由 thedarkcolour 维护，CurseForge/Modrinth 可下载）
   - **渲染前置（DC Render API）**：`dcrapi-*-1.21.1NeoForge.jar`
   - **主模组**：`dcc-0.1.0-1.21.1NeoForge.jar`
3. 将三份 jar 同时放入 `mods/` 目录
4. 启动游戏

---

## 构建与开发

### 项目结构

```
Twelve Talismans/                           # 主仓库
├── src/main/java/com/qituo/dcc/            # Forge 1.20.1 主模组代码
├── fabric/Dragon Curse Chronicles/src/     # Fabric 1.20.1 主模组代码
├── neoforge/src/main/java/com/qituo/dcc/   # NeoForge 1.21.1 主模组代码
├── DC Render API/                          # 粒子前置 API 多平台子目录
│   ├── src/                                # Forge API
│   ├── fabric/DC Render API/               # Fabric API
│   └── neoforge/                           # NeoForge API
├── gradle.properties                       # Forge 版版本号
├── README.md / README_en.md                # 中文 / 英文文档
```

### 构建命令

```bash
# Forge 1.20.1（根目录）
./gradlew build            # 产物 → build/libs/dcc-0.2.4-rc-9.jar

# Fabric 1.20.1
cd fabric/Dragon\ Curse\ Chronicles
./gradlew build            # 产物 → build/libs/dcc-1.0.6-1.20.1Fabric.jar

# NeoForge 1.21.1
cd neoforge
./gradlew build            # 产物 → build/libs/dcc-0.1.0-1.21.1NeoForge.jar
```

构建同时自动生成 `-sources.jar` 源代码包。

---

## 未来计划

- 猴符咒（变形）与虎符咒（善恶分身）的完整实现
- NeoForge 1.21.1 端完善更多功能细节
- 符咒盒 GUI 与快捷切换功能
- 始源之力附魔的数据包驱动注册替代当前过渡方案（NBT 直读）

---

## 贡献

欢迎 Issue 反馈、功能建议与 Pull Request！

---

## 许可证

本项目采用 **MIT License**，详见 [LICENSE.md](https://github.com/19136644525lxy/Dragon-Curse-Chronicles/blob/main/LICENSE.md)。
