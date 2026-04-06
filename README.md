# Dragon Curse Chronicles

## 模组概述

Dragon Curse Chronicles是一个基于Minecraft Forge的模组，灵感来源于动画《成龙历险记》，为玩家提供了各种强大的符咒能力。该模组不仅实现了符咒的部分功能，还开发了自定义的粒子渲染API（DC Render API），用于创建各种华丽的粒子效果。

## 功能特性

- **十二符咒系统**：实现了多种符咒能力，包括龙符咒（火焰弹）、猪符咒（激光）等
- **自定义粒子API**：开发了DC Render API，支持创建复杂的粒子动画效果
- **Java与Kotlin混合开发**：核心功能使用Java实现，动画系统使用Kotlin实现
- **网络同步**：实现了服务器与客户端之间的粒子效果同步
- **性能优化**：采用高效的粒子管理和渲染机制

## 符咒介绍

### 龙符咒

- **能力**：发射恶魂风格的火焰弹，造成大范围爆炸伤害
- **粒子效果**：圆形轨道火焰粒子、螺旋粒子和波浪粒子
- **使用方法**：手持龙符咒，右键点击释放

### 猪符咒

- **能力**：发射激光，对直线上的目标造成伤害并点燃
- **粒子效果**：激光起点的圆形轨道粒子、终点的螺旋粒子和路径上的波浪粒子
- **使用方法**：手持猪符咒，右键点击释放

## 安装方法

1. 确保已安装Minecraft Forge 1.20.1
2. 下载Dragon Curse Chronicles模组的jar文件
3. 将jar文件放入mods文件夹
4. 启动游戏，享受符咒的力量

## 开发说明

### 项目结构

- `src/main/java/com/qituo/dcc/`：主要模组代码
- `DC Render API/src/main/java/com/qituo/dcrapi/`：粒子API代码
- `DC Render API/src/main/kotlin/com/qituo/dcrapi/`：Kotlin实现的动画系统

### 构建项目

1. 克隆项目到本地
2. 使用Gradle构建项目：
   ```bash
   ./gradlew build
   ```
3. 构建产物将在`build/libs`目录中生成

### 依赖

- Minecraft Forge 1.20.1-47.4.17
- DC Render API 0.1.0+

## 未来计划

- 实现更多符咒能力
- 优化性能和兼容性

## 贡献

欢迎对项目提出建议和贡献！如果您有任何问题或想法，请在GitHub上提交issue或pull request。

## 许可证

本项目采用QSUP许可证。详见LICENSE文件。
