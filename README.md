# Custom Server Mod

A **server-side** Fabric mod for Minecraft **26.2** that adds custom enchantments and a Mace recipe.

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
- **Items:** Only **axes** (`#custom-server-mod:enchantable/axes` → all axe types)
- **Levels:** 1–3
- **Color:** Gold/Orange (`gold`)
- **Treasure:** No — obtainable via enchanting table, anvil, librarians
- **Features:**
  - Breaking a log breaks the **entire connected trunk** (Treecapitator).
  - **Leaves preserved.**
  - All wood types. **Sneak to disable.**
  - Durability per extra log, compatible with **Fortune/Silk Touch/Unbreaking/Mending**.

### Telekinesis — `custom-server-mod:telekinesis`
- **Items:** **Axe, Pickaxe, Shovel** (`#custom-server-mod:enchantable/mining_tools`)
- **Levels:** 1 (single level)
- **Color:** Aqua/Türkis (`aqua`)
- **Treasure:** No — enchanting table, anvil, librarians
- **Features:**
  - Drops go **directly into inventory** (no ground entities).
  - Works **together with all custom enchantments** (Lumberjack, Excavation, etc.) — additional blocks' drops also go to inventory via the combined handler.

### Excavation — `custom-server-mod:excavation`
- **Items:** Only **pickaxes** (`#custom-server-mod:enchantable/pickaxes`)
- **Levels:** 1
- **Color:** Dark Purple (`dark_purple`)
- **Treasure:** **Yes** — **NOT** obtainable via enchanting table or librarians
- **Features:**
  - Mines a **true 3×3×3 cube (27 blocks)** centered on the broken block (26 additional + origin).
  - Only on pickaxes. Drops go to inventory if Telekinesis also present (combined handler).

## 3. Wahrscheinlichkeiten / How to Obtain

| Enchantment | Enchanting Table | Librarian (Lectern) | Loot | Weight | Notes |
|---|---|---|---|---|---|
| **Lumberjack** | ✅ Yes | ✅ Yes — `tradeable` tag | — | **50** (very common) | `weight 50` → ~18–20% per book slot (vanilla pool avg ~5). Tradeable pool = `non_treasure` (~33 vanilla) + `binding_curse`, `vanishing_curse`, `frost_walker`, `mending` + our 2 → ~37 total, vanilla total weight ~165, ours 50 each → ~15–20% per slot, appears at **every librarian level** (Novice–Master). |
| **Telekinesis** | ✅ Yes | ✅ Yes — `tradeable` tag | — | **50** (very common) | Same pool/math as Lumberjack. Works on axe/pickaxe/shovel, single level, aqua display. |
| **Excavation** | ❌ No (treasure) | ❌ No | **Ancient City chest only — ~20% per chest** | **1** (rare) | Added to `data/minecraft/loot_table/chests/ancient_city.json` as `minecraft:book` with `enchant_randomly: custom-server-mod:excavation`, `weight 4` in first pool (5–10 rolls, total pool weight ~84 → `4/84 ≈ 4.8%` per roll, `1-(1-0.048)^5 ≈ 21%`, `^10 ≈ 38%` → **~20–38% per chest** depending on rolls). Exclusive with mining enchantments. |

### Librarian Details (26.2)
Vanilla 26.2 uses `data/minecraft/tags/enchantment/tradeable.json`:
```json
{ "values": ["#minecraft:non_treasure", "minecraft:binding_curse", "minecraft:vanishing_curse", "minecraft:frost_walker", "minecraft:mending"] }
```
Where `non_treasure` is a **hardcoded list of 33 vanilla non-treasure enchantments**. Mod enchantments are **NOT** auto-included. We add via datapack override:
```json
{ "replace": false, "values": ["custom-server-mod:lumberjack", "custom-server-mod:telekinesis"] }
```
Excavation is **not** in `tradeable`, so librarians never offer it.

### Ancient City Details
Vanilla `ancient_city.json` first pool: `rolls 5–10`, ~84 total weight, our book `weight 4` → ~20% per chest (5 rolls) → ~38% (10 rolls). Average **~25–30%**. Second pool (templates) unchanged.

## Requirements

- **Minecraft Java Edition 26.2**
- **Fabric Loader** 0.19.3
- **Fabric API** 0.158.0+26.2
- **Java 25** (Minecraft 26.2 requires Java 25, Gradle 9.5.0, Loom 1.17.20)
- **Server-only** (`environment: server`, `DedicatedServerModInitializer`) — datapack resources sync to clients via Fabric Registry Sync; no client mod needed.

## Installation (Nitrado / Dedicated Server)

1. Install **Fabric Loader 0.19.3** for 26.2 on the server.
2. Put **Fabric API 0.158.0+26.2** and this mod's `.jar` in `mods/`.
3. Start server — datapack loads automatically (`Found new data pack custom-server-mod`).

Test commands:
```
/enchant @p custom-server-mod:lumberjack 3
/enchant @p custom-server-mod:telekinesis 1
/enchant @p custom-server-mod:excavation 1
```

## Building

GitHub Actions builds on every `main` push (auto-bumps `version` patch `1.0.X → 1.0.X+1`, `[skip ci]` to avoid loops, `contents: write` for releases):

```bash
gradle build --no-daemon --scan
# jar in build/libs/
```

Local JDK must be **25**.

## License

MIT
