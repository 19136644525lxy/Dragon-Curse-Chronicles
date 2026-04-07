# Dragon Curse Chronicles

## Mod Overview

Dragon Curse Chronicles is a Minecraft Forge mod inspired by the animated series "Jackie Chan Adventures", providing players with various powerful talisman abilities. The mod not only implements some of the talisman functions but also develops a custom particle rendering API (DC Render API) for creating various magnificent particle effects.

跳转至中文介绍： [README.md](https://github.com/19136644525lxy/Dragon-Curse-Chronicles/blob/6389b5969bf56083bc95ec9ec892a4a5c7c0cf46/README.md)

## Features

- **Twelve Talismans System**: Implements multiple talisman abilities, including the Dragon Talisman (fireball) and Pig Talisman (laser)
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
- **Ability**: Toggles flight mode, providing flight capability
- **Usage**: Hold the Chicken Talisman and right-click to toggle flight status

#### Dog Talisman
- **Ability**: Provides life recovery, clears negative effects, and grants temporary invulnerability
- **Usage**: Hold the Dog Talisman and right-click to release

#### Horse Talisman
- **Ability**: Completely restores health and clears all negative effects
- **Usage**: Hold the Horse Talisman and right-click to release

#### Snake Talisman
- **Ability**: Provides enhanced invisibility effect
- **Usage**: Hold the Snake Talisman and right-click to release

#### Rabbit Talisman
- **Ability**: Increases movement speed and enables short-range teleportation
- **Usage**: Hold the Rabbit Talisman and right-click to release

#### Cow Talisman
- **Ability**: Increases attack power, defense, and movement speed
- **Usage**: Hold the Cow Talisman and right-click to release

#### Mouse Talisman
- **Ability**: Transforms specific blocks into corresponding creatures
- **Usage**: Hold the Mouse Talisman and right-click on target blocks

### Unimplemented Talismans

#### Monkey Talisman
- **Planned Function**: Transformation Power (transform targets into other creatures)

#### Tiger Talisman
- **Planned Function**: Good and Evil Separation (create good and evil clones of the player)

#### Sheep Talisman
- **Planned Function**: Out-of-Body Power (allow the soul to leave the body)

## Installation

1. Ensure Minecraft Forge 1.20.1 is installed
2. Download the Dragon Curse Chronicles mod jar file
3. Place the jar file into the mods folder
4. Start the game and enjoy the power of the talismans

## Development Notes

### Project Structure

- `src/main/java/com/qituo/dcc/`: Main mod code
- `DC Render API/src/main/java/com/qituo/dcrapi/`: Particle API code
- `DC Render API/src/main/kotlin/com/qituo/dcrapi/`: Kotlin-implemented animation system

### Building the Project

1. Clone the project to your local machine
2. Build the project using Gradle:
   ```bash
   ./gradlew build
   ```
3. The build product will be generated in the `build/libs` directory

### Dependencies

- Minecraft Forge 1.20.1-47.4.17
- DC Render API 0.1.0+

## Future Plans

- Implement more talisman abilities
- Optimize performance and compatibility

## Contributing

Welcome to submit suggestions and contributions to the project! If you have any questions or ideas, please submit an issue or pull request on GitHub.

## License

This project is licensed under the QSUP License. See the LICENSE file for details.
