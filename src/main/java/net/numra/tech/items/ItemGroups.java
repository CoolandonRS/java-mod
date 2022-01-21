package net.numra.tech.items;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import static net.numra.tech.blocks.ConveyorBasic.CONVEYOR_WOOD;

public class ItemGroups {
    public static final ItemGroup NUMRA_CONVEYORS = FabricItemGroupBuilder.build(new Identifier("numra:conveyors"), () -> new ItemStack(CONVEYOR_WOOD));
}
