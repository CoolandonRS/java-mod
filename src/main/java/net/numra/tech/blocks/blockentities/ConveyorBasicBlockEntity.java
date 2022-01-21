package net.numra.tech.blocks.blockentities;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.PositionImpl;
import net.minecraft.world.World;
import net.numra.tech.blocks.ConveyorBasic;
import net.numra.tech.blocks.ConveyorBasicBlock;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static net.numra.tech.NumraTech.logger_block;
import static net.numra.tech.blocks.ConveyorBasicBlock.ACTIVE;
import static net.numra.tech.blocks.ConveyorBasicBlock.DIRECTION;

public class ConveyorBasicBlockEntity extends BlockEntity implements SidedInventory {
    private final int inventorySize;
    private final int slotSize;
    private final int transferSpeed;
    private boolean blocked;
    private int[] progress;
    
    private final DefaultedList<ItemStack> stacks;

    public static void tick(World world, BlockPos pos, BlockState state, ConveyorBasicBlockEntity blockEntity) {
        if (world.isClient) return;
        if (blockEntity.getCachedState().get(ACTIVE)) {
            if (!blockEntity.blocked) {
                for (int i = 0; i < blockEntity.progress.length; ++i) {
                    if (!blockEntity.stacks.get(i).isEmpty()) {
                        blockEntity.progress[i] += blockEntity.transferSpeed;
                        if (blockEntity.progress[i] >= 800) {
                            if (blockEntity.progress[i] >= 1000) {
                                String progressResult = blockEntity.progressItem(i, world, pos);
                                if (!progressResult.equals("Blocked")) {
                                    blockEntity.progress[i] = -1;
                                }
                            }
                            if (blockEntity.checkIfBlocked(world, pos)) {
                                blockEntity.blocked = true;
                                blockEntity.markDirty();
                            }
                        }
                        blockEntity.markDirty();
                    } else if (blockEntity.progress[i] < 1) blockEntity.progress[i] = 0;
                    blockEntity.markDirty();
                }
            } else {
                if (!blockEntity.checkIfBlocked(world, pos)) {
                    blockEntity.blocked = false;
                    blockEntity.markDirty();
                }
            }
        }
    }
    
    private String progressItem(int itemIndex, World world, BlockPos pos) { // as of now the only return that does anything is "Blocked"
        BlockPos outPos = pos.offset(getCachedState().get(DIRECTION).getSecondDirection());
        PositionImpl dropPos = getDropPos(pos);
        Block outBlock = world.getBlockState(outPos).getBlock();
        @Nullable BlockEntity tempOutEntity = world.getBlockEntity(outPos);
        if (outBlock instanceof ConveyorBasicBlock) {
            if (((ConveyorBasicBlock)getCachedState().getBlock()).testConveyorConnect(getCachedState(), getCachedState().get(DIRECTION).getSecondDirection(), world.getBlockState(outPos))) {
                if (tempOutEntity != null) {
                    ConveyorBasicBlockEntity outEntity = (ConveyorBasicBlockEntity) tempOutEntity;
                    ItemStack outStack = stacks.get(itemIndex);
                    Direction outDir = getCachedState().get(DIRECTION).getSecondDirection().getOpposite(); //Only half understand why this needs to be opposite
                    if (!outEntity.blocked) {
                        if (insertItem(outEntity, outDir, outStack)) {
                            removeStack(itemIndex);
                            return "Transferred";
                        } else {
                            blocked = true;
                            markDirty();
                            return "Blocked";
                        }
                    } else {
                        blocked = true;
                        markDirty();
                        return "Blocked";
                    }
                } else {
                    logger_block.error("Null BlockEntity found during ConveyorBasicBlockEntity.progressItem");
                    return "ERR";
                }
            }
            blocked = true;
            markDirty();
            return "Blocked";
        } else if (tempOutEntity instanceof Inventory) {
            boolean inserted;
            if (tempOutEntity instanceof SidedInventory) {
                inserted = insertItem((SidedInventory) tempOutEntity, getCachedState().get(DIRECTION).getSecondDirection(), stacks.get(itemIndex));
            } else {
                inserted = insertItem((Inventory) tempOutEntity, stacks.get(itemIndex));
            }
            if (inserted) {
                removeStack(itemIndex);
                return "Transferred";
            } else {
                blocked = true;
                markDirty();
                return "Blocked";
            }
        } else if (outBlock instanceof AirBlock || outBlock instanceof FluidBlock || outBlock instanceof BubbleColumnBlock) {
            ItemEntity itemEntity = new ItemEntity(world, dropPos.getX(), dropPos.getY(), dropPos.getZ(), stacks.get(itemIndex), 0, 0, 0);
            itemEntity.setPickupDelay(15);
            world.spawnEntity(itemEntity);
            removeStack(itemIndex);
            return "Dropped";
        } else {
            blocked = true;
            markDirty();
            return "Blocked";
        }
    }
    
    private boolean checkIfBlocked(World world, BlockPos pos) {
        BlockPos outPos = pos.offset(getCachedState().get(DIRECTION).getSecondDirection());
        BlockEntity outBlockEntity = world.getBlockEntity(outPos);
        Block outBlock = world.getBlockState(outPos).getBlock();
        return !(outBlock instanceof AirBlock || outBlock instanceof FluidBlock || outBlock instanceof BubbleColumnBlock || (outBlockEntity instanceof Inventory && !(outBlockEntity instanceof ConveyorBasicBlockEntity)) || (outBlockEntity instanceof ConveyorBasicBlockEntity && ((ConveyorBasicBlock) outBlock).testConveyorConnect(getCachedState(), getCachedState().get(DIRECTION).getSecondDirection(), outBlockEntity.getCachedState())));
    }

