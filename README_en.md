# Dragon Curse Chronicles

## Mod Overview

Dragon Curse Chronicles is a Minecraft mod inspired by the animated series "Jackie Chan Adventures", providing players with various powerful talisman abilities. The mod supports both Forge and Fabric mod loaders (1.20.1), and develops a custom particle rendering API (DC Render API) for creating various magnificent particle effects.

跳转至中文介绍: [README.md](https://github.com/19136644525lxy/Dragon-Curse-Chronicles/blob/main/README.md)

## Features

- **Twelve Talismans System**: Implements multiple talisman abilities, including the Dragon Talisman (fireball) and Pig Talisman (laser)
- **Dual Loader Support**: Supports both Forge and Fabric (1.20.1) with feature parity
- **Custom Particle API**: Developed DC Render API, supporting complex particle animation effects
- **Java and Kotlin Hybrid Development**: Core functionality implemented in Java, animation system implemented in Kotlin
- **Network Synchronization**: Achieves particle effect synchronization between server and client
- **Performance Optimization**: Adopts efficient particle management and rendering mechanisms
- **Origin Power Enchantment**: Powerful enchantment system providing defense-ignoring damage capabilities
- **Uncle's Dried Puffer Fish**: Special in-game item
- **Talisman Base**: Basic material for crafting talismans

## Talisman Introduction

### Implemented Talismans

#### Dragon Talisman
- **Ability**: Fires Ghast-style fireballs, causing large-scale explosion damage
- **Particle Effects**: Circular orbit fire particles, spiral particles, and wave particles
- **Usage**: Hold the Dragon Talisman and right-click to release

#### Pig Talisman
- **Ability**: Fires lasers, causing damage and igniting targets in a straight line
- **Particle Effects**: Circular orbit particles at the laser origin, spiral particles at the end, and wave particles along the path
- **Usage**: Hold the Pig Talisman and right-click to release

#### Chicken Talisman
- **Ability**: Gain Slow Falling Power or Levitation Power
- **Usage**: Hold the Chicken Talisman and right-click to release, Shift+right-click to switch between Slow Falling Power and Levitation Power

#### Dog Talisman
- **Ability**: Provides life recovery, clears negative effects, and grants sustained absorption
- **Usage**: Hold the Dog Talisman and right-click to release

#### Horse Talisman
- **Ability**: Completely restores health and clears all negative effects
- **Usage**: Hold the Horse Talisman and right-click to release

#### Snake Talisman
- **Ability**: Provides enhanced invisibility effect
- **Usage**: Hold the Snake Talisman and right-click to release

#### Rabbit Talisman
- **Ability**: Increases movement speed and enables short-range teleportation, when the Chicken Talisman's Levitation Power is active, right-clicking this talisman will grant Chicken-Rabbit Power (flight ability)
- **Usage**: Hold the Rabbit Talisman and right-click to release

#### Cow Talisman
- **Ability**: Increases attack power, defense, and movement speed
- **Usage**: Hold the Cow Talisman and right-click to release

#### Mouse Talisman
- **Ability**: Transforms specific blocks into corresponding creatures
- **Usage**: Hold the Mouse Talisman and right-click on target blocks

#### Sheep Talisman
- **Ability**: Out-of-Body Power (invisibility, night vision, flight, wall-clipping, invulnerability)
- **Usage**: Hold the Sheep Talisman and right-click to release

### Unimplemented Talismans

#### Monkey Talisman
- **Planned Function**: Transformation Power (transform targets into other creatures)

#### Tiger Talisman
- **Planned Function**: Good and Evil Separation (create good and evil clones of the player)

## Item Introduction

### Talisman Power Extractor

- **Ability**: Extract power from animals to craft talismans
- **Usage**: Hold the extractor in the main hand and a Talisman Base in the offhand, then right-click on animals to extract power and craft talismans. There are usage limits and probability restrictions. The probability can be adjusted in the configuration file, with a default probability of 10%. Except for Chicken, Dragon, and Pig, other animals require 10 extraction attempts, while these three require 100 attempts (the attempt limit is hard-coded and cannot be changed).
- **Obtainment**: Craftable

### Cube of Tang Shan

- **Ability**: Extract Sheep Talisman, following the same steps as the Talisman Power Extractor
- **Usage**: Hold the Cube of Tang Shan in the main hand and a Talisman Base in the offhand, then right-click on sheep to extract the Sheep Talisman. The probability is 10% and requires 100 attempts (hard-coded, cannot be changed).
- **Obtainment**: Obtained through the Meteor Shower event. There is a 35% chance to find it in the treasure chest that appears after the meteor shower ends. The meteor shower event randomly triggers at midnight (12:00 AM) in the Overworld (30% probability), or can be forced using the command `/meteorshower start` (requires OP permission). The chest spawns inside a meteor crater and needs to be dug out to find.

### Talisman Box

- **Ability**: Store and manage talismans
- **Usage**: No use yet, not implemented.

## Installation

### Forge Version
1. Ensure Minecraft Forge 1.20.1 (47.x or higher) is installed
2. Download the Forge version jar files of Dragon Curse Chronicles and DC Render API
3. Place both jar files into the mods folder
4. Start the game

### Fabric Version
1. Ensure Fabric Loader 0.16.13 or higher, and Fabric API 0.92.11+ are installed
2. Download the Fabric version jar files of Dragon Curse Chronicles and DC Render API
3. Place both jar files into the mods folder
4. Start the game and enjoy the power of the talismans

## Development Notes

### Project Structure

Main repository (Dragon Curse Chronicles main mod):
- `src/main/java/com/qituo/dcc/`: Forge main mod code
- `fabric/Dragon Curse Chronicles/src/`: Fabric main mod code
- `fabric/sources/`: Fabric 1.20.1 Yarn mapping sources (for reference)

Dependency repository (DC Render API):
- `src/main/java/com/qituo/dcrapi/`: Forge particle API code
- `src/main/kotlin/com/qituo/dcrapi/`: Kotlin-implemented animation system
- `fabric/DC Render API/src/`: Fabric particle API code

### Building the Project

#### Forge Version
1. Enter the project root directory
2. Build using Gradle:
   ```bash
   ./gradlew build
   ```
3. The build product will be generated in the `build/libs/` directory

#### Fabric Version
1. Enter the `fabric/Dragon Curse Chronicles/` directory
2. Build using JDK 17+ and Gradle:
   ```bash
   ./gradlew build
   ```
3. The build product will be generated in `fabric/Dragon Curse Chronicles/build/libs/`

### Dependencies

#### Forge Version
- Minecraft Forge 1.20.1-47.4.17
- DC Render API (Forge) 0.1.0+

#### Fabric Version
- Fabric Loader 0.16.13+
- Fabric API 0.92.11+1.20.1
- DC Render API (Fabric) 0.1.0+

## Future Plans

- Implement more talisman abilities
- Optimize performance and compatibility

## Contributing

Welcome to submit suggestions and contributions to the project! If you have any questions or ideas, please submit an issue or pull request on GitHub.

## License

This project is licensed under the MIT License. See the LICENSE file for details.
