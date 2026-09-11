# Library of Babel (lob) — project notes for Claude

NeoForge mod (Java only, no Kotlin) that generates endless library dimensions made of 16x16x16 room cells.
Mod id `lob`, packages `com.dpeter99.lob` and `com.dpeter99.borgeslib`.

## Toolchain

- Minecraft 26.2, NeoForge **26.2.0.86**, ModDevGradle 2.0.146, Gradle 9.2.1, Java 25 toolchain.
- No system Java. Launch Gradle with
  `JAVA_HOME=~/.gradle/jdks/eclipse_adoptium-21-amd64-linux/jdk-21.0.3+9 ./gradlew <task>`;
  the toolchain resolver downloads Java 25 for compilation.
- Do not bump to NeoForge 26.2.0.87: its access transformer for `HolderSet$Named.contents()` breaks the
  NeoForm recompile in userdev. Check newer builds before upgrading.
- Decompiled Minecraft/NeoForge 26.2 sources for API lookups: unzip
  `~/.gradle/caches/neoformruntime/intermediate_results/transformSources_*_output.zip`
  (a copy lives in `~/.cache/lob-src26`). Prefer grepping these over guessing method names; 26.x renamed
  many things (`ResourceLocation`→`Identifier`, `noCollission`→`noCollision`, `ChunkPos` is a record,
  `Player.sendOverlayMessage`, `Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)`).

## Commands

```
./gradlew build       # compile + unit tests + jar (build/libs)
./gradlew test        # JUnit (ModDevGradle unitTest gives tests the Minecraft classpath)
./gradlew runData     # regenerate src/generated/resources (commit the output)
./gradlew runServer   # dedicated dev server in run/ (RCON on 25575, password lobdev)
./gradlew runClient
```

Enter a library in game: `/execute in lob:library_alpha run tp @s 8 17 8`. `/lob cell` prints the resolved
room at your position.

## Layout

- `world/CellPos`, `world/CellHash` — pure cell math and seeded hashing; everything deterministic derives from these.
- `room/RoomDefinition`, `room/LibraryDefinition` — datapack records (`data/<ns>/lob/library_room/`, `data/<ns>/lob/library/`).
- `room/RoomResolver` — pure, unit-tested placement: which room sits in which cell, as a function of seed and cell.
- `world/LibraryChunkGenerator` + `world/TemplateStamper` — stamp templates in `buildSurface`, clipped to the chunk.
  Placement uses known shape and ignores waterlogging so it never reads neighbouring chunks.
- `block/LibraryPortalBlock`, `world/PortalTeleporter`, `item/LibrarianKeyItem` — travel; a portal's target comes
  from the room definition covering its cell, the block holds no state.
- `datagen/*` — `RoomBootstrap` and `DimensionBootstrap` are the canonical room/library/dimension lists;
  JSON under `src/generated/resources` is generated, do not hand edit it.
- `tools/gen_room.py` — generates the shipped `.nbt` room templates (16-cube shells, doorways centred on every
  wall segment at local y 1..4, `lob:library_portal` blocks in halls). Regenerate with
  `python3 tools/gen_room.py src/main/resources/data/lob/structure`.
- `borgeslib/EncodingLib` — keyed bijection for future book addressing; no gameplay consumer yet.

## Rules

- Every room defines only its `template`, `size_in_cells`, `weight` and optional `portal_target`.
  There is no separate small/large room class; placement must stay a pure function of (seed, cell).
- Keep `RoomResolver`, `CellPos`, `CellHash` free of registry/holder types so JUnit can test them directly.
- Template size must equal `size_in_cells * 16`; the stamper logs once and places a fallback box on mismatch.
- Verification of worldgen is done against the dev server over RCON (`~/.cache/lob-tools/rcon.py lobdev "<cmd>"`),
  e.g. `execute in lob:library_alpha if block 8 16 8 minecraft:dark_oak_planks`. GameTestServer cannot load
  datapack dimensions, so GameTests inside the library are not an option. Set `pause-when-empty-seconds=0`
  in `run/server.properties` or entities will not tick without a player.
- Commit `src/generated/resources`; `run/` is ignored.
