package com.dpeter99.lob.world;

import com.dpeter99.lob.LobMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Places structure templates into a chunk during world generation, clipped to the chunk
 * so that templates larger than one chunk are stamped piecewise by every chunk they cover.
 */
public final class TemplateStamper {
    private final Map<Identifier, Optional<StructureTemplate>> cache = new ConcurrentHashMap<>();
    private final Set<Identifier> reportedMismatch = ConcurrentHashMap.newKeySet();
    private volatile StructureTemplateManager cachedFor;

    /**
     * Stamps {@code templateId} with its lowest corner at {@code origin}, writing only blocks inside {@code clip}.
     *
     * @param expectedSize the size the template must have; a mismatch is logged once and a fallback box is placed
     * @return true if the real template was placed
     */
    public boolean stamp(WorldGenRegion region, Identifier templateId, BlockPos origin, Vec3i expectedSize, BoundingBox clip, RandomSource random) {
        StructureTemplateManager manager = region.getLevel().getStructureManager();
        if (cachedFor != manager) {
            cache.clear();
            reportedMismatch.clear();
            cachedFor = manager;
        }
        Optional<StructureTemplate> template = cache.computeIfAbsent(templateId, manager::get);
        if (template.isEmpty()) {
            if (reportedMismatch.add(templateId)) {
                LobMod.LOGGER.error("Library room template {} does not exist; placing fallback box", templateId);
            }
            placeFallback(region, origin, expectedSize, clip);
            return false;
        }
        StructureTemplate t = template.get();
        if (!t.getSize().equals(expectedSize)) {
            if (reportedMismatch.add(templateId)) {
                LobMod.LOGGER.error("Library room template {} is {} but the room definition needs {}; placing fallback box",
                        templateId, t.getSize(), expectedSize);
            }
            placeFallback(region, origin, expectedSize, clip);
            return false;
        }
        // Known shape and no waterlogging keep placement from reading neighbouring chunks, which
        // is unsafe during the surface step.
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setBoundingBox(clip)
                .setIgnoreEntities(false)
                .setKnownShape(true)
                .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING);
        t.placeInWorld(region, origin, origin, settings, random, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        return true;
    }

    /** A hollow box so the dimension stays walkable when a template is missing. */
    private static void placeFallback(WorldGenRegion region, BlockPos origin, Vec3i size, BoundingBox clip) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int maxX = origin.getX() + size.getX() - 1;
        int maxY = origin.getY() + size.getY() - 1;
        int maxZ = origin.getZ() + size.getZ() - 1;
        for (int x = Math.max(origin.getX(), clip.minX()); x <= Math.min(maxX, clip.maxX()); x++) {
            for (int z = Math.max(origin.getZ(), clip.minZ()); z <= Math.min(maxZ, clip.maxZ()); z++) {
                for (int y = Math.max(origin.getY(), clip.minY()); y <= Math.min(maxY, clip.maxY()); y++) {
                    boolean shell = x == origin.getX() || x == maxX || y == origin.getY() || y == maxY || z == origin.getZ() || z == maxZ;
                    region.setBlock(pos.set(x, y, z), shell ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(),
                            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
    }
}
