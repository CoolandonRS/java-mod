package net.numra.tech.blocks.blockentities;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.PositionImpl;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.numra.tech.blocks.ConveyorDirection;
import org.jetbrains.annotations.NotNull;

import static net.numra.tech.blocks.ConveyorBasicBlock.DIRECTION;

public class ConveyorBasicBlockEntityRenderer implements BlockEntityRenderer<ConveyorBasicBlockEntity> {
    public DefaultedList<ItemStack> stacks;
    
    public ConveyorBasicBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    
    }
    
    public Quaternion getBeltRotationQuaternion(ConveyorBasicBlockEntity blockEntity, int index) {
        ConveyorDirection directions = blockEntity.getCachedState().get(DIRECTION);
        int progress = blockEntity.getProgress(index);
        if((directions.getFirstDirection() == directions.getSecondDirection()) || (progress <= 500)) {
            return getDirectionQuaternion(directions.getFirstDirection());
        } else {
            return getDirectionQuaternion(directions.getSecondDirection());
        }
    }
    
    @NotNull
    private Quaternion getDirectionQuaternion(Direction dir) {
        return switch (dir) {
            case NORTH -> Vec3f.POSITIVE_Y.getDegreesQuaternion(180);
            case EAST -> Vec3f.POSITIVE_Y.getDegreesQuaternion(90);
            case SOUTH -> Vec3f.POSITIVE_Y.getDegreesQuaternion(0);
            default -> Vec3f.NEGATIVE_Y.getDegreesQuaternion(90);
        };
    }
    
    private PositionImpl getBeltTranslations(ConveyorBasicBlockEntity blockEntity, int index) {
        double x, y = 0.22, z;
        ConveyorDirection directions = blockEntity.getCachedState().get(DIRECTION);
        Direction firstDirection = directions.getFirstDirection();
        Direction secondDirection = directions.getSecondDirection();
        double t;
        if (index != -1) t = (double) blockEntity.getProgress(index) / 1000; else t = 1;
        if (firstDirection == secondDirection || t <= 0.5) {
            x = switch (firstDirection) {
                case NORTH, SOUTH -> 0.5;
                case EAST -> t;
                default -> -t + 1;
            };
            z = switch (firstDirection) {
                case EAST, WEST -> 0.5;
                case NORTH -> -t + 1;
                default -> t;
            };
        } else {
            x = switch (secondDirection) {
                case NORTH, SOUTH -> 0.5;
                case EAST -> t;
                default -> -t + 1;
            };
            z = switch (secondDirection) {
                case EAST, WEST -> 0.5;
                case NORTH -> -t + 1;
                default -> t;
            };
        }
        return new PositionImpl(x, y, z);
    }
    
    @Override
    public void render(ConveyorBasicBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        stacks = blockEntity.getStacks();
        int index = 0;
        for (ItemStack stack : stacks) {
            matrices.push();
            PositionImpl translationValues;
            if (blockEntity.getProgress(index) != -1) translationValues = getBeltTranslations(blockEntity, index); else translationValues = getBeltTranslations(blockEntity, -1);
            matrices.translate(translationValues.getX(), translationValues.getY(), translationValues.getZ());
            matrices.multiply(getBeltRotationQuaternion(blockEntity, index));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
            matrices.scale(0.9F, 0.9F, 0.9F);
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers,  0);
            matrices.pop();
            index++;
        }
    }
}
