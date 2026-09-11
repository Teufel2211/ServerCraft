# ServerCraft

**ServerCraft** — Fabric-Mod für **Minecraft 26.2 Chaos Cubed**, **Client + Server Pflicht** (`environment: "*"`).

Fügt Mace-Rezept, 4 Custom-Enchantments, Infinite Totem, Pizza-Kette + 6-Face Pizza-Ofen und MsgSpy hinzu.

**Releases:** https://github.com/Teufel2211/ServerCraft/releases · **Wiki:** https://github.com/Teufel2211/ServerCraft/wiki · **Version:** 1.4.9

> **Wichtig:** Client **und** Server brauchen **exakt gleiche** `ServerCraft-1.4.x.jar` in `mods/`. Bei `Registry remapping failed: custom-server-mod` → Versionen angleichen.

## Features

- **Mace** — gleich wie Vanilla, alternatives 3×3-Crafting
- **Enchantments** — Lumberjack (3 Lv.), Telekinesis, Excavation (Treasure), Auto Smelting
- **Infinite Totem** — Vanilla-Totem mit `custom_data:{InfiniteTotem:1b}`, 30s Cooldown, unendlich
- **Pizza** — Tomate, Käse, Teig → Pizza (8 Hunger) + **Pizza-Ofen** (6 Texturen)
- **MsgSpy** — `/msgspy` / `/socialspy` für OPs
- **AutoUpdater** — prüft bei `SERVER_STARTED` GitHub Releases, lädt nach `mods/update/`

## Voraussetzungen

| Komponente | Version |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.158.0+26.2 |
| Java | 25 (Temurin Hotspot) |
| Gradle | 9.5.0 / Loom 1.17.20 |

Beide Seiten: Loader + API + ServerCraft.jar in `mods/`.

## Schnellstart (Nitrado / Dedicated)

1. Fabric Loader 0.19.3 für 26.2 installieren
2. `fabric-api-0.158.0+26.2.jar` + `ServerCraft-1.4.9.jar` (aus Releases) in `mods/` auf **Client und Server**
3. Server starten → Log: `Found new data pack custom-server-mod` + `[ServerCraft] Server initialized: Lumberjack + ... + AutoUpdater + VersionCheck`

Test im Spiel:
```
/enchant @p custom-server-mod:lumberjack 3
/enchant @p custom-server-mod:telekinesis 1
/enchant @p custom-server-mod:excavation 1
/enchant @p custom-server-mod:auto_smelting 1
/give @p minecraft:totem_of_undying[custom_data={InfiniteTotem:1b}] 1
/give @p custom-server-mod:pizza_oven 1
/msgspy
```

Bei `Version Mismatch! Server: x Client: y` → gleiche Jar auf beiden Seiten nutzen. Reconnect nach F3+T falls Texturen fehlen.

## 1) Mace-Rezept (Vanilla-Stats)

```
DiamondBlock | NetherStar | DiamondBlock
FlowArmorTrim| HeavyCore  | FlowArmorTrim
DiamondBlock | BreezeRod  | DiamondBlock
```
`data/custom-server-mod/recipe/mace.json` — `crafting_shaped`, 3×3.

## 2) Enchantments

| Enchant | ID | Ziel | Lv | Farbe | Treasure | Weight | Erhalt |
|---|---|---|---|---|---|---|---|
| **Lumberjack** | `lumberjack` | Nur Äxte | 1-3 | Gold | Nein | 50 | Zaubertisch, Bibliothekar (~15-20% pro Slot, jedes Lv.), Amboss |
| **Telekinesis** | `telekinesis` | Axt, Spitzhacke, Schaufel | 1 | Aqua | Nein | 50 | Wie Lumberjack + Minecarts/Boote via Vehicle-Mixin |
| **Excavation** | `excavation` | Nur Spitzhacke | 1 | Dark Purple | **Ja** | 1 | Nur Ancient City ~5% (weight 1, 5-10 Rolls → ~6-11%) |
| **Auto Smelting** | `auto_smelting` | Nur Spitzhacke | 1 | Grün | Nein | 5 | Zaubertisch/Bibliothekar, nicht mit Lumberjack, kombi mit Telekinesis/Excavation |

**Details:**
- **Lumberjack:** 6-Richtungen Flood-Fill, Cap 256, Blätter bleiben, Sneak deaktiviert, 1 Haltbarkeit pro Log, schützt `isUnbreakable` (Bedrock, Barrier, End Portal, `getDestroySpeed<0`). `TreeFeller.java` guard verhindert Doppel-Listener bei `environment:*`.
- **Telekinesis:** Drops direkt ins Inventar. Blöcke via `TelekinesisMixin` (`Block.dropResources` → `ci.cancel()`), Vehicles via `VehicleTelekinesisMixin`, weitere Blöcke via `CombinedEnchantmentHandler` (`Block.getDrops`).
- **Excavation:** 3×3×1 **gerichtet nach Blick** — UP/DOWN → XZ-Ebene, NORTH/SOUTH → XY, EAST/WEST → YZ. 9 Blöcke, 8 Nachbarn kosten **0 Haltbarkeit** (nur Origin via Vanilla).
- **Auto Smelting:** Schmilzt Erze via `RecipeType.SMELTING` (`trySmelt`), exklusiv mit Lumberjack.

Anmeldung: `CustomServerMod.java` (main) + `CustomServerModServer.java` (server) beide mit `static registered` Guard.

## 3) Infinite Totem

Vanilla `minecraft:totem_of_undying` mit `components: { custom_name: "Infinite Totem" (gold), lore, custom_data: {InfiniteTotem:1b}, enchantment_glint_override: true }`.

