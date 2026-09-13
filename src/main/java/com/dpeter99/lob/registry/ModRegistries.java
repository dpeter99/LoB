package com.dpeter99.lob.registry;

import com.dpeter99.lob.LobMod;
import com.dpeter99.lob.block.LibraryPortalBlock;
import com.dpeter99.lob.book.BookAddress;
import com.dpeter99.lob.item.LibrarianKeyItem;
import com.dpeter99.lob.item.LibraryBookItem;
import com.dpeter99.lob.world.LibraryChunkGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Owns every DeferredRegister of the mod. Registers them all on the mod event bus.
 */
@EventBusSubscriber(modid = LobMod.MOD_ID)
public final class ModRegistries {
    private ModRegistries() {
    }

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, LobMod.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LobMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LobMod.MOD_ID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, LobMod.MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, LobMod.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<LibraryChunkGenerator>> LIBRARY_GENERATOR =
            CHUNK_GENERATORS.register("library", () -> LibraryChunkGenerator.CODEC);

    public static final DeferredBlock<LibraryPortalBlock> LIBRARY_PORTAL = BLOCKS.registerBlock("library_portal",
            LibraryPortalBlock::new, p -> p
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollision()
                    .noOcclusion()
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(state -> 11)
                    .sound(SoundType.GLASS)
                    .pushReaction(PushReaction.BLOCK));

    public static final DeferredItem<BlockItem> LIBRARY_PORTAL_ITEM = ITEMS.registerSimpleBlockItem(LIBRARY_PORTAL);

    public static final DeferredItem<LibrarianKeyItem> LIBRARIAN_KEY = ITEMS.registerItem("librarian_key",
            LibrarianKeyItem::new, p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));

    /** Where in a library a book was taken from; its pages derive from this. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BookAddress>> BOOK_ADDRESS =
            DATA_COMPONENTS.registerComponentType("book_address", b -> b
                    .persistent(BookAddress.CODEC)
                    .networkSynchronized(BookAddress.STREAM_CODEC));

    public static final DeferredItem<LibraryBookItem> LIBRARY_BOOK = ITEMS.registerItem("library_book",
            LibraryBookItem::new, p -> p.stacksTo(1));

    /** Where a player was standing before the key took them into a library. */
    public static final Supplier<AttachmentType<Optional<GlobalPos>>> RETURN_POINT = ATTACHMENTS.register("return_point",
            () -> AttachmentType.<Optional<GlobalPos>>builder(Optional::empty)
                    .serialize(GlobalPos.CODEC.optionalFieldOf("pos"))
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ATTACHMENTS.register(modBus);
        DATA_COMPONENTS.register(modBus);
    }

    @SubscribeEvent
    static void addCreativeTabEntries(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(LIBRARIAN_KEY);
            event.accept(LIBRARY_BOOK);
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(LIBRARY_PORTAL_ITEM);
        }
    }
}
