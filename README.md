# Traveler's Backpack - Homeostatic Bridge

A Minecraft Fabric mod that integrates Homeostatic's hydration system with Traveler's Backpack.

## Features

### Flask Filling
- **Flask as Fluid Container**: Homeostatic flasks (leather flask, etc.) work as fluid containers in Traveler's Backpack
- Place your flask in the backpack's fluid input slots to fill/empty it
- Works with any fluid that Homeostatic recognizes as hydratable

### Hose Drinking
Drinking from the backpack hose now affects Homeostatic hydration. Works with both regular water and purified water. Two drinking modes:

#### Dirty Water (unpurified)

**Gulp (normal use)**
- Uses 1 bucket of water
- Adds 12 hydration
- 95% chance of Thirst effect (potency 45, 10 seconds) - drains ~10 hydration
- 15 seconds of Nausea
- Net gain: ~2 hydration. Emergency option when you're desperate!

**Sip (sneak + use)**
- Uses 50mB of water (same as flask)
- Adds 1 hydration
- 20% chance of Thirst effect (potency 45, 10 seconds)
- No nausea
- Water-efficient and lower risk, but slower

#### Purified Water

**Gulp (normal use)**
- Uses 1 bucket of purified water
- Adds 12 hydration + 0.7 saturation
- No Thirst effect
- 15 seconds of Nausea (you still chugged a whole bucket)

**Sip (sneak + use)**
- Uses 50mB of purified water (same as flask)
- Adds 3 hydration + 0.7 saturation
- No Thirst effect
- No nausea
- The optimal way to drink from your backpack!

For best hydration, keep purified water in your backpack tanks and sip when needed.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.0+
- Fabric API
- [Traveler's Backpack](https://www.curseforge.com/minecraft/mc-mods/travelers-backpack-fabric)
- [Homeostatic](https://www.curseforge.com/minecraft/mc-mods/homeostatic)

## Installation

1. Install Fabric Loader and Fabric API
2. Install Traveler's Backpack and Homeostatic
3. Drop the mod JAR into your `mods` folder

## Usage

1. Equip a Traveler's Backpack (via Trinkets or in your chest slot)
2. Open the backpack inventory
3. Place your Homeostatic flask in one of the fluid input slots (next to the tanks)
4. The flask will fill from / empty into the tank automatically

## Building

Requires Java 25.

```bash
./gradlew :Fabric:build
```

The built JAR will be in `Fabric/build/libs/`.

## License

MIT
