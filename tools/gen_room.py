#!/usr/bin/env python3
"""Generates procedural 16x16x16 library room templates as Minecraft structure NBT files.

Templates authored here are starting points; they can be refined in-game with structure
blocks and re-exported. Usage: python3 tools/gen_room.py <output_dir>
"""
import gzip
import struct
import sys
from pathlib import Path

DATA_VERSION = 4903  # Minecraft 26.2
SIZE = 16


# --- minimal NBT writer -------------------------------------------------------------
def _name(s):
    b = s.encode("utf-8")
    return struct.pack(">H", len(b)) + b


def _tag_payload(v):
    """Returns (type id, payload bytes) for a python value."""
    if isinstance(v, bool):
        return 1, struct.pack(">b", 1 if v else 0)
    if isinstance(v, int):
        return 3, struct.pack(">i", v)
    if isinstance(v, str):
        return 8, _name(v)
    if isinstance(v, list):
        if not v:
            return 9, struct.pack(">bi", 0, 0)
        types = {_tag_payload(e)[0] for e in v}
        assert len(types) == 1, "heterogeneous list"
        t = types.pop()
        return 9, struct.pack(">bi", t, len(v)) + b"".join(_tag_payload(e)[1] for e in v)
    if isinstance(v, dict):
        out = b""
        for k, e in v.items():
            t, p = _tag_payload(e)
            out += struct.pack(">b", t) + _name(k) + p
        return 10, out + b"\x00"
    raise TypeError(type(v))


def write_nbt(path, root):
    t, p = _tag_payload(root)
    assert t == 10
    data = struct.pack(">b", t) + _name("") + p
    with gzip.open(path, "wb") as f:
        f.write(data)


# --- room building ------------------------------------------------------------------
class Room:
    def __init__(self, cells=(1, 1, 1)):
        self.cells = cells
        self.sx, self.sy, self.sz = (c * SIZE for c in cells)
        self.palette = []
        self.index = {}
        self.blocks = {}

    def state(self, name, props=None):
        key = (name, tuple(sorted((props or {}).items())))
        if key not in self.index:
            entry = {"Name": name}
            if props:
                entry["Properties"] = {k: str(v) for k, v in props.items()}
            self.index[key] = len(self.palette)
            self.palette.append(entry)
        return self.index[key]

    def set(self, x, y, z, name, props=None):
        self.blocks[(x, y, z)] = self.state(name, props)

    def fill(self, x0, y0, z0, x1, y1, z1, name, props=None):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    self.set(x, y, z, name, props)

    def to_nbt(self):
        return {
            "DataVersion": DATA_VERSION,
            "size": [self.sx, self.sy, self.sz],
            "palette": self.palette,
            "blocks": [{"pos": [x, y, z], "state": s} for (x, y, z), s in sorted(self.blocks.items())],
            "entities": [],
        }


AIR = "minecraft:air"
FLOOR = "minecraft:dark_oak_planks"
CEILING = "minecraft:spruce_planks"
SHELF = "minecraft:bookshelf"
PILLAR = "minecraft:dark_oak_log"
TRIM = "minecraft:spruce_slab"
LANTERN = "minecraft:lantern"

# Wall openings: 4 wide, 4 tall, centred on each wall at floor+1..floor+4.
DOOR_LO, DOOR_HI = 6, 9
DOOR_Y0, DOOR_Y1 = 1, 4


def shell(r):
    """Floor, ceiling, bookshelf walls with a doorway per cell face on every floor, and pillars.

    Doorways sit at the centre of each 16-block wall segment, at floor+1..+4 of every 16-block
    storey, so any two rooms placed next to each other line up, whatever their size.
    """
    mx, my, mz = r.sx - 1, r.sy - 1, r.sz - 1
    r.fill(0, 0, 0, mx, my, mz, AIR)
    r.fill(0, 0, 0, mx, 0, mz, FLOOR)
    r.fill(0, my, 0, mx, my, mz, CEILING)
    r.fill(0, 1, 0, 0, my - 1, mz, SHELF)
    r.fill(mx, 1, 0, mx, my - 1, mz, SHELF)
    r.fill(0, 1, 0, mx, my - 1, 0, SHELF)
    r.fill(0, 1, mz, mx, my - 1, mz, SHELF)
    for storey in range(r.cells[1]):
        y0, y1 = storey * SIZE + DOOR_Y0, storey * SIZE + DOOR_Y1
        for seg in range(r.cells[2]):
            lo, hi = seg * SIZE + DOOR_LO, seg * SIZE + DOOR_HI
            r.fill(0, y0, lo, 0, y1, hi, AIR)
            r.fill(mx, y0, lo, mx, y1, hi, AIR)
            r.fill(0, y1 + 1, lo, 0, y1 + 1, hi, TRIM, {"type": "top"})
            r.fill(mx, y1 + 1, lo, mx, y1 + 1, hi, TRIM, {"type": "top"})
        for seg in range(r.cells[0]):
            lo, hi = seg * SIZE + DOOR_LO, seg * SIZE + DOOR_HI
            r.fill(lo, y0, 0, hi, y1, 0, AIR)
            r.fill(lo, y0, mz, hi, y1, mz, AIR)
            r.fill(lo, y1 + 1, 0, hi, y1 + 1, 0, TRIM, {"type": "top"})
            r.fill(lo, y1 + 1, mz, hi, y1 + 1, mz, TRIM, {"type": "top"})
    for x in range(0, r.sx, SIZE - 1 if r.cells[0] == 1 else SIZE):
        for z in range(0, r.sz, SIZE - 1 if r.cells[2] == 1 else SIZE):
            r.fill(x, 1, z, x, my - 1, z, PILLAR, {"axis": "y"})
    r.fill(mx, 1, 0, mx, my - 1, 0, PILLAR, {"axis": "y"})
    r.fill(0, 1, mz, 0, my - 1, mz, PILLAR, {"axis": "y"})
    r.fill(mx, 1, mz, mx, my - 1, mz, PILLAR, {"axis": "y"})


