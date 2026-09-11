package com.dpeter99.lob.item;

import com.dpeter99.lob.LobConfig;
import com.dpeter99.lob.registry.ModRegistries;
import com.dpeter99.lob.world.CellPos;
import com.dpeter99.lob.world.PortalTeleporter;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;

import java.util.Optional;

/**
 * The overworld entry point. Used outside a library it remembers where the player stood and
 * sends them to the configured entry library; used inside a library it brings them back.
 */
public class LibrarianKeyItem extends Item {
    private static final int USE_COOLDOWN_TICKS = 40;

    public LibrarianKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        TeleportTransition transition;
        if (PortalTeleporter.isLibrary(serverLevel)) {
            transition = PortalTeleporter.toReturnPoint(serverLevel, serverPlayer);
        } else {
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, Identifier.parse(LobConfig.DEFAULT_ENTRY_DIMENSION.get()));
            ServerLevel target = serverLevel.getServer().getLevel(key);
            if (target == null) {
                player.sendOverlayMessage(Component.translatable("item.lob.librarian_key.missing_dimension", key.identifier().toString()));
                return InteractionResult.FAIL;
            }
            serverPlayer.setData(ModRegistries.RETURN_POINT, Optional.of(GlobalPos.of(serverLevel.dimension(), serverPlayer.blockPosition())));
            CellPos entry = new CellPos(serverPlayer.blockPosition().getX() >> CellPos.SHIFT, PortalTeleporter.ENTRY_CELL_Y,
                    serverPlayer.blockPosition().getZ() >> CellPos.SHIFT);
            transition = PortalTeleporter.toLibraryCell(target, entry, serverPlayer);
        }
        serverPlayer.teleport(transition);
        player.getCooldowns().addCooldown(stack, USE_COOLDOWN_TICKS);
        return InteractionResult.SUCCESS_SERVER;
    }
}
