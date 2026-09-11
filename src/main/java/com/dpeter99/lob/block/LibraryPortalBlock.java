package com.dpeter99.lob.block;

import com.dpeter99.lob.LobMod;
import com.dpeter99.lob.room.RoomResolver;
import com.dpeter99.lob.world.CellPos;
import com.dpeter99.lob.world.LibraryChunkGenerator;
import com.dpeter99.lob.world.PortalTeleporter;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A portal that leads wherever the room it stands in says. The target comes from the room
 * definition resolved for the portal's cell, so the block itself carries no state.
 */
public class LibraryPortalBlock extends Block implements Portal {
    public static final MapCodec<LibraryPortalBlock> CODEC = simpleCodec(LibraryPortalBlock::new);
    private static final int TRANSITION_TICKS = 10;

    public LibraryPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return TRANSITION_TICKS;
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos) {
        Optional<ResourceKey<Level>> target = targetOf(currentLevel, portalEntryPos);
        if (target.isEmpty()) {
            return null;
        }
        ServerLevel targetLevel = currentLevel.getServer().getLevel(target.get());
        if (targetLevel == null) {
            LobMod.LOGGER.warn("Library portal at {} targets missing dimension {}", portalEntryPos, target.get().identifier());
            return null;
        }
        return PortalTeleporter.to(currentLevel, targetLevel, CellPos.fromBlock(portalEntryPos), entity);
    }

    /** The dimension this portal leads to, taken from the room covering its cell. Empty outside libraries. */
    public static Optional<ResourceKey<Level>> targetOf(ServerLevel level, BlockPos pos) {
        if (!(level.getChunkSource().getGenerator() instanceof LibraryChunkGenerator generator)) {
            return Optional.empty();
        }
        RoomResolver.Placement placement = generator.resolver().resolve(level.getSeed(), CellPos.fromBlock(pos));
        return placement.room().portalTarget();
    }
}
