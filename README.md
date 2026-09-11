# ServerCraft

Fabric-Mod für **Minecraft 26.2** — **Client + Server Pflicht** (`environment: "*"`).

**Releases:** https://github.com/Teufel2211/ServerCraft/releases · **Wiki:** https://github.com/Teufel2211/ServerCraft/wiki

> **Wichtig:** Client und Server brauchen **exakt gleiche** `ServerCraft-1.4.x.jar` in `mods/`. Mismatch → `Registry remapping failed` / Kick.

## Installation

1. **Fabric Loader 0.19.3** + **Fabric API 0.158.0+26.2** (Client + Server)
2. `ServerCraft-1.4.9.jar` aus Releases in **beide** `mods/`
3. Server starten → `Found new data pack custom-server-mod`

Details + Rezepte → Wiki.

## Features

- **Mace** — alternatives 3×3-Rezept (Vanilla-Stats)
- **Enchantments** — Lumberjack (1-3), Telekinesis, Excavation (Ancient City), Auto Smelting
- **Infinite Totem** — Vanilla-Totem mit `custom_data:{InfiniteTotem:1b}`, 30s Cooldown
- **Pizza** — Tomate, Käse, Teig → Pizza + **Pizza-Ofen** (6 Texturen)
- **MsgSpy** — `/msgspy` für OPs
- **AutoUpdater** — prüft `releases/latest` bei Server-Start, lädt nach `mods/update/`

Alle Details, Rezepte und Befehle im **Wiki**.

## Troubleshooting

- `Registry remapping failed` / `Version Mismatch` → gleiche Version auf beiden Seiten, F3+T
- Pink/schwarze Texturen → Version 1.4.9+, F3+T

## Lizenz

MIT
