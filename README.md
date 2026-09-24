# Traveler's Backpack - Homeostatic Bridge

A Minecraft Fabric mod that integrates Homeostatic flasks with Traveler's Backpack fluid tanks.

## Features

- **Flask as Fluid Container**: Homeostatic flasks (leather flask, etc.) work as fluid containers in Traveler's Backpack
- Place your flask in the backpack's fluid input slots to fill/empty it
- Works with any fluid that Homeostatic recognizes as hydratable

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
