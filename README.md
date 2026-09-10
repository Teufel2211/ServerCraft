# ServerCraft

**Teufel's Essentials** — A **server-side only** Fabric mod for Minecraft **26.2 Chaos Cubed** (no client mod needed). Adds Infinite Totem, custom enchantments, and a Mace recipe.

## 1. Custom Mace Recipe

Same stats as vanilla Mace, different recipe:

| Slot | Item |
|------|------|
| Top row | Diamond Block, Nether Star, Diamond Block |
| Middle row | Flow Armor Trim, Heavy Core, Flow Armor Trim |
| Bottom row | Diamond Block, Breeze Rod, Diamond Block |

```
DiamondBlock | NetherStar | DiamondBlock
FlowArmorTrim| HeavyCore  | FlowArmorTrim
DiamondBlock | BreezeRod  | DiamondBlock
```

## 2. Enchantments

### Holzfäller (Lumberjack) — `custom-server-mod:lumberjack`
- **Items:** Only **axes** (`#custom-server-mod:enchantable/axes`)
- **Levels:** 1–3 — Gold/Orange (`gold`)
- **Treasure:** No — enchanting table, anvil, librarians (`weight 50`, very common, every level)

### Telekinesis — `custom-server-mod:telekinesis`
- **Items:** Axe, Pickaxe, Shovel (`#custom-server-mod:enchantable/mining_tools`) — Level 1, Aqua, `weight 50`
- Drops go directly into inventory, works with **all** custom enchantments (Lumberjack, Excavation, Auto Smelting) via combined handler + `TelekinesisMixin` for blocks + `VehicleTelekinesisMixin` for minecarts/boats

### Excavation — `custom-server-mod:excavation`
- **Items:** Only pickaxes (`#custom-server-mod:enchantable/pickaxes`) — Level 1, Dark Purple, `weight 1`
- **Treasure:** Yes — **only Ancient City chests, ~5% per chest** (`weight 1` in `ancient_city.json`, 5–10 rolls → ~6–11%)
- **Effect:** **C1 directional 3×3×1** (9 blocks, 1 layer thick) perpendicular to look direction (UP/DOWN→XZ, NORTH/SOUTH→XY, EAST/WEST→YZ), **no bedrock**, **0 durability for the 8 extra blocks** (only origin costs 1)

### Auto Smelting — `custom-server-mod:auto_smelting`
- **Items:** Only pickaxes (`#custom-server-mod:enchantable/pickaxes`) — Level 1, Green, `weight 5`
- **Treasure:** No — enchanting table, librarians
- **Effect:** Smelts ores on break (via `RecipeType.SMELTING` lookup, `trySmelt`), not with Lumberjack, works with Telekinesis (smelted → inventory) and Excavation

## 3. Infinite Totem

- **Item:** Vanilla `minecraft:totem_of_undying` with `custom_data:{InfiniteTotem:1b}`, gold name "Infinite Totem", lore, glint — **crafted** with 8× `minecraft:netherite_block` surrounding 1× `minecraft:totem_of_undying` (`data/custom-server-mod/recipe/infinite_totem.json`)
- **Effect:** When holding/offhand/inventory and would die, `LivingEntity.checkTotemDeathProtection` mixin triggers: `setHealth(1)`, `REGENERATION 900/1`, `ABSORPTION 100/1`, `FIRE_RESISTANCE 800/0`, `broadcastEntityEvent 35`, **30s cooldown** (`600 ticks` via `ItemCooldowns.addCooldown(ItemStack,600)`), **not consumed** (infinite)
- **Server-only:** Uses vanilla totem NBT, no custom item registry, no client mod needed

## 4. Wahrscheinlichkeiten

| Enchantment | Table | Librarian | Loot | Weight |
|---|---|---|---|---|
| Lumberjack | ✅ | ✅ tradeable | — | 50 |
| Telekinesis | ✅ | ✅ tradeable | — | 50 |
| Auto Smelting | ✅ | ✅ tradeable | — | 5 |
| Excavation | ❌ | ❌ | Ancient City ~5% | 1 |

Tradeable pool = `non_treasure` (33) + curses/mending (4) + our 3 tradeable → ~40, weight 50 → ~15–20% per book slot, every level.

## 5. Other Features

- **MsgSpy:** `/msgspy` / `/socialspy` (requires `Permissions.COMMANDS_OWNER` / OP 4) — toggles spy, `MsgCommandMixin` forwards all `/msg`/`/tell`/`/w` as `[Spy] sender -> target: msg` to spies
- **Bedrock protection:** `isUnbreakable` (BEDROCK, BARRIER, END_PORTAL, etc., `getDestroySpeed <0`) for Lumberjack/Excavation
- **Versioning:** Auto-bump `1.0.X` with rollover at 10 (`1.0.10→1.1.0`, `1.10.10→2.0.0`), only pushed if `gradle build` succeeds (`--scan` enabled)

## Requirements

- Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.158.0+26.2, Java 25, Gradle 9.5.0, Loom 1.17.20
- **Server-only** (`environment: server`, `DedicatedServerModInitializer`) — enchantments via datapack + mixins, totem via NBT, all synced, no client mod

## Installation

1. Fabric Loader 0.19.3 for 26.2 on server
2. Fabric API + ServerCraft `.jar` in `mods/`
3. Start — `Found new data pack custom-server-mod`

Test:
```
/enchant @p custom-server-mod:lumberjack 3
/enchant @p custom-server-mod:telekinesis 1
/enchant @p custom-server-mod:excavation 1
/enchant @p custom-server-mod:auto_smelting 1
/give @p minecraft:totem_of_undying[custom_data={InfiniteTotem:1b},custom_name='{"text":"Infinite Totem","color":"gold"}'] 1
/msgspy
```

## Building

```bash
gradle build --no-daemon --scan
```

## License

MIT
