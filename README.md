# Custom Server Mod

A Fabric mod for Minecraft **26.2** that adds:

1. A **custom crafting recipe for the Mace** (same stats as vanilla, just a different recipe).
2. A custom **Holzfäller (Lumberjack)** enchantment for axes.

## 1. Custom Mace Recipe

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

## 2. Holzfäller (Lumberjack) Enchantment

An enchantment for **axes** (levels 1-3) that turns an axe into a tree-feller.

Features:
- Breaking a tree log breaks the **entire connected trunk** (Treecapitator style).
- **Leaves are preserved** (only logs are removed).
- Works with **all wood types**.
- **Sneak** while breaking to **disable** the effect (normal single-block mining).
- Tool **durability** is reduced for every extra log broken.
- Compatible with **Fortune, Silk Touch, Unbreaking and Mending**.

## Requirements

- **Minecraft Java Edition 26.2**
- **Fabric Loader** 0.19.x
- **Fabric API** (required, the mod uses Fabric events)
- **Java 21+**

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 26.2.
2. Download **Fabric API** and place it in your `mods` folder.
3. Put the compiled `.jar` of this mod in your `mods` folder.
4. Start the game.

## Building

The GitHub Actions workflow builds the mod and publishes a release automatically on every push to `main`. You can also build locally:

```bash
gradle build
```

The built jar will be in `build/libs/`.

## License

MIT
