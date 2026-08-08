# Dragon Curse Chronicles

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/19136644525lxy/Dragon-Curse-Chronicles/blob/main/LICENSE.md)
[![GitHub](https://img.shields.io/badge/GitHub-Source-blue)](https://github.com/19136644525lxy/Dragon-Curse-Chronicles)
[![CurseForge](https://img.shields.io/badge/CurseForge-Download-orange)](https://www.curseforge.com/minecraft/mc-mods/dragon-curse-chronicles)
[![Modrinth](https://img.shields.io/badge/Modrinth-Download-blue)](https://modrinth.com/mod/dragon-curse-chronicles)
[![Platform](https://img.shields.io/badge/Platform-Forge%20%7C%20Fabric%20%7C%20NeoForge-darkgreen)](#platform-support)
[![Version](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%201.21.1-blue)](#platform-support)

> A Minecraft mod inspired by the classic animated series *Jackie Chan Adventures*, recreating the powers of the Twelve Chinese Zodiac Talismans. Expanded gameplay includes the Origin Power enchantment, Uncle's Dried Puffer Fish, and a custom particle rendering API.

跳转至中文介绍: [README.md](https://github.com/19136644525lxy/Dragon-Curse-Chronicles/blob/main/README.md)

---

## Platform Support

| Loader | Minecraft Version | Mod Version | Status |
|---|---|---|---|
| **Minecraft Forge** | 1.20.1 | `0.2.4-rc-9` | ✅ Released, feature-complete |
| **Fabric** | 1.20.1 | `1.0.6-1.20.1Fabric` | ✅ Released, feature-complete |
| **NeoForge** | 1.21.1 | `0.1.0-1.21.1NeoForge` | ✅ Released, feature-complete |

> All three loaders ship with feature parity. **Each platform requires three jars to be installed together**: the main mod + DC Render API dependency + a Kotlin language adapter (see *Installation* below).

---

## Features

- **Twelve Talismans System**: Re-creates the powers of the Rat, Ox, Tiger (WIP), Rabbit, Dragon, Snake, Horse, Sheep, Monkey (WIP), Rooster, Dog, and Pig. Each talisman has dedicated particles and sound effects.
- **Origin Power Enchantment**: 10-level progressive enchantment. Levels 1–5 are obtainable from the enchanting table and villager trades; levels 6–10 are crafted by combining 4 books of level N → 1 book of level N+1. Full armor unlocks 6 layered effects: reflection, damage reduction, energy shield, health regeneration, knockback immunity, and Origin Aura.
- **Origin Aura**: 10-block radius effect, toggleable via custom keybind (unbound by default). Applies continuous Origin End damage to nearby entities.
- **Uncle's Dried Puffer Fish**: Right-click fires a green laser (no charge-up) with two counter-rotating spiral particle bands and gradient colors, then enters a 5-second cooldown. Includes a specialized "five-kill-chain" bypass for bosses such as the Draconic Guardian.
- **Custom Particle API (DC Render API)**: Home-grown particle dispatcher featuring color/particle object pooling, frame-aware batching, LOD distance falloff, and progressive emission — bypassing aggressive culling performed by client-side particle optimization mods.
- **Talisman Power Extractor & Cube of Tang Shan**: Extract talismanic essence from animals. The Cube of Tang Shan drops from Meteor Shower loot chests at 35% chance.
- **Meteor Shower Event**: 30% chance to trigger every Overworld midnight. Chests spawn inside impact craters with Cube of Tang Shan, Talisman Base, and other rare items. Trigger manually with `/meteorshower start` (OP only).
- **Sheep Talisman — Soul Out-of-Body**: Invisibility + Night Vision + Flight + No-Clip + Invulnerability. Returns the player to their recorded body position when deactivated.
- **Data-driven recipes**: Datapack recipes are provided for all talismans, Talisman Base, and Origin Power book upgrades.

---

## Talisman Reference

### ✅ Implemented

| Talisman | Ability | Usage |
|---|---|---|
| 🐭 **Rat** | Transform specific blocks into the matching creatures | Hold and right-click the target block |
| 🐮 **Ox** | Strength III + Resistance III + Speed II | Hold and right-click — 3-minute duration |
| 🐇 **Rabbit** | Speed boost + 5-block teleport. **Chicken + Rabbit combo** (flight) is unlocked if the Rooster's Levitation is active first | Hold and right-click |
| 🐉 **Dragon** | Fires a ghast-style fireball with a flaming particle trail, dealing AoE explosion damage | Hold and right-click — 1-second cooldown |
| 🐍 **Snake** | Enhanced Invisibility II + custom Snake Power icon | Hold and right-click — 5-minute duration |
| 🐎 **Horse** | Fully heals the player and clears 10 negative effects (Poison, Wither, Weakness, Slowness, Blindness, Hunger, Levitation, Glowing, Bad Omen, Darkness) | Hold and right-click — 1-minute Horse Power |
| 🐑 **Sheep** | Soul Out-of-Body: Invisibility + Night Vision + Flight + No-Clip + Invulnerability. Body position is saved and restored on exit | Hold and right-click to toggle |
| 🐔 **Rooster** | Slow Falling / Levitation modes. When Levitation is active, right-click Rabbit Talisman to trigger Chicken-Rabbit Flight. | Right-click fires mode. **Shift + right-click** switches mode |
| 🐶 **Dog** | Health regen + clears 10 debuffs + sustained damage absorption (renewing every 30s) | Hold and right-click |
| 🐷 **Pig** | Fires a high-energy laser dealing 99 damage + ignites targets along the line. Custom particle track | Hold and right-click — 2-second cooldown |

### 🔧 Work In Progress (WIP)

- 🐯 **Tiger**: Good/Evil Separation — spawns a benevolent and a malicious clone of the player
- 🐒 **Monkey**: Polymorph Power — randomly transforms target mobs into other creatures

---

## Origin Power Enchantment

| Item | Details |
|---|---|
| **Acquisition** | Levels 1–5: Enchanting Table / Villager Trades (higher level = lower probability). Levels 6–10: Combine 4 × Level N books in a crafting table → 1 × Level N+1 book. |
| **Max Level** | 10 |
| **Valid Equipment** | Any armor piece (helmet, chestplate, leggings, boots). Enchanting non-armor has no effect. |
| **Origin End Damage** | Triggered by Uncle's Dried Puffer Fish via the custom `origin_end` damage type, bypassing most defenses, effects, and invulnerability. |

### Armor Effect Ladder (unlocked by total level across 4 armor slots)

| Total Level Tier | Unlocked Effect |
|---|---|
| 1+ | **Damage Reflection** — reflects a portion of damage back to the attacker (scales with level) |
| 5+ | **Damage Reduction** — tiered DR, strongest at the highest bracket |
| 10+ | **Energy Shield** — periodic damage absorption |
| 15+ | **Regeneration** — periodic healing ticks |
| 20+ | **Knockback Immunity** — no longer knocked back |
| 25+ | **Origin Aura** — continuous Origin End damage to all entities within a 10-block radius. Toggle via keybind. |

> Aura keybind is unbound by default; set it in *Controls → Key Binds*. Toggle state is network-synced and persisted per player.

---

## Uncle's Dried Puffer Fish

- **Trigger**: Right-click fires a green laser immediately (no charge-up), then enters a **5-second cooldown**.
- **Damage Pipeline**: Origin End damage type; uses a specialized kill-chain for bosses like the Draconic Guardian (break shield → attack head → execute).
- **Visual Pipeline**: Green central beam + 2 counter-rotating helical bands (green→cyan gradient and purple→pink gradient). ~1200 particle candidates are enqueued per shot and emitted progressively near→far via SmartParticleDispatcher (pooled + LOD).
- **Audio**: Uncle's "Madgaq" voice line on cast, followed by a firework blast + ambient fire sound on impact.

---

## Items & Events

### Talisman Power Extractor
- **Usage**: Main hand = Extractor, offhand = Talisman Base → right-click an animal to extract.
- **Probability**: 10% by default, configurable via config file.
- **Use limits**: Rooster/Dragon/Pig = 100 attempts each; all others = 10 attempts each (hardcoded).
- **Obtainment**: Crafting recipe.

### Cube of Tang Shan
- **Usage**: Same flow as the Extractor, but only works on sheep — yields Sheep Talisman.
- **Obtainment**: Meteor Shower loot chest (35% chance).

### Meteor Shower Event
- **Trigger time**: Overworld midnight 00:00, 30% chance per night.
- **Manual trigger**: OP players run `/meteorshower start`.
- **Rewards**: Chest(s) spawn inside meteor craters with the Cube of Tang Shan, Talisman Bases, and other rare loot.

---

## Installation

> ⚠ All three loaders require the **three-jar setup**: Main Mod + DC Render API + Kotlin Language Adapter. Missing any of the three will crash during load.

### Forge 1.20.1
1. Install Minecraft Forge 1.20.1 (47.x or newer).
2. Download these three **Forge** jars:
   - **Kotlin Adapter**: `Kotlin-for-Forge-*-1.20.1.jar` (by thedarkcolour, available on CurseForge and Modrinth)
   - **DC Render API (required dependency)**: `dcrapi-X.X.X-1.20.1Forge.jar`
   - **Main Mod**: `dcc-0.2.4-rc-9.jar`
3. Drop all three jars into your `mods/` folder.
4. Launch the game.

### Fabric 1.20.1
1. Install Fabric Loader 0.16.13+ and Fabric API 0.92.11+1.20.1.
2. Download these three **Fabric** jars:
   - **Kotlin Adapter**: `fabric-language-kotlin-*` (available on CurseForge and Modrinth)
   - **DC Render API (required dependency)**: `dcrapi-X.X.X-1.20.1Fabric.jar`
   - **Main Mod**: `dcc-1.0.6-1.20.1Fabric.jar`
3. Drop all three jars into your `mods/` folder.
4. Launch the game.

### NeoForge 1.21.1
1. Install NeoForge 21.1.248+ for Minecraft 1.21.1.
2. Download these three **NeoForge** jars:
   - **Kotlin Adapter**: `Kotlin-for-Forge-*-1.21.1-NeoForge.jar` (by thedarkcolour, available on CurseForge and Modrinth)
   - **DC Render API (required dependency)**: `dcrapi-*-1.21.1NeoForge.jar`
   - **Main Mod**: `dcc-0.1.0-1.21.1NeoForge.jar`
3. Drop all three jars into your `mods/` folder.
4. Launch the game.

---

## Building & Developing

### Project Structure

```
Twelve Talismans/                           # Monorepo root
├── src/main/java/com/qituo/dcc/            # Forge 1.20.1 main mod sources
├── fabric/Dragon Curse Chronicles/src/     # Fabric 1.20.1 main mod sources
├── neoforge/src/main/java/com/qituo/dcc/   # NeoForge 1.21.1 main mod sources
├── DC Render API/                          # Particle API dependency subdirectory
│   ├── src/                                # Forge API
│   ├── fabric/DC Render API/               # Fabric API
│   └── neoforge/                           # NeoForge API
├── gradle.properties                       # Forge version numbering
├── README.md / README_en.md                # Chinese / English documentation
```

### Build Commands

```bash
# Forge 1.20.1 (repo root)
./gradlew build            # artifact → build/libs/dcc-0.2.4-rc-9.jar

# Fabric 1.20.1
cd fabric/Dragon\ Curse\ Chronicles
./gradlew build            # artifact → build/libs/dcc-1.0.6-1.20.1Fabric.jar

# NeoForge 1.21.1
cd neoforge
./gradlew build            # artifact → build/libs/dcc-0.1.0-1.21.1NeoForge.jar
```

A `-sources.jar` is generated automatically for every build.

---

## Roadmap

- Full implementations of Monkey (Polymorph) and Tiger (Good/Evil Split) Talismans.
- Polish and expand remaining feature details on the NeoForge 1.21.1 side.
- Talisman Box GUI & hotbar quick-switch.
- Replace the current Origin Power NBT-stub approach with a true datapack-driven enchantment registration.

---

## Contributing

Issues, feature requests, and pull requests are always welcome!

---

## License

This project is released under the **MIT License** — see [LICENSE.md](https://github.com/19136644525lxy/Dragon-Curse-Chronicles/blob/main/LICENSE.md).
