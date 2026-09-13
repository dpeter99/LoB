package com.dpeter99.lob.item;

import com.dpeter99.lob.LobConfig;
import com.dpeter99.lob.book.BookAddress;
import com.dpeter99.lob.book.BookAlphabet;
import com.dpeter99.lob.book.BookText;
import com.dpeter99.lob.registry.ModRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

/**
 * A book taken from a library. The only state it carries is its {@link BookAddress}; the pages
 * are computed from that address when the book is opened and written into the vanilla
 * written-book component, so the stock book screen displays them without any client code.
 * Regenerating on every open keeps the text in step with the configured book size.
 */
public class LibraryBookItem extends Item {
    public LibraryBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        BookAddress address = stack.get(ModRegistries.BOOK_ADDRESS.get());
        if (address == null) {
            return InteractionResult.FAIL;
        }
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, content(address));
        // The client renders its own copy of the held stack, so it must see the pages before the open packet.
        serverPlayer.containerMenu.broadcastChanges();
        serverPlayer.openItemGui(stack, hand);
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Pages for an address under the current config. */
    public static WrittenBookContent content(BookAddress address) {
        BookText.BookLayout layout = new BookText.BookLayout(
                LobConfig.BOOK_PAGES.get(), LobConfig.BOOK_LINES_PER_PAGE.get(), LobConfig.BOOK_CHARS_PER_LINE.get());
        BookAlphabet alphabet = BookAlphabet.BORGES;
        List<Filterable<Component>> pages = BookText.pages(address.bookNumber(LobConfig.BOOKS_PER_CELL.get()), layout, alphabet).stream()
                .map(text -> Filterable.passThrough((Component) Component.literal(text)))
                .toList();
        String title = "Book " + address.index();
        return new WrittenBookContent(Filterable.passThrough(title), address.dimension().identifier().getPath(), 0, pages, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        BookAddress address = stack.get(ModRegistries.BOOK_ADDRESS.get());
        if (address != null) {
            builder.accept(Component.translatable("item.lob.library_book.address",
                    address.dimension().identifier().toString(),
                    address.cell().x(), address.cell().y(), address.cell().z(), address.index()));
        }
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
