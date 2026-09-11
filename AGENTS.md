# AGENTS.md — ServerCraft

Guidance for AI coding agents working on this repository.

## Project Overview

ServerCraft is a Fabric mod for Minecraft 26.2. Mod ID `custom-server-mod`, current version 1.4.7. It adds pizza content (pizza, tomato, cheese, pizza_dough, pizza_oven), mace recipe, custom enchantments (TreeFeller/Lumberjack, CombinedEnchantmentHandler), VersionChecker, MsgSpy, and debug commands.

## Environment & Entry points

- `fabric.mod.json` `environment: "*"` — mod must run on **both** client and server. All players need same `ServerCraft-1.4.x.jar` in `mods/`.
- Entry points: `main` (`CustomServerMod`), `client` (`CustomServerModClient`), `server` (`CustomServerModServer`).
- DedicatedServer runs both `main` + `server`. Use `static boolean registered` guards in `TreeFeller.java:1` and `CombinedEnchantmentHandler.java:1` to avoid double listener registration.
- Pizza registry (blocks/items) lives in `CustomServerMod.java:1` (common). Server-only handlers in `CustomServerModServer.java:1`.

## Repo & Branches

- GitHub: `Teufel2211/ServerCraft`
- `main` — stable, protected
- `dev` — active development. Keep in sync: merge `main` → `dev` before feature work, merge `dev` → `main` via PR when stable.
- Local project does NOT exist under `C:\Users\Steven` outside GitHub (except `C:\Users\Steven\Minecraft farbric Server`). Work **directly on GitHub** via `gh` / API. Do not create local folders unless user confirms.

## Key Directories

```
src/main/java/com/example/customservermod/  # java sources
src/main/resources/
  fabric.mod.json
  custom-server-mod.mixins.json
  assets/custom-server-mod/
    blockstates/pizza_oven.json              # blockstate -> model
    models/block/pizza_oven.json             # multipart: front/up/down/back/left/right
    models/item/                             # item models
    textures/block/                          # front.png, up.png, down.png, back.png, left.png, right.png + pizza_oven.png
    textures/item/                           # cheese.png, pizza.png, pizza_dough.png, tomato.png
    lang/en_us.json
    recipes/                                 # legacy path (do not use)
  data/custom-server-mod/
    recipe/                                  # cheese.json, pizza.json, pizza_dough.json, tomato.json, mace.json, pizza_oven.json
    enchantment/                             # enchantment definitions
```

Model `models/block/pizza_oven.json:1` uses `multipart` with 6 face textures (`custom-server-mod:block/front` etc.). Textures are 16x16 PNG. Single `pizza_oven.png` is legacy; prefer 6-face set.

Recipes: `category: "misc"` (not `food`), ingredients as string IDs `"minecraft:item"` for 26.2. `recipe/mace.json:1` is 3x3 shaped (Diamond Block / Nether Star / Breeze Rod / Heavy Core).

## Build & Test

- Build: `./gradlew build` (or `gradlew.bat build` on Windows)
- Run server: `fabric-server-launch.jar` in `Minecraft farbric Server/` or `java -jar fabric-server-launch.jar nogui`
- Always bump `gradle.properties:mod_version` + `fabric.mod.json:version` together. CI bumps with `[skip ci]`.
- Test: update both server and client to same jar, press F3+T to reload textures, check JEI (U) for Misc recipes, verify enchantments not doubled.

## Rules for Agents

- Edit only `src/main/resources/assets/...` and `src/main/java/...` as needed. Do not edit synced/generated files without checking.
- Never change `environment` away from `*` without explicit request.
- When fixing textures/models, ensure `textures/block/<name>.png` exists for every reference in `models/block/*.json`.
- Do not commit secrets. Use `safeWrite` style for flags if touching hooks.
- After changes, verify build compiles: look for `Loaded 1824 recipes` and no `List is too short`.