def lanterns(r, positions, y=14):
    for x, z in positions:
        r.set(x, y, z, LANTERN, {"hanging": "true"})


def room_basic():
    r = Room()
    shell(r)
    # two free-standing shelf stacks leaving a cross-shaped walkway
    r.fill(3, 1, 3, 4, 4, 5, SHELF)
    r.fill(11, 1, 3, 12, 4, 5, SHELF)
    r.fill(3, 1, 10, 4, 4, 12, SHELF)
    r.fill(11, 1, 10, 12, 4, 12, SHELF)
    lanterns(r, ((4, 4), (11, 4), (4, 11), (11, 11)))
    return r


def room_crossing():
    r = Room()
    shell(r)
    # a central pillar ring with a lantern
    r.fill(7, 1, 7, 8, 14, 8, PILLAR, {"axis": "y"})
    lanterns(r, ((5, 5), (10, 5), (5, 10), (10, 10)))
    return r


def room_stairwell():
    r = Room()
    shell(r)
    # open the ceiling and a matching hole in the floor so stairwells stack into a shaft
    r.fill(3, 15, 3, 6, 15, 6, AIR)
    r.fill(3, 0, 3, 6, 0, 6, AIR)
    # ladder column against a shelf wall from floor to ceiling
    r.fill(2, 0, 3, 2, 15, 6, SHELF)
    for y in range(0, 16):
        for z in range(3, 7):
            r.set(3, y, z, "minecraft:ladder", {"facing": "east"})
    # the shaft is 4x4 at x 3..6, z 3..6; fence the rest of the hole edge for safety
    r.fill(7, 1, 3, 7, 1, 6, "minecraft:dark_oak_fence")
    r.fill(3, 1, 7, 7, 1, 7, "minecraft:dark_oak_fence")
    lanterns(r, ((11, 4), (11, 11), (4, 11)))
    return r


# Portal blocks are placed as this block until the mod's own portal block exists.
PORTAL = "lob:library_portal"
PORTAL_FRAME = "minecraft:polished_blackstone_bricks"
GLASS = "minecraft:tinted_glass"


def hall_grand():
    """3x2x3 cells: a two-storey reading hall with a balcony ring and a central portal dais."""
    r = Room(cells=(3, 2, 3))
    shell(r)
    cx, cz = r.sx // 2, r.sz // 2
    # balcony floor on the second storey around the outside, leaving a big central well
    r.fill(1, 16, 1, r.sx - 2, 16, r.sz - 2, FLOOR)
    r.fill(6, 16, 6, r.sx - 7, 16, r.sz - 7, AIR)
    r.fill(6, 17, 6, r.sx - 7, 17, r.sz - 7, "minecraft:dark_oak_fence")
    r.fill(7, 17, 7, r.sx - 8, 17, r.sz - 8, AIR)
    # stairs up to the balcony in one corner
    for i in range(16):
        r.fill(2, 1 + i, 2 + i, 3, 1 + i, 2 + i, "minecraft:dark_oak_stairs", {"facing": "south"})
        r.fill(2, 1, 2 + i, 3, i, 2 + i, FLOOR)
    # inner shelf rows on the ground floor
    for x in (6, 12, r.sx - 13, r.sx - 7):
        r.fill(x, 1, 6, x + 1, 5, cz - 6, SHELF)
        r.fill(x, 1, cz + 6, x + 1, 5, r.sz - 7, SHELF)
    # portal dais in the centre
    r.fill(cx - 3, 1, cz - 3, cx + 2, 1, cz + 2, PORTAL_FRAME)
    r.fill(cx - 2, 2, cz - 2, cx + 1, 2, cz + 1, PORTAL_FRAME)
    r.fill(cx - 1, 3, cz - 1, cx, 3, cz, PORTAL_FRAME)
    r.fill(cx - 1, 4, cz - 1, cx, 5, cz, PORTAL)
    # lanterns under the balcony and the ceiling
    lanterns(r, [(x, z) for x in (4, cx, r.sx - 5) for z in (4, cz, r.sz - 5)], y=15)
    lanterns(r, [(x, z) for x in (4, cx, r.sx - 5) for z in (4, cz, r.sz - 5)], y=r.sy - 2)
    return r


def hall_exit():
    """2x1x2 cells: a quiet reading room whose portal leads back to the overworld."""
    r = Room(cells=(2, 1, 2))
    shell(r)
    cx, cz = r.sx // 2, r.sz // 2
    r.fill(cx - 2, 1, cz - 2, cx + 1, 1, cz + 1, PORTAL_FRAME)
    r.fill(cx - 1, 2, cz - 1, cx, 3, cz, PORTAL)
    for x in (4, r.sx - 6):
        r.fill(x, 1, 4, x + 1, 3, r.sz - 5, SHELF)
    lanterns(r, [(x, z) for x in (5, r.sx - 6) for z in (5, r.sz - 6)])
    return r


ROOMS = {
    "room_basic": room_basic,
    "room_crossing": room_crossing,
    "room_stairwell": room_stairwell,
    "hall_grand": hall_grand,
    "hall_exit": hall_exit,
}


def main():
    out = Path(sys.argv[1] if len(sys.argv) > 1 else "src/main/resources/data/lob/structure")
    out.mkdir(parents=True, exist_ok=True)
    for name, build in ROOMS.items():
        write_nbt(out / f"{name}.nbt", build().to_nbt())
        print("wrote", out / f"{name}.nbt")


if __name__ == "__main__":
    main()
