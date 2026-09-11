# Library of Babel

A NeoForge mod that turns Borges' Library of Babel into a set of endless library dimensions.

## Layout

Every library dimension is a grid of **16x16x16 cells**. Each cell holds one room template. Which
room sits in which cell is a pure function of the world seed and the cell coordinates, so every
chunk generates independently and neighbouring chunks always agree.

- Rooms: `data/<namespace>/lob/library_room/*.json` (datapack registry `lob:library_room`)
  - `template`: structure template id, must be exactly `size_in_cells * 16` blocks on every axis
  - `size_in_cells`: `[x, y, z]`, default `[1, 1, 1]`, at most 8 per axis
  - `weight`: relative chance that a cell anchors this room, default 1
  - `portal_target`: optional dimension id that portal blocks inside this room lead to
- Libraries: `data/<namespace>/lob/library/*.json` (datapack registry `lob:library`)
  - `rooms`: list of room ids (or a `#tag`); at least one must be 1x1x1 so gaps can be filled

Placement: every cell hashes to one candidate room (by weight) anchored at that cell plus a
priority; a candidate is placed when it outranks every candidate whose footprint would overlap
it, so rooms never overlap and each chunk decides on its own. Cells nothing claims get a 1x1x1
room by weight.
- Dimensions: `data/lob/dimension/*.json` use generator `lob:library` with a `biome` and a `library` field.

The shipped rooms and libraries are defined in Java under `com.dpeter99.lob.datagen` and written to
`src/generated/resources` by `./gradlew runData`.

## Room templates

Templates live in `src/main/resources/data/lob/structure/*.nbt`. The shipped ones are produced by
`python3 tools/gen_room.py` so they can be regenerated; you can also refine them in-game with
structure blocks and re-export, as long as the size stays a multiple of 16.

Authoring convention so any two rooms connect:

- floor at local y = 0, ceiling at local y = 15 of every storey
- a 4 wide, 4 tall doorway centred on every 16-block wall segment at local y = 1..4
- the `hall_*` templates contain `lob:library_portal` blocks; the room's `portal_target` decides where they lead

## Building

Requires Java 25 for compilation (Gradle's toolchain resolver downloads it) and any Java 17+ to launch Gradle.

```
./gradlew build          # jar in build/libs
./gradlew test           # JUnit tests
./gradlew runData        # regenerate src/generated/resources
./gradlew runClient      # start the dev client
```

Enter a library with `/execute in lob:library_alpha run tp @s 8 17 8`.
