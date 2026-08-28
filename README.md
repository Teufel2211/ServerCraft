# Custom Server Mod

A Fabric mod for Minecraft **1.21.1** that adds a **custom crafting recipe for the Mace** (same stats as vanilla, just a different recipe).

## Recipe

To craft the Mace you need:

| Slot | Item |
|------|------|
| Top row | Diamond Block, Nether Star, Diamond Block |
| Middle row | Flow Armor Trim, Heavy Core, Flow Armor Trim |
| Bottom row | Diamond Block, Breeze Rod, Diamond Block |

Pattern:
```
DiamondBlock | NetherStar | DiamondBlock
FlowArmorTrim| HeavyCore  | FlowArmorTrim
DiamondBlock | BreezeRod  | DiamondBlock
```

## Requirements

- **Minecraft Java Edition 1.21.1**
- **Fabric Loader** 0.16.x
- **Fabric API** 0.109.0
- **Java 21**

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.1.
2. Download **Fabric API** and place it in your `mods` folder.
3. Put the compiled `.jar` of this mod in your `mods` folder.
4. Start the game.

## Building

```bash
./gradlew build
```

The built jar will be in `build/libs/`.

## License

MIT