    private PositionImpl getDropPos(BlockPos pos) {
        BlockPos tempPos;
        switch (getCachedState().get(DIRECTION).getSecondDirection()) {
            case NORTH -> { tempPos = pos.north(); return new PositionImpl((tempPos.getX() + 0.5), tempPos.getY(), (tempPos.getZ() + 0.8)); }
            case EAST -> { tempPos = pos.east(); return new PositionImpl((tempPos.getX() + 0.2), tempPos.getY(), (tempPos.getZ() + 0.5)); }
            case SOUTH -> { tempPos = pos.south(); return new PositionImpl((tempPos.getX() + 0.5), tempPos.getY(), (tempPos.getZ() + 0.2)); }
            default -> { tempPos = pos.west(); return new PositionImpl((tempPos.getX() + 0.8), tempPos.getY(), (tempPos.getZ() + 0.5)); } // see above comment
        }
    }

    public int getProgress(int index) {
        return this.progress[index];
    }
    @Override
    public int size() {
        return inventorySize;
    }

    @Override
    public boolean isEmpty() { //CREDIT: Stolen from https://fabricmc.net/wiki/tutorial:inventory
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (!(slot < 0 || slot >= size())) {
            return this.stacks.get(slot);
        } else return ItemStack.EMPTY;
    }

    public DefaultedList<ItemStack> getStacks() {
        return this.stacks;
    }

    @Override
    public ItemStack removeStack(int slot, int count) { //CREDIT: Adapted from https://fabricmc.net/wiki/tutorial:inventory
        ItemStack result = Inventories.splitStack(stacks, slot, count);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) { //CREDIT: Adapted from https://fabricmc.net/wiki/tutorial:inventory
        return Inventories.removeStack(stacks, slot);
    }

    @Override
    public int getMaxCountPerStack() { // Doesn't seem to do anything for some reason
        return slotSize;
    }

    @Override
    public void setStack(int slot, ItemStack stack) { //CREDIT: Adapted from https://fabricmc.net/wiki/tutorial:inventory
        this.refreshProgress();
        if (this.progress[slot] == -1) this.progress[slot] = 0;
        stacks.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
    }
    
    private void refreshProgress() { //Fixes a bug but there is a probably a better way to do so.
        BlockState blockState = this.world.getBlockState(this.pos);
        this.world.setBlockState(this.pos, blockState.with(ACTIVE, !blockState.get(ACTIVE)));
        this.world.setBlockState(this.pos, blockState.with(ACTIVE, blockState.get(ACTIVE)));
    }

    @Override
    public void clear() { //CREDIT: Adapted from https://fabricmc.net/wiki/tutorial:inventory
        stacks.clear();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    public boolean insertItem(SidedInventory destination, Direction direction, ItemStack inStack) {
        for (int slot : destination.getAvailableSlots(direction)) {
            if (destination.canInsert(slot, inStack, direction)) {
                if (!destination.getStack(slot).isEmpty()) {
                    if (destination.getStack(slot).isOf(inStack.getItem())) {
                        destination.setStack(slot, new ItemStack(inStack.getItem(), this.getStack(slot).getCount() + inStack.getCount()));
                        return true;
                    }
                } else {
                    destination.setStack(slot, inStack);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean insertItem(Inventory destination, ItemStack inStack) {
        for (int slot = 0; slot < destination.size(); slot++) {
            if (!destination.getStack(slot).isEmpty()) {
                if (destination.getStack(slot).isOf(inStack.getItem())) {
                    destination.setStack(slot, new ItemStack(inStack.getItem(), this.getStack(slot).getCount() + inStack.getCount()));
                    return true;
                }
            } else {
                destination.setStack(slot, inStack);
                return true;
            }
        }
        return false;
    }

    public void insertDroppedItem(ItemEntity droppedItem) {
        ItemStack newStack = droppedItem.getStack();
        if (insertItem(this, Direction.UP, newStack)) droppedItem.discard();
    }
    @Override
    public void writeNbt(NbtCompound tag) {
        Inventories.writeNbt(tag, stacks);
        tag.putIntArray("progress", progress);
        tag.putBoolean("blocked", blocked);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        stacks.clear();
        Inventories.readNbt(tag, stacks);
        progress = tag.getIntArray("progress");
        blocked = tag.getBoolean("blocked");
    }

    @Override
    public int[] getAvailableSlots(Direction side) { //CREDIT: Adapted from https://fabricmc.net/wiki/tutorial:inventory
        int[] result = new int[stacks.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (dir != null) {
            return (dir == Direction.UP || dir == getCachedState().get(DIRECTION).getFirstDirection().getOpposite()) && !(stacks.get(slot).getCount() + stack.getCount() > slotSize /* Hacky replacement for getMaxCountPerStack() */);
        } else return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir == Direction.DOWN /* Temporary */ || dir == getCachedState().get(DIRECTION).getSecondDirection();
    }
    
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    
    @Override
    public void markDirty()  {
        ((ServerWorld) world).getChunkManager().markForUpdate(pos);
    }
    
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbtWithIdentifyingData();
    }

    public ConveyorBasicBlockEntity(BlockPos pos, BlockState state) {
        super(ConveyorBasic.CONVEYOR_BASIC_BLOCK_ENTITY, pos, state);
        this.inventorySize = ((ConveyorBasicBlock)state.getBlock()).getInventorySize();
        this.slotSize = ((ConveyorBasicBlock)state.getBlock()).getSlotSize();
        this.stacks = DefaultedList.ofSize(size(), ItemStack.EMPTY);
        this.transferSpeed = ((ConveyorBasicBlock)state.getBlock()).getTransferSpeed();
        this.progress = new int[inventorySize];
        this.blocked = false;
    }
}
