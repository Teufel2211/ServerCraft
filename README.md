# ServerCraft

**ServerCraft** — Fabric 26.2 Chaos Cubed mod, **benötigt auf Client + Server** (`environment: *`). Fügt Infinite Totem, Enchantments, Pizza + Ofen, Mace-Rezept und MsgSpy hinzu.

**Wiki:** https://github.com/Teufel2211/ServerCraft/wiki — **Releases:** https://github.com/Teufel2211/ServerCraft/releases

## 1. Mace Rezept

Gleiche Stats wie Vanilla, anderes Rezept (3×3):

```
DiamondBlock | NetherStar | DiamondBlock
FlowArmorTrim| HeavyCore  | FlowArmorTrim
DiamondBlock | BreezeRod  | DiamondBlock
```

## 2. Enchantments

| Enchant | Items | Level | Farbe | Treasure | Weight | Erhalt |
|---|---|---|---|---|---|---|
| **Lumberjack** `custom-server-mod:lumberjack` | Nur Äxte | 1-3 | Gold | Nein | 50 | Zaubertisch, Bibliothekar (jedes Level, ~15-20% pro Slot), Amboss |
| **Telekinesis** `custom-server-mod:telekinesis` | Axt, Spitzhacke, Schaufel | 1 | Aqua | Nein | 50 | Wie Lumberjack, + Minecarts/Boote (Vehicle Mixin) |
| **Excavation** `custom-server-mod:excavation` | Nur Spitzhacke | 1 | Dark Purple | **Ja** | 1 | **Nur Ancient City ~5%** (`weight 1`, 5-10 Rolls → ~6-11%) |
| **Auto Smelting** `custom-server-mod:auto_smelting` | Nur Spitzhacke | 1 | Green | Nein | 5 | Zaubertisch, Bibliothekar, nicht mit Lumberjack, mit Telekinesis/Excavation kombinierbar |

- **Lumberjack:** Ganzer Stamm fällt (6-Richtungen Flood-Fill, 256 Cap), Blätter bleiben, Sneak deaktiviert, 1 Haltbarkeit pro Log, `isUnbreakable` (Bedrock etc.) Schutz
- **Telekinesis:** Drops direkt ins Inventar, für Blöcke via `TelekinesisMixin` (`Block.dropResources` → Inventory, `ci.cancel()`), für Vehicles via `VehicleTelekinesisMixin`, für zusätzliche Blöcke via `CombinedEnchantmentHandler` (`Block.getDrops` → Inventory)
- **Excavation:** **C1 3×3×1 gerichtet** nach Blick (UP/DOWN→XZ, NORTH/SOUTH→XY, EAST/WEST→YZ), 9 Blöcke (8 Nachbarn), **0 Haltbarkeit** für 8 Nachbarn (nur Origin kostet via Vanilla)
- **Auto Smelting:** Schmilzt Erze via `RecipeType.SMELTING` (`trySmelt`), nicht mit Lumberjack

## 3. Infinite Totem

- **Item:** Vanilla `minecraft:totem_of_undying` mit `custom_data:{InfiniteTotem:1b}`, Gold-Name „Infinite Totem“, Lore, Glint — **crafted** 8× Netherite Block um 1× Totem (Mitte) in 3×3 (`data/custom-server-mod/recipe/infinite_totem.json`, `crafting_shaped`, `components` mit `custom_name`/`lore`/`custom_data` als Component-Objekte, Fix für `text` sichtbar)
- **Effekt:** `LivingEntity.checkTotemDeathProtection` Mixin, prüft Hand/Offhand/Inventar auf Infinite Totem, `isOnCooldown(ItemStack)` (600 Ticks = 30s), wenn nicht auf Cooldown: `setHealth(1)`, `REGENERATION 900/1`, `ABSORPTION 100/1`, `FIRE_RESISTANCE 800/0`, `broadcastEntityEvent 35`, `addCooldown(ItemStack,600)`, **nicht verbraucht**

## 4. Pizza + Ofen

- **Items:** `pizza` (8 Hunger, 0.8, 16 Stack, Textur 16×16), `tomato` (2, 0.3, beetroot→2), `cheese` (3, 0.4, milk_bucket→3), `pizza_dough` (3× wheat + water_bucket →2)
- **Pizza:** `pizza_dough` + `tomato` + `cheese` (shapeless) → 1× Pizza
- **Ofen `pizza_oven`:** Block `2.0/6.0`, `requiresCorrectToolForDrops`, Rechtsklick prüft Inventar auf je 1× Teig/Tomate/Käse → verbraucht, gibt Pizza (Inventory oder `popResource`), Sound `SMOKER_SMOKE`, Chat, Rezept 8× `bricks` um `furnace`, Textur 16×16 Ziegel+Fenster, `blockstates`/`models`/`loot_table`

## 5. MsgSpy

- **Befehle:** `/msgspy`, `/socialspy` — **nur OP 4** (`Permissions.COMMANDS_OWNER` via `permissions().hasPermission`), toggelt `MsgSpyManager` (ConcurrentHashMap), `MsgCommandMixin` auf `MsgCommand.sendMessage` (TAIL) leitet an alle `permissions().hasPermission(COMMANDS_OWNER) && isSpyEnabled` als `[Spy] sender -> target: msg`

## 6. AutoUpdater

- **Check on `SERVER_STARTED`** — vergleicht `FabricLoader` Version mit `https://api.github.com/repos/Teufel2211/ServerCraft/releases/latest` (`tag_name` ohne `v`), notifiziert OPs, lädt erstes Asset nach `mods/update/` (Fabric Update Dir) falls neuer

## Installation

1. **Fabric Loader 0.19.3** + **Fabric API 0.158.0+26.2** + **Java 25** auf **Client und Server** (beide brauchen Mod, `environment: *`)
2. `ServerCraft-1.0.XX.jar` aus **Releases** in beide `mods/` (exakt gleiche Version, sonst `Registry remapping failed custom-server-mod`)
3. Server starten — `Found new data pack custom-server-mod`

Test:
```
/enchant @p custom-server-mod:lumberjack 3
/enchant @p custom-server-mod:telekinesis 1
/enchant @p custom-server-mod:excavation 1
/enchant @p custom-server-mod:auto_smelting 1
/give @p minecraft:totem_of_undying[custom_data={InfiniteTotem:1b}] 1
/give @p custom-server-mod:pizza_oven 1
/msgspy
```

## Build & Version

- **Gradle 9.5.0**, **Loom 1.17.20**, **Java 25**, `minecraft 26.2`, `fabric 0.158.0+26.2`
- **Auto-Bump:** `1.0.X` mit Rollover bei 10 (`1.0.10→1.1.0`, `1.10.10→2.0.0`), **nur bei erfolgreichem Build** gepusht (vorher lokal `commit`, nach `gradle build --scan` dann `git push`), `[skip ci]` vermeidet Loops, `contents: write` für Releases
- **Branch Protection `main`:** `enforce_admins: false` (Workflow darf pushen), kein `required_pull_request_reviews` (direct push erlaubt, aber kein Force/Delete), nur **Merge Commit** (`allow_squash/allow_rebase: false`, `allow_merge_commit: true` — sicherste, bewahrt History)

## Wiki & Changelog

- **Wiki:** https://github.com/Teufel2211/ServerCraft/wiki — Home, Installation, Enchantments, Recipes, Commands, **Changelog**
- **Changelog:** Alle Änderungen seit 2026-08-29 (26.2 Migration, Mace, Enchants, Totem, Pizza, Ofen, MsgSpy, Updater, Branch Protection, Rename)

## Lizenz

MIT
