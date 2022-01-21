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
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.numra.tech.blocks.ConveyorDirection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.numra.tech.blocks.ConveyorBasicBlock.DIRECTION;

public class ConveyorBasicBlockEntityRenderer implements BlockEntityRenderer<ConveyorBasicBlockEntity> {
    public DefaultedList<ItemStack> stacks;
    
    public ConveyorBasicBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    
    }
    
    public Quaternion getBeltRotationQuaternion(ConveyorBasicBlockEntity blockEntity, int index) {
        ConveyorDirection directions = blockEntity.getCachedState().get(DIRECTION);
        int progress = blockEntity.getProgress(index);
        if((directions.getFirstDirection() == directions.getSecondDirection()) || (progress <= 450)) {
            return getDirectionQuaternion(directions.getFirstDirection());
        } else if (progress <= 550){
            int rotation = switch(directions.getFirstDirection()) {
                case NORTH -> 180;
                case EAST -> 90;
                case SOUTH -> 0;
                default -> -90;
            };
            rotation += switch(directions.getSecondDirection()) {
                case NORTH, WEST -> 45;
                default -> -45;
            };
            if(rotation == Math.abs(rotation)) return Vec3f.POSITIVE_Y.getDegreesQuaternion(rotation); else return Vec3f.NEGATIVE_Y.getDegreesQuaternion(-rotation);
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
    
    private double getBeltTranslationValue(ConveyorBasicBlockEntity blockEntity, int index, String desiredValue) {
        if (Objects.equals(desiredValue, "y")) return 0.22;
        ConveyorDirection directions = blockEntity.getCachedState().get(DIRECTION);
        if (directions.getFirstDirection() == directions.getSecondDirection()) {
            switch (directions.getFirstDirection()) {
                case NORTH -> { if (Objects.equals(desiredValue, "x")) return 0.5; else return (double) -blockEntity.getProgress(index) / 1000 + 1; }
                case EAST -> { if (Objects.equals(desiredValue, "z")) return 0.5; else return (double) blockEntity.getProgress(index) / 1000; }
                case SOUTH -> { if (Objects.equals(desiredValue, "x")) return 0.5; else return (double) blockEntity.getProgress(index) / 1000; }
                default -> { if (Objects.equals(desiredValue, "z")) return 0.5; else return (double) -blockEntity.getProgress(index) / 1000 + 1; }
            }
        } else {
            return 0; //temp
        }
    }
    
    @Override
    public void render(ConveyorBasicBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        stacks = blockEntity.getStacks();
        int index = 0;
        for (ItemStack stack : stacks) {
            if (blockEntity.getProgress(index) == -1) continue;
            matrices.translate(getBeltTranslationValue(blockEntity, index, "x"), getBeltTranslationValue(blockEntity, index, "y"), getBeltTranslationValue(blockEntity, index, "z"));
            matrices.multiply(getBeltRotationQuaternion(blockEntity, index));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
            matrices.scale(0.9F, 0.9F, 0.9F);
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers,  0);
            index++;
        }
        matrices.pop();
    }
}
