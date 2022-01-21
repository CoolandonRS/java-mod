package net.numra.tech.blocks;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.numra.tech.blocks.blockentities.ConveyorBasicBlockEntity;
import net.numra.tech.blocks.blockentities.ConveyorBasicBlockEntityRenderer;
import net.numra.tech.items.ItemGroups;

import static net.numra.tech.NumraTech.logger_block;
import static net.numra.tech.NumraTechClient.logger_client;

public class ConveyorBasic {
    // Defines and registers conveyor_basic blocks and their corresponding items

    public static final ConveyorBasicBlock CONVEYOR_WOOD = new ConveyorBasicBlock(FabricBlockSettings.of(Material.WOOD).hardness(2.5f).resistance(3.0f), 0.0165F, 0.0135F, 1, 16, 5);

    public static BlockEntityType<ConveyorBasicBlockEntity> CONVEYOR_BASIC_BLOCK_ENTITY;

    public static void initBlockEntities() {
        logger_block.debug("initializing numra:conveyor_basic_block_entity");
        CONVEYOR_BASIC_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "numra:conveyor_basic_block_entity", FabricBlockEntityTypeBuilder.create(ConveyorBasicBlockEntity::new, CONVEYOR_WOOD).build(null));
    }
    
    public static void initEntityRenderers() {
        logger_client.debug("initializing ConveyorBasicEntityRenderer");
        BlockEntityRendererRegistry.register(CONVEYOR_BASIC_BLOCK_ENTITY, ConveyorBasicBlockEntityRenderer::new);
    }

    public static void initBlocks() {
        // Wood
        logger_block.debug("initializing numra:conveyor_wood");
        Registry.register(Registry.BLOCK, new Identifier("numra:conveyor_wood"), CONVEYOR_WOOD);
        Registry.register(Registry.ITEM, new Identifier("numra:conveyor_wood"), new BlockItem(CONVEYOR_WOOD, new FabricItemSettings().group(ItemGroups.NUMRA_CONVEYORS)));
    }
}