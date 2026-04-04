# 十二符咒模组 (Twelve Talismans)

## 模组概述

十二符咒模组是一个基于Minecraft Forge的模组，灵感来源于动画《成龙历险记》中的十二符咒，为玩家提供了各种强大的符咒能力。该模组不仅实现了符咒的核心功能，还开发了自定义的粒子渲染API（Twelve Render API），用于创建各种华丽的粒子效果。

## 功能特性

- **十二符咒系统**：实现了多种符咒能力，包括龙符咒（火焰弹）、猪符咒（激光）等
- **自定义粒子API**：开发了Twelve Render API，支持创建复杂的粒子动画效果
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

## 粒子API（Twelve Render API）

### 概述

Twelve Render API是一个专为十二符咒模组开发的自定义粒子渲染API，替代了之前使用的AAAParticles模组。该API提供了以下功能：

- **粒子创建与管理**：支持创建和管理各种类型的粒子
- **粒子动画系统**：支持圆形轨道、螺旋、波浪等多种动画效果
- **网络同步**：确保粒子效果在服务器和客户端之间同步
- **性能优化**：采用高效的粒子更新和渲染机制

### 核心类

- `Trapi`：API主类，负责初始化和注册事件监听器
- `TrapiParticleManager`：粒子管理器，负责粒子的创建、更新和移除
- `ServerParticleGroupManager`：服务器端粒子组管理
- `ClientParticleGroupManager`：客户端粒子组管理
- `ParticleAnimation`：Kotlin实现的动画系统，提供各种粒子动画效果

### 使用示例

```java
// 创建圆形轨道粒子效果
ParticleAnimationExample.createCircleOrbitEffect(
    level,
    eyePos,
    1.0, // 半径
    10   // 粒子数量
);

// 创建螺旋粒子效果
ParticleAnimationExample.createSpiralEffect(
    level,
    startPos,
    1.5, // 半径
    2.0, // 高度
    15   // 粒子数量
);

// 创建波浪粒子效果
ParticleAnimationExample.createWaveEffect(
    level,
    eyePos.add(lookVec.x * 2, lookVec.y * 2, lookVec.z * 2),
    3.0,  // 长度
    0.5,  // 振幅
    10    // 粒子数量
);
```

## 安装方法

1. 确保已安装Minecraft Forge 1.20.1
2. 下载十二符咒模组的jar文件
3. 将jar文件放入mods文件夹
4. 启动游戏，享受符咒的力量

## 开发说明

### 项目结构

- `src/main/java/com/qituo/twelvetelismans/`：主要模组代码
- `Twelve Render API/src/main/java/com/qituo/trapi/`：粒子API代码
- `Twelve Render API/src/main/kotlin/com/qituo/trapi/`：Kotlin实现的动画系统

### 构建项目

1. 克隆项目到本地
2. 使用Gradle构建项目：
   ```bash
   ./gradlew build
   ```
3. 构建产物将在`build/libs`目录中生成

### 依赖

- Minecraft Forge 1.20.1-47.4.16
- Kotlin For Forge

## 未来计划

- 实现更多符咒能力
- 扩展粒子API功能
- 添加更多粒子动画效果
- 优化性能和兼容性

## 贡献

欢迎对项目提出建议和贡献！如果您有任何问题或想法，请在GitHub上提交issue或pull request。

## 许可证

本项目采用QSUP许可证。详见LICENSE文件。
