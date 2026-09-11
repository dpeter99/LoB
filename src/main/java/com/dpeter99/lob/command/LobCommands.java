package com.dpeter99.lob.command;

import com.dpeter99.lob.LobMod;
import com.dpeter99.lob.room.RoomResolver;
import com.dpeter99.lob.world.CellPos;
import com.dpeter99.lob.world.LibraryChunkGenerator;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** {@code /lob cell}: prints the cell and resolved room at the command source's position. */
@EventBusSubscriber(modid = LobMod.MOD_ID)
public final class LobCommands {
    private LobCommands() {
    }

    @SubscribeEvent
    static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("lob")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("cell").executes(ctx -> describeCell(ctx.getSource()))));
    }

    private static int describeCell(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        CellPos cell = CellPos.fromBlock(pos);
        if (!(level.getChunkSource().getGenerator() instanceof LibraryChunkGenerator generator)) {
            source.sendSuccess(() -> Component.literal("Cell " + cell + " (not a library dimension)"), false);
            return 0;
        }
        RoomResolver.Placement placement = generator.resolver().resolve(level.getSeed(), cell);
        source.sendSuccess(() -> Component.literal("Cell " + cell + ": " + placement.room().template()
                + " size " + placement.room().sizeInCells() + " origin cell " + placement.origin()
                + placement.room().portalTarget().map(k -> " portal -> " + k.identifier()).orElse("")), false);
        return 1;
    }
}
