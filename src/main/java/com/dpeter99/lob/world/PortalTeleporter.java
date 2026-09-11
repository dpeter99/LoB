package com.dpeter99.lob.world;

import com.dpeter99.lob.LobConfig;
import com.dpeter99.lob.registry.LobDimensions;
import com.dpeter99.lob.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/** Builds teleport transitions into and out of library dimensions. */
public final class PortalTeleporter {
    private PortalTeleporter() {
    }

    /** Cell Y a player lands in when entering a library from outside. */
    public static final int ENTRY_CELL_Y = Math.floorDiv(LobDimensions.MIN_Y, CellPos.SIZE) + 1;

    public static boolean isLibrary(Level level) {
        return level instanceof ServerLevel serverLevel && serverLevel.getChunkSource().getGenerator() instanceof LibraryChunkGenerator;
    }

    /** Transition into {@code target} at the same horizontal cell, on the first walkable floor of that cell. */
    public static TeleportTransition toLibraryCell(ServerLevel target, CellPos cell, Entity entity) {
        int minCell = Math.floorDiv(target.getMinY(), CellPos.SIZE);
        int maxCell = Math.floorDiv(target.getMaxY(), CellPos.SIZE);
        CellPos clamped = new CellPos(cell.x(), Math.clamp(cell.y(), minCell, maxCell), cell.z());
        target.getChunk(clamped.x(), clamped.z()); // force generation so the scan sees real blocks
        BlockPos arrival = findArrival(target, clamped);
        return transition(target, Vec3.atBottomCenterOf(arrival), entity);
    }

    /** Transition back to where the entity entered the library from, or the world spawn as a fallback. */
    public static TeleportTransition toReturnPoint(ServerLevel current, Entity entity) {
        Optional<GlobalPos> saved = entity.getData(ModRegistries.RETURN_POINT);
        if (saved.isPresent()) {
            ServerLevel level = current.getServer().getLevel(saved.get().dimension());
            if (level != null) {
                return transition(level, Vec3.atBottomCenterOf(saved.get().pos()), entity);
            }
        }
        ServerLevel overworld = current.getServer().overworld();
        BlockPos spawn = overworld.getRespawnData().pos();
        return transition(overworld, Vec3.atBottomCenterOf(spawn), entity);
    }

    /** Transition to an arbitrary dimension: libraries land on a cell floor, anything else uses the return point. */
    public static @Nullable TeleportTransition to(ServerLevel current, ServerLevel target, CellPos cell, Entity entity) {
        if (target.getChunkSource().getGenerator() instanceof LibraryChunkGenerator) {
            return toLibraryCell(target, cell, entity);
        }
        return toReturnPoint(current, entity);
    }

    private static TeleportTransition transition(ServerLevel level, Vec3 pos, Entity entity) {
        int cooldown = LobConfig.PORTAL_COOLDOWN_TICKS.getAsInt();
        return new TeleportTransition(level, pos, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(e -> e.setPortalCooldown(cooldown)));
    }

    /**
     * Searches the cell, starting near its centre just above the floor, for a spot with solid ground
     * and two blocks of air. Falls back to one block above the cell floor.
     */
    static BlockPos findArrival(ServerLevel level, CellPos cell) {
        BlockPos origin = cell.origin();
        int cx = origin.getX() + CellPos.SIZE / 2;
        int cz = origin.getZ() + CellPos.SIZE / 2;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // spiral outwards from the centre column
        for (int radius = 0; radius < CellPos.SIZE / 2; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int x = cx + dx;
                    int z = cz + dz;
                    if (x < origin.getX() || x > origin.getX() + CellPos.SIZE - 1 || z < origin.getZ() || z > origin.getZ() + CellPos.SIZE - 1) continue;
                    for (int y = origin.getY() + 1; y < origin.getY() + CellPos.SIZE - 1; y++) {
                        if (level.getBlockState(pos.set(x, y - 1, z)).isSolid()
                                && level.getBlockState(pos.set(x, y, z)).isAir()
                                && level.getBlockState(pos.set(x, y + 1, z)).isAir()) {
                            return new BlockPos(x, y, z);
                        }
                    }
                }
            }
        }
        return new BlockPos(cx, origin.getY() + 1, cz);
    }
}
