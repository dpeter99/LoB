package com.dpeter99.lob.command;

import com.dpeter99.lob.LobConfig;
import com.dpeter99.lob.LobMod;
import com.dpeter99.lob.book.BookAddress;
import com.dpeter99.lob.registry.ModRegistries;
import com.dpeter99.lob.room.RoomResolver;
import com.dpeter99.lob.world.CellPos;
import com.dpeter99.lob.world.LibraryChunkGenerator;
import com.dpeter99.lob.world.PortalTeleporter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /lob cell}: prints the cell and resolved room at the command source's position.
 * {@code /lob book [index]} and {@code /lob book <cellX> <cellY> <cellZ> [index]}: gives the
 * source player the book with that address in the source's dimension.
 */
@EventBusSubscriber(modid = LobMod.MOD_ID)
public final class LobCommands {
    private LobCommands() {
    }

    @SubscribeEvent
    static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("lob")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("cell").executes(ctx -> describeCell(ctx.getSource())))
                .then(Commands.literal("book")
                        .executes(ctx -> giveBookHere(ctx.getSource(), 0))
                        .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                .executes(ctx -> giveBookHere(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "index"))))
                        .then(Commands.argument("cellX", IntegerArgumentType.integer())
                                .then(Commands.argument("cellY", IntegerArgumentType.integer())
                                        .then(Commands.argument("cellZ", IntegerArgumentType.integer())
                                                .executes(ctx -> giveBook(ctx.getSource(), cellArg(ctx), 0))
                                                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> giveBook(ctx.getSource(), cellArg(ctx),
                                                                IntegerArgumentType.getInteger(ctx, "index")))))))));
    }

    private static CellPos cellArg(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        return new CellPos(IntegerArgumentType.getInteger(ctx, "cellX"), IntegerArgumentType.getInteger(ctx, "cellY"),
                IntegerArgumentType.getInteger(ctx, "cellZ"));
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

    private static int giveBookHere(CommandSourceStack source, int index) throws CommandSyntaxException {
        if (!PortalTeleporter.isLibrary(source.getLevel())) {
            source.sendFailure(Component.translatable("command.lob.book.not_in_library"));
            return 0;
        }
        return giveBook(source, CellPos.fromBlock(BlockPos.containing(source.getPosition())), index);
    }

    private static int giveBook(CommandSourceStack source, CellPos cell, int index) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int booksPerCell = LobConfig.BOOKS_PER_CELL.get();
        if (index >= booksPerCell) {
            source.sendFailure(Component.translatable("command.lob.book.index_out_of_range", index, booksPerCell));
            return 0;
        }
        BookAddress address = new BookAddress(source.getLevel().dimension(), cell, index);
        ItemStack stack = new ItemStack(ModRegistries.LIBRARY_BOOK.get());
        stack.set(ModRegistries.BOOK_ADDRESS.get(), address);
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
        source.sendSuccess(() -> Component.translatable("command.lob.book.given",
                address.dimension().identifier().toString(), cell.x(), cell.y(), cell.z(), index), false);
        return 1;
    }
}