- **Rezept:** 8× `minecraft:netherite_block` um 1× Totem (Mitte) — `data/custom-server-mod/recipe/infinite_totem.json`.
- **Effekt:** Mixin `LivingEntity.checkTotemDeathProtection` prüft Hand/Offhand/Inventar, `isOnCooldown(stack)` 600 Ticks (30s). Wenn frei: `setHealth(1)`, `REGENERATION 900/1`, `ABSORPTION 100/1`, `FIRE_RESISTANCE 800/0`, `broadcastEntityEvent 35`, `addCooldown(stack,600)`, **wird nicht verbraucht**.

## 4) Pizza & Pizza-Ofen

| Item | ID | Nahrung | Rezept |
|---|---|---|---|
| Tomate | `tomato` | 2 / 0.3 | `beetroot` → 2 |
| Käse | `cheese` | 3 / 0.4 | `milk_bucket` → 3 |
| Teig | `pizza_dough` | — | 3× `wheat` + `water_bucket` → 2 (shapeless) |
| Pizza | `pizza` | 8 / 0.8 (16 Stack) | `pizza_dough` + `tomato` + `cheese` → 1 |

**Pizza-Ofen `pizza_oven`:**
- Block `2.0 / 6.0`, `requiresCorrectToolForDrops`, 8× `bricks` um `furnace` → Ofen
- Rechtsklick: prüft Inventar auf je 1× Teig/Tomate/Käse → verbraucht, gibt Pizza (Inventar oder `popResource`), Sound `SMOKER_SMOKE`
- **6 Texturen:** `textures/block/Front.png`, `Up.png`, `Down.png`, `Back.png`, `Left.png`, `Right.png` (je 16×16) via `models/block/pizza_oven.json` multipart (`custom-server-mod:block/front` etc.). Fallback `pizza_oven.png` gelöscht. `blockstates/pizza_oven.json` + `loot_table`.
- Kategorie: `misc` (26.2 Fix `food→misc`), Ingredient-IDs als String.

## 5) MsgSpy

`/msgspy` / `/socialspy` — **nur OP Stufe 4** (`Permissions.COMMANDS_OWNER`). Toggelt `MsgSpyManager` (ConcurrentHashMap). `MsgCommandMixin` (TAIL auf `MsgCommand.sendMessage`) leitet an alle OPs mit Spy als `[Spy] sender -> target: msg`.

## 6) AutoUpdater & VersionCheck

- **VersionCheck:** `VersionChecker.java` / `VersionCheckerClient.java` — Login-Handshake via `ServerLoginNetworking` (`custom-server-mod:version_check`). Mismatch → Kick mit Link zu Releases. Log: `[ServerCraft] VersionCheck registered (server version x)`.
- **AutoUpdater:** `updater/AutoUpdater.java` — `ServerLifecycleEvents.SERVER_STARTED.register(AutoUpdater::checkOnStartup)` in `CustomServerModServer.java`. Liest aktuelle Version via `FabricLoader`, `GET https://api.github.com/repos/Teufel2211/ServerCraft/releases/latest` (`tag_name` ohne `v`, Header `Accept: application/vnd.github+json`), `isNewer()`. Bei Update: Log + Broadcast an OPs + Download erstes Asset nach `mods/update/<jar>` (erstellt `mods/update` falls nötig, skip wenn vorhanden). **Aktiv erst nach nächstem Neustart** (Fabric übernimmt aus `update`). Unauthentifiziertes Limit 60 req/h — 1 Check pro Start unkritisch. Deaktivieren: Aufruf in `CustomServerModServer.java` entfernen.

## Troubleshooting

- `Registry remapping failed custom-server-mod` → gleiche Jar auf Client + Server, beide neu starten, F3+T
- Pink/schwarze Texturen → `Front.png` etc. existieren? `models/block/pizza_oven.json` Referenzen prüfen, F3+T, `mods/` Version 1.4.9+
- `List is too short` bei Rezepten → alte Kategorie `food` oder falsches Ingredient-Format (muss String-ID sein, Fix seit 1.4.6)
- Build fail `DedicatedServerModInitializer not found` → `import net.fabricmc.api.DedicatedServerModInitializer` fehlt (Fix in 1.4.9)

## Build & Entwicklung

```
./gradlew build --scan   # Java 25, Loom 1.17-SNAPSHOT
```
- **Version:** `gradle.properties` `version=1.4.9` + `fabric.mod.json` `version` immer zusammen bumpen. `AGENTS.md` beachten.
- **Auto-Bump:** CI bumpt `1.0.X` mit Rollover bei 10 (`1.0.10→1.1.0`, `1.10.10→2.0.0`) nur bei erfolgreichem Build, `[skip ci]` vermeidet Loops.
- **Branches:** `main` (protected, nur Merge Commit) ↔ `dev` (aktiv). Vor Feature: `main→dev` syncen, danach `dev→main` PR.
- **Tests:** Beide Seiten auf gleiche Version, F3+T, JEI (U) → Misc-Rezepte, Enchants via `/enchant`.

## Wiki & Changelog

- **Wiki:** https://github.com/Teufel2211/ServerCraft/wiki — Home · Installation · Enchantments · Recipes · Commands · AutoUpdater · Changelog
- **Changelog:** `wiki/Changelog.md` — alle Änderungen seit 2026-08-29 (26.2-Migration, Mace, Enchants, Totem, Pizza/Ofen 6-Face, MsgSpy, Updater, Branch Protection, Rename Custom Server Mod → ServerCraft)

## Lizenz

MIT — siehe `LICENSE`
