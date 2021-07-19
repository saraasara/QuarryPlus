package com.yogpc.qp.render;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

@Environment(EnvType.CLIENT)
@SuppressWarnings("DuplicatedCode")
public class RenderQuarry implements BlockEntityRenderer<TileQuarry> {
    private static final double d1 = 1d / 16d;
    private static final double d4 = 4d / 16d;

    @SuppressWarnings("unused")
    public RenderQuarry(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public boolean rendersOutsideBoundingBox(TileQuarry blockEntity) {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 256;
    }

    @Override
    public void render(TileQuarry quarry, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient.getInstance().getProfiler().push(QuarryPlus.modID);
        MinecraftClient.getInstance().getProfiler().push("RenderQuarry");
        matrices.push();
        var pos = quarry.getPos();
        matrices.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        if (quarry.getArea() != null) {
            switch (quarry.state) {
                case WAITING, MAKE_FRAME -> renderFrame(quarry, matrices, vertexConsumers);
                case BREAK_BLOCK, MOVE_HEAD -> renderDrill(quarry, matrices, vertexConsumers);
            }
        }

        matrices.pop();
        MinecraftClient.getInstance().getProfiler().pop();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    private void renderFrame(TileQuarry quarry, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        assert quarry.getArea() != null; // Null check is done.

        var buffer = new Buffer(vertexConsumers.getBuffer(RenderLayer.getCutout()), matrices);
        matrices.translate(0.5, 0.5, 0.5);
        var minX = quarry.getArea().minX();
        var minY = quarry.getArea().minY();
        var minZ = quarry.getArea().minZ();
        var maxX = quarry.getArea().maxX();
        var maxY = quarry.getArea().maxY();
        var maxZ = quarry.getArea().maxZ();
        var subtract = new Vec3i(maxX - minX, maxY - minY, maxZ - minZ);
        var mXm = minX - d1;
        var mXP = minX + d1;
        var mYm = minY - d1;
        var mYP = minY + d1;
        var mZm = minZ - d1;
        var mZP = minZ + d1;

        var MXm = maxX - d1;
        var MXP = maxX + d1;
        var MYm = maxY - d1;
        var MYP = maxY + d1;
        var MZm = maxZ - d1;
        var MZP = maxZ + d1;

        var spriteV = Sprites.INSTANCE.getFrameV();
        var spriteH = Sprites.INSTANCE.getFrameH();
        var boxStripe = Sprites.INSTANCE.getBoxStripe();
        var V_minU = spriteV.getMinU();
        var V_minV = spriteV.getMinV();
        var V_maxU = spriteV.getFrameU(8);
        var V_maxV = spriteV.getFrameV(8);
        var B_minU = boxStripe.getMinU();
        var B_minV = boxStripe.getMinV();
        var B_maxU = boxStripe.getMaxU();
        var B_maxV = boxStripe.getMaxV();
        for (int i = 0; i < subtract.getX(); i++) {
            var n = i == subtract.getX() - 1 ? 1 - d1 * 2 : 1d;
            var mXi = mXP + i + 0;
            var mXn = mXP + i + n;
            tempYFrameX(minY, buffer, mXi, mXn, mZm, mZP, MZm, MZP, V_minU, V_maxU, V_minV, V_maxV);
            tempYFrameX(maxY, buffer, mXi, mXn, mZm, mZP, MZm, MZP, V_minU, V_maxU, V_minV, V_maxV);
            tempZFrameX(minZ, buffer, mXi, mXn, mYm, mYP, MYm, MYP, V_minU, V_maxU, V_minV, V_maxV);
            tempZFrameX(maxZ, buffer, mXi, mXn, mYm, mYP, MYm, MYP, V_minU, V_maxU, V_minV, V_maxV);
        }
        for (int i = 0; i < subtract.getY(); i++) {
            var n = i == subtract.getY() - 1 ? 1 - d1 * 2 : 1d;
            var H_minU = spriteH.getMinU();
            var H_minV = spriteH.getMinV();
            var H_maxU = spriteH.getFrameU(8);
            var H_maxV = spriteH.getFrameV(8);

            var y0 = mYP + i + 0;
            var yn = mYP + i + n;
            tempFrameY(buffer, y0, yn, mXm, mXP, mZm, mZP, MXm, MXP, MZm, MZP, H_minU, H_maxU, H_minV, H_maxV);
        }
        for (int i = 0; i < subtract.getZ(); i++) {
            var n = i == subtract.getZ() - 1 ? 1 - d1 * 2 : 1d;
            var mZi = mZP + i + 0;
            var mZn = mZP + i + n;
            tempYFrameZ(minY, buffer, mXm, mXP, mZi, mZn, MXm, MXP, V_minU, V_maxU, V_minV, V_maxV);
            tempYFrameZ(maxY, buffer, mXm, mXP, mZi, mZn, MXm, MXP, V_minU, V_maxU, V_minV, V_maxV);
            tempXFrameZ(minX, buffer, mYm, mYP, mZi, mZn, MYm, MYP, V_minU, V_maxU, V_minV, V_maxV);
            tempXFrameZ(maxX, buffer, mYm, mYP, mZi, mZn, MYm, MYP, V_minU, V_maxU, V_minV, V_maxV);
        }
        renderMiniBoxFrame(buffer, mXm, mXP, mYm, mYP, mZm, mZP, MXm, MXP, MYm, MYP, MZm, MZP, B_minU, B_maxU, B_minV, B_maxV);
    }

    private static void tempYFrameX(double y, Buffer buffer, double mXi, double mXn, double mZm, double mZP, double MZm, double MZP, float V_minU, float V_maxU, float V_minV, float V_maxV) {
        var ymd = y - d1;
        var yPd = y + d1;
        buffer.pos(mXi, ymd, mZm).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(mXn, ymd, mZm).colored().tex(V_maxU, V_minV).lightedAndEnd();
        buffer.pos(mXn, ymd, mZP).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(mXi, ymd, mZP).colored().tex(V_minU, V_maxV).lightedAndEnd();

        buffer.pos(mXi, ymd, MZm).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(mXn, ymd, MZm).colored().tex(V_maxU, V_minV).lightedAndEnd();
        buffer.pos(mXn, ymd, MZP).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(mXi, ymd, MZP).colored().tex(V_minU, V_maxV).lightedAndEnd();

        buffer.pos(mXi, yPd, mZm).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(mXi, yPd, mZP).colored().tex(V_minU, V_maxV).lightedAndEnd();
        buffer.pos(mXn, yPd, mZP).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(mXn, yPd, mZm).colored().tex(V_maxU, V_minV).lightedAndEnd();

        buffer.pos(mXi, yPd, MZm).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(mXi, yPd, MZP).colored().tex(V_minU, V_maxV).lightedAndEnd();
        buffer.pos(mXn, yPd, MZP).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(mXn, yPd, MZm).colored().tex(V_maxU, V_minV).lightedAndEnd();
    }

    private static void tempZFrameX(double z, Buffer buffer, double mXi, double mXn, double mYm, double mYP, double MYm, double MYP, float V_minU, float V_maxU, float V_minV, float V_maxV) {
        var zmd = z - d1;
        var zPd = z + d1;
        buffer.pos(mXi, mYP, zmd).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(mXn, mYP, zmd).colored().tex(V_maxU, V_minV).lightedAndEnd();
        buffer.pos(mXn, mYm, zmd).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(mXi, mYm, zmd).colored().tex(V_minU, V_maxV).lightedAndEnd();

        buffer.pos(mXi, MYP, zmd).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(mXn, MYP, zmd).colored().tex(V_maxU, V_minV).lightedAndEnd();
        buffer.pos(mXn, MYm, zmd).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(mXi, MYm, zmd).colored().tex(V_minU, V_maxV).lightedAndEnd();

        buffer.pos(mXi, mYP, zPd).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(mXi, mYm, zPd).colored().tex(V_minU, V_maxV).lightedAndEnd();
        buffer.pos(mXn, mYm, zPd).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(mXn, mYP, zPd).colored().tex(V_maxU, V_minV).lightedAndEnd();

        buffer.pos(mXi, MYP, zPd).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(mXi, MYm, zPd).colored().tex(V_minU, V_maxV).lightedAndEnd();
        buffer.pos(mXn, MYm, zPd).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(mXn, MYP, zPd).colored().tex(V_maxU, V_minV).lightedAndEnd();
    }

    private static void tempFrameY(Buffer buffer, double y0, double yn, double mXm, double mXP, double mZm, double mZP, double MXm, double MXP, double MZm, double MZP, float H_minU, float H_maxU, float H_minV, float H_maxV) {
        buffer.pos(mXm, y0, mZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(mXm, yn, mZm).colored().tex(H_minU, H_maxV).lightedAndEnd();
        buffer.pos(mXP, yn, mZm).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(mXP, y0, mZm).colored().tex(H_maxU, H_minV).lightedAndEnd();

        buffer.pos(MXm, y0, mZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(MXm, yn, mZm).colored().tex(H_minU, H_maxV).lightedAndEnd();
        buffer.pos(MXP, yn, mZm).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(MXP, y0, mZm).colored().tex(H_maxU, H_minV).lightedAndEnd();

        buffer.pos(mXm, y0, MZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(mXm, yn, MZm).colored().tex(H_minU, H_maxV).lightedAndEnd();
        buffer.pos(mXP, yn, MZm).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(mXP, y0, MZm).colored().tex(H_maxU, H_minV).lightedAndEnd();

        buffer.pos(MXm, y0, MZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(MXm, yn, MZm).colored().tex(H_minU, H_maxV).lightedAndEnd();
        buffer.pos(MXP, yn, MZm).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(MXP, y0, MZm).colored().tex(H_maxU, H_minV).lightedAndEnd();

        buffer.pos(mXm, y0, mZP).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(mXP, y0, mZP).colored().tex(H_maxU, H_minV).lightedAndEnd();
        buffer.pos(mXP, yn, mZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(mXm, yn, mZP).colored().tex(H_minU, H_maxV).lightedAndEnd();

        buffer.pos(MXm, y0, mZP).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(MXP, y0, mZP).colored().tex(H_maxU, H_minV).lightedAndEnd();
        buffer.pos(MXP, yn, mZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(MXm, yn, mZP).colored().tex(H_minU, H_maxV).lightedAndEnd();

        buffer.pos(mXm, y0, MZP).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(mXP, y0, MZP).colored().tex(H_maxU, H_minV).lightedAndEnd();
        buffer.pos(mXP, yn, MZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(mXm, yn, MZP).colored().tex(H_minU, H_maxV).lightedAndEnd();

        buffer.pos(MXm, y0, MZP).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(MXP, y0, MZP).colored().tex(H_maxU, H_minV).lightedAndEnd();
        buffer.pos(MXP, yn, MZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(MXm, yn, MZP).colored().tex(H_minU, H_maxV).lightedAndEnd();

        // => X;
        buffer.pos(mXm, y0, mZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(mXm, y0, mZP).colored().tex(H_maxU, H_minV).lightedAndEnd();
        buffer.pos(mXm, yn, mZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(mXm, yn, mZm).colored().tex(H_minU, H_maxV).lightedAndEnd();

        buffer.pos(MXm, y0, mZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(MXm, y0, mZP).colored().tex(H_maxU, H_minV).lightedAndEnd();
        buffer.pos(MXm, yn, mZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(MXm, yn, mZm).colored().tex(H_minU, H_maxV).lightedAndEnd();

        buffer.pos(mXm, y0, MZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(mXm, y0, MZP).colored().tex(H_maxU, H_minV).lightedAndEnd();
        buffer.pos(mXm, yn, MZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(mXm, yn, MZm).colored().tex(H_minU, H_maxV).lightedAndEnd();

        buffer.pos(MXm, y0, MZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(MXm, y0, MZP).colored().tex(H_maxU, H_minV).lightedAndEnd();
        buffer.pos(MXm, yn, MZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(MXm, yn, MZm).colored().tex(H_minU, H_maxV).lightedAndEnd();

        buffer.pos(mXP, y0, mZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(mXP, yn, mZm).colored().tex(H_minU, H_maxV).lightedAndEnd();
        buffer.pos(mXP, yn, mZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(mXP, y0, mZP).colored().tex(H_maxU, H_minV).lightedAndEnd();

        buffer.pos(MXP, y0, mZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(MXP, yn, mZm).colored().tex(H_minU, H_maxV).lightedAndEnd();
        buffer.pos(MXP, yn, mZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(MXP, y0, mZP).colored().tex(H_maxU, H_minV).lightedAndEnd();

        buffer.pos(mXP, y0, MZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(mXP, yn, MZm).colored().tex(H_minU, H_maxV).lightedAndEnd();
        buffer.pos(mXP, yn, MZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(mXP, y0, MZP).colored().tex(H_maxU, H_minV).lightedAndEnd();

        buffer.pos(MXP, y0, MZm).colored().tex(H_minU, H_minV).lightedAndEnd();
        buffer.pos(MXP, yn, MZm).colored().tex(H_minU, H_maxV).lightedAndEnd();
        buffer.pos(MXP, yn, MZP).colored().tex(H_maxU, H_maxV).lightedAndEnd();
        buffer.pos(MXP, y0, MZP).colored().tex(H_maxU, H_minV).lightedAndEnd();
    }

    private static void tempYFrameZ(double y, Buffer buffer, double mXm, double mXP, double mZi, double mZn, double MXm, double MXP, float V_minU, float V_maxU, float V_minV, float V_maxV) {
        var ymd = y - d1;
        var yPd = y + d1;
        buffer.pos(mXm, ymd, mZi).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(mXP, ymd, mZi).colored().tex(V_minU, V_maxV).lightedAndEnd();
        buffer.pos(mXP, ymd, mZn).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(mXm, ymd, mZn).colored().tex(V_maxU, V_minV).lightedAndEnd();

        buffer.pos(MXm, ymd, mZi).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(MXP, ymd, mZi).colored().tex(V_minU, V_maxV).lightedAndEnd();
        buffer.pos(MXP, ymd, mZn).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(MXm, ymd, mZn).colored().tex(V_maxU, V_minV).lightedAndEnd();

        buffer.pos(mXm, yPd, mZi).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(mXm, yPd, mZn).colored().tex(V_maxU, V_minV).lightedAndEnd();
        buffer.pos(mXP, yPd, mZn).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(mXP, yPd, mZi).colored().tex(V_minU, V_maxV).lightedAndEnd();

        buffer.pos(MXm, yPd, mZi).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(MXm, yPd, mZn).colored().tex(V_maxU, V_minV).lightedAndEnd();
        buffer.pos(MXP, yPd, mZn).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(MXP, yPd, mZi).colored().tex(V_minU, V_maxV).lightedAndEnd();
    }

    private static void tempXFrameZ(double x, Buffer buffer, double mYm, double mYP, double mZi, double mZn, double MYm, double MYP, float V_minU, float V_maxU, float V_minV, float V_maxV) {
        var xmd = x - d1;
        var xPd = x + d1;
        buffer.pos(xmd, mYP, mZi).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(xmd, mYm, mZi).colored().tex(V_minU, V_maxV).lightedAndEnd();
        buffer.pos(xmd, mYm, mZn).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(xmd, mYP, mZn).colored().tex(V_maxU, V_minV).lightedAndEnd();

        buffer.pos(xmd, MYP, mZi).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(xmd, MYm, mZi).colored().tex(V_minU, V_maxV).lightedAndEnd();
        buffer.pos(xmd, MYm, mZn).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(xmd, MYP, mZn).colored().tex(V_maxU, V_minV).lightedAndEnd();

        buffer.pos(xPd, mYP, mZi).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(xPd, mYP, mZn).colored().tex(V_maxU, V_minV).lightedAndEnd();
        buffer.pos(xPd, mYm, mZn).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(xPd, mYm, mZi).colored().tex(V_minU, V_maxV).lightedAndEnd();

        buffer.pos(xPd, MYP, mZi).colored().tex(V_minU, V_minV).lightedAndEnd();
        buffer.pos(xPd, MYP, mZn).colored().tex(V_maxU, V_minV).lightedAndEnd();
        buffer.pos(xPd, MYm, mZn).colored().tex(V_maxU, V_maxV).lightedAndEnd();
        buffer.pos(xPd, MYm, mZi).colored().tex(V_minU, V_maxV).lightedAndEnd();
    }

    private static void renderMiniBoxFrame(Buffer buffer, double mXm, double mXP, double mYm, double mYP, double mZm, double mZP, double MXm, double MXP, double MYm, double MYP, double MZm, double MZP, float B_minU, float B_maxU, float B_minV, float B_maxV) {
        // z
        buffer.pos(mXP, mYP, mZm).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXP, mYm, mZm).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(mXm, mYm, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, mYP, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        buffer.pos(MXP, mYP, mZm).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXP, mYm, mZm).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(MXm, mYm, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXm, mYP, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        buffer.pos(mXP, mYP, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXm, mYP, MZP).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, mYm, MZP).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXP, mYm, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();

        buffer.pos(MXP, mYP, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXm, mYP, MZP).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(MXm, mYm, MZP).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXP, mYm, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();

        buffer.pos(mXP, MYP, mZm).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXP, MYm, mZm).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(mXm, MYm, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, MYP, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        buffer.pos(MXP, MYP, mZm).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXP, MYm, mZm).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(MXm, MYm, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXm, MYP, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        buffer.pos(mXP, MYP, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXm, MYP, MZP).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, MYm, MZP).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXP, MYm, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();

        buffer.pos(MXP, MYP, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXm, MYP, MZP).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(MXm, MYm, MZP).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXP, MYm, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();

        // x
        buffer.pos(mXm, mYP, mZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXm, mYP, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, mYm, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, mYm, mZP).colored().tex(B_maxU, B_minV).lightedAndEnd();

        buffer.pos(mXm, mYP, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXm, mYP, MZm).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, mYm, MZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, mYm, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();

        buffer.pos(mXm, MYP, mZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXm, MYP, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, MYm, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, MYm, mZP).colored().tex(B_maxU, B_minV).lightedAndEnd();

        buffer.pos(mXm, MYP, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXm, MYP, MZm).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, MYm, MZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, MYm, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();

        buffer.pos(MXP, mYP, mZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXP, mYm, mZP).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(MXP, mYm, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXP, mYP, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        buffer.pos(MXP, mYP, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXP, mYm, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(MXP, mYm, MZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXP, mYP, MZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        buffer.pos(MXP, MYP, mZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXP, MYm, mZP).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(MXP, MYm, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXP, MYP, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        buffer.pos(MXP, MYP, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXP, MYm, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(MXP, MYm, MZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXP, MYP, MZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        // y
        // Lower Side
        buffer.pos(mXP, mYm, mZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXm, mYm, mZP).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(mXm, mYm, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXP, mYm, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        buffer.pos(MXP, mYm, mZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXm, mYm, mZP).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(MXm, mYm, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXP, mYm, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        buffer.pos(mXP, mYm, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXm, mYm, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(mXm, mYm, MZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXP, mYm, MZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        buffer.pos(MXP, mYm, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXm, mYm, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();
        buffer.pos(MXm, mYm, MZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXP, mYm, MZm).colored().tex(B_minU, B_maxV).lightedAndEnd();

        // Upper Side
        buffer.pos(mXP, MYP, mZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXP, MYP, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, MYP, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, MYP, mZP).colored().tex(B_maxU, B_minV).lightedAndEnd();

        buffer.pos(MXP, MYP, mZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXP, MYP, mZm).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(MXm, MYP, mZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXm, MYP, mZP).colored().tex(B_maxU, B_minV).lightedAndEnd();

        buffer.pos(mXP, MYP, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(mXP, MYP, MZm).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, MYP, MZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(mXm, MYP, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();

        buffer.pos(MXP, MYP, MZP).colored().tex(B_minU, B_minV).lightedAndEnd();
        buffer.pos(MXP, MYP, MZm).colored().tex(B_minU, B_maxV).lightedAndEnd();
        buffer.pos(MXm, MYP, MZm).colored().tex(B_maxU, B_maxV).lightedAndEnd();
        buffer.pos(MXm, MYP, MZP).colored().tex(B_maxU, B_minV).lightedAndEnd();
    }

    private void renderDrill(TileQuarry quarry, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        assert quarry.getArea() != null; // Null check is done.
        var buffer = new Buffer(vertexConsumers.getBuffer(RenderLayer.getCutout()), matrices);
        matrices.translate(0.5, 0.5, 0.5);
        var minX = quarry.getArea().minX();
        var minZ = quarry.getArea().minZ();
        var maxX = quarry.getArea().maxX();
        var maxY = quarry.getArea().maxY();
        var maxZ = quarry.getArea().maxZ();
        var headPosX = quarry.headX;
        var headPosY = quarry.headY;
        var headPosZ = quarry.headZ;

        var drillStripe = Sprites.INSTANCE.getDrillStripe();
        var headSprite = Sprites.INSTANCE.getDrillHeadStripe();
        var D_minU = drillStripe.getMinU();
        var D_minV = drillStripe.getMinV();
        var D_maxU = drillStripe.getFrameU(8);
        var D_maxV = drillStripe.getMaxV();

        var hXmd = headPosX - d4;
        var hXPd = headPosX + d4;
        var MYmd = maxY - d4;
        var MYPd = maxY + d4;
        var hZmd = headPosZ - d4;
        var hZPd = headPosZ + d4;

        //X lines
        //positive(East)
        var xp_length = maxX - headPosX - d4 * 2;
        var xp_floor = MathHelper.floor(xp_length);
        xLineDrill(buffer, true, xp_floor, xp_length, headPosX, drillStripe, MYmd, MYPd, hZmd, hZPd, D_minU, D_maxU, D_minV, D_maxV);
        //negative(West)
        var xn_length = headPosX - minX - d4 * 2;
        var xn_floor = MathHelper.floor(xn_length);
        xLineDrill(buffer, false, xn_floor, xn_length, headPosX, drillStripe, MYmd, MYPd, hZmd, hZPd, D_minU, D_maxU, D_minV, D_maxV);
        //Z lines
        //positive(South)
        var zp_length = maxZ - headPosZ - d4 * 2;
        var zp_floor = MathHelper.floor(zp_length);
        zLineDrill(buffer, true, zp_floor, zp_length, headPosZ, drillStripe, hXmd, hXPd, MYmd, MYPd, D_minU, D_maxU, D_minV, D_maxV);
        //negative(North)
        var zn_length = headPosZ - minZ - d4 * 2;
        var zn_floor = MathHelper.floor(zn_length);
        zLineDrill(buffer, false, zn_floor, zn_length, headPosZ, drillStripe, hXmd, hXPd, MYmd, MYPd, D_minU, D_maxU, D_minV, D_maxV);

        var y_length = maxY - headPosY - 0.75;
        var y_floor = MathHelper.floor(y_length);
        yLineDrill(buffer, y_floor, y_length, drillStripe, headSprite, headPosX, headPosY, headPosZ, hXmd, hXPd, MYPd, hZmd, hZPd, D_minU, D_maxU, D_minV);
    }

    private static void xLineDrill(Buffer buffer, boolean plus, int floor, double length, double headPosX, Sprite drillStripe,
                                   double MYmd, double MYPd, double hZmd, double hZPd, float D_minU, float D_maxU, float D_minV, float D_maxV) {
        for (int i1 = 0; i1 < floor; i1++) {
            int i2 = i1 + 1;
            var fX1 = plus ? headPosX + (d4 + i1) : headPosX - (d4 + i1);
            var fX2 = plus ? headPosX + (d4 + i2) : headPosX - (d4 + i2);
            if (plus) {
                buffer.pos(fX1, MYPd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(fX1, MYPd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();
                buffer.pos(fX2, MYPd, hZPd).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(fX2, MYPd, hZmd).colored().tex(D_minU, D_maxV).lightedAndEnd();

                buffer.pos(fX1, MYmd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(fX2, MYmd, hZmd).colored().tex(D_minU, D_maxV).lightedAndEnd();
                buffer.pos(fX2, MYmd, hZPd).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(fX1, MYmd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();

                buffer.pos(fX2, MYPd, hZmd).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(fX2, MYmd, hZmd).colored().tex(D_minU, D_maxV).lightedAndEnd();
                buffer.pos(fX1, MYmd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(fX1, MYPd, hZmd).colored().tex(D_maxU, D_minV).lightedAndEnd();

                buffer.pos(fX2, MYPd, hZPd).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(fX1, MYPd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();
                buffer.pos(fX1, MYmd, hZPd).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(fX2, MYmd, hZPd).colored().tex(D_minU, D_maxV).lightedAndEnd();
            } else {
                buffer.pos(fX1, MYPd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(fX2, MYPd, hZmd).colored().tex(D_minU, D_maxV).lightedAndEnd();
                buffer.pos(fX2, MYPd, hZPd).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(fX1, MYPd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();

                buffer.pos(fX1, MYmd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(fX1, MYmd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();
                buffer.pos(fX2, MYmd, hZPd).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(fX2, MYmd, hZmd).colored().tex(D_minU, D_maxV).lightedAndEnd();

                buffer.pos(fX2, MYPd, hZmd).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(fX1, MYPd, hZmd).colored().tex(D_maxU, D_minV).lightedAndEnd();
                buffer.pos(fX1, MYmd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(fX2, MYmd, hZmd).colored().tex(D_minU, D_maxV).lightedAndEnd();

                buffer.pos(fX2, MYPd, hZPd).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(fX2, MYmd, hZPd).colored().tex(D_minU, D_maxV).lightedAndEnd();
                buffer.pos(fX1, MYmd, hZPd).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(fX1, MYPd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();
            }
        }
        var fixedV = drillStripe.getFrameV((length - floor) * 16);
        var xF = plus ? headPosX + (d4 + floor) : headPosX - (d4 + floor);
        var xL = plus ? headPosX + (d4 + length) : headPosX - (d4 + length);
        if (plus) {
            buffer.pos(xF, MYPd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(xF, MYPd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();
            buffer.pos(xL, MYPd, hZPd).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(xL, MYPd, hZmd).colored().tex(D_minU, fixedV).lightedAndEnd();

            buffer.pos(xF, MYmd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(xL, MYmd, hZmd).colored().tex(D_minU, fixedV).lightedAndEnd();
            buffer.pos(xL, MYmd, hZPd).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(xF, MYmd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();

            buffer.pos(xL, MYPd, hZmd).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(xL, MYmd, hZmd).colored().tex(D_minU, fixedV).lightedAndEnd();
            buffer.pos(xF, MYmd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(xF, MYPd, hZmd).colored().tex(D_maxU, D_minV).lightedAndEnd();

            buffer.pos(xL, MYPd, hZPd).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(xF, MYPd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();
            buffer.pos(xF, MYmd, hZPd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(xL, MYmd, hZPd).colored().tex(D_minU, fixedV).lightedAndEnd();
        } else {
            buffer.pos(xF, MYPd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(xL, MYPd, hZmd).colored().tex(D_minU, fixedV).lightedAndEnd();
            buffer.pos(xL, MYPd, hZPd).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(xF, MYPd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();

            buffer.pos(xF, MYmd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(xF, MYmd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();
            buffer.pos(xL, MYmd, hZPd).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(xL, MYmd, hZmd).colored().tex(D_minU, fixedV).lightedAndEnd();

            buffer.pos(xL, MYPd, hZmd).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(xF, MYPd, hZmd).colored().tex(D_maxU, D_minV).lightedAndEnd();
            buffer.pos(xF, MYmd, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(xL, MYmd, hZmd).colored().tex(D_minU, fixedV).lightedAndEnd();

            buffer.pos(xL, MYPd, hZPd).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(xL, MYmd, hZPd).colored().tex(D_minU, fixedV).lightedAndEnd();
            buffer.pos(xF, MYmd, hZPd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(xF, MYPd, hZPd).colored().tex(D_maxU, D_minV).lightedAndEnd();
        }
    }

    private static void yLineDrill(Buffer buffer, int floor, double length, Sprite drillStripe, Sprite headSprite, double headPosX, double headPosY, double headPosZ,
                                   double hXmd, double hXPd, double MYPd, double hZmd, double hZPd, float D_minU, float D_maxU, float D_minV) {
        var D_I8dV = drillStripe.getFrameV(8d);
        var D_16dU = drillStripe.getMaxU();
        //Top
        buffer.pos(hXmd, MYPd, hZmd).colored().tex(D_minU, D_I8dV).lightedAndEnd();
        buffer.pos(hXmd, MYPd, hZPd).colored().tex(D_maxU, D_I8dV).lightedAndEnd();
        buffer.pos(hXPd, MYPd, hZPd).colored().tex(D_maxU, drillStripe.getMaxV()).lightedAndEnd();
        buffer.pos(hXPd, MYPd, hZmd).colored().tex(D_minU, drillStripe.getMaxV()).lightedAndEnd();
        for (int i1 = 0; i1 < floor; i1++) {
            int i2 = i1 + 1;
            var MY1 = MYPd - i1;
            var MY2 = MYPd - i2;
            buffer.pos(hXPd, MY1, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXPd, MY2, hZmd).colored().tex(D_16dU, D_minV).lightedAndEnd();
            buffer.pos(hXmd, MY2, hZmd).colored().tex(D_16dU, D_I8dV).lightedAndEnd();
            buffer.pos(hXmd, MY1, hZmd).colored().tex(D_minU, D_I8dV).lightedAndEnd();

            buffer.pos(hXPd, MY1, hZPd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXmd, MY1, hZPd).colored().tex(D_minU, D_I8dV).lightedAndEnd();
            buffer.pos(hXmd, MY2, hZPd).colored().tex(D_16dU, D_I8dV).lightedAndEnd();
            buffer.pos(hXPd, MY2, hZPd).colored().tex(D_16dU, D_minV).lightedAndEnd();

            buffer.pos(hXPd, MY1, hZPd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXPd, MY2, hZPd).colored().tex(D_16dU, D_minV).lightedAndEnd();
            buffer.pos(hXPd, MY2, hZmd).colored().tex(D_16dU, D_I8dV).lightedAndEnd();
            buffer.pos(hXPd, MY1, hZmd).colored().tex(D_minU, D_I8dV).lightedAndEnd();

            buffer.pos(hXmd, MY1, hZPd).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXmd, MY1, hZmd).colored().tex(D_minU, D_I8dV).lightedAndEnd();
            buffer.pos(hXmd, MY2, hZmd).colored().tex(D_16dU, D_I8dV).lightedAndEnd();
            buffer.pos(hXmd, MY2, hZPd).colored().tex(D_16dU, D_minV).lightedAndEnd();
        }
        var fixedU = drillStripe.getFrameU((length - floor) * 16);
        var MYF = MYPd - floor;
        var MYL = MYPd - length;
        buffer.pos(hXPd, MYF, hZmd).colored().tex(D_minU, D_minV).lightedAndEnd();
        buffer.pos(hXPd, MYL, hZmd).colored().tex(fixedU, D_minV).lightedAndEnd();
        buffer.pos(hXmd, MYL, hZmd).colored().tex(fixedU, D_I8dV).lightedAndEnd();
        buffer.pos(hXmd, MYF, hZmd).colored().tex(D_minU, D_I8dV).lightedAndEnd();

        buffer.pos(hXPd, MYF, hZPd).colored().tex(D_minU, D_minV).lightedAndEnd();
        buffer.pos(hXmd, MYF, hZPd).colored().tex(D_minU, D_I8dV).lightedAndEnd();
        buffer.pos(hXmd, MYL, hZPd).colored().tex(fixedU, D_I8dV).lightedAndEnd();
        buffer.pos(hXPd, MYL, hZPd).colored().tex(fixedU, D_minV).lightedAndEnd();

        buffer.pos(hXPd, MYF, hZPd).colored().tex(D_minU, D_minV).lightedAndEnd();
        buffer.pos(hXPd, MYL, hZPd).colored().tex(fixedU, D_minV).lightedAndEnd();
        buffer.pos(hXPd, MYL, hZmd).colored().tex(fixedU, D_I8dV).lightedAndEnd();
        buffer.pos(hXPd, MYF, hZmd).colored().tex(D_minU, D_I8dV).lightedAndEnd();

        buffer.pos(hXmd, MYF, hZPd).colored().tex(D_minU, D_minV).lightedAndEnd();
        buffer.pos(hXmd, MYF, hZmd).colored().tex(D_minU, D_I8dV).lightedAndEnd();
        buffer.pos(hXmd, MYL, hZmd).colored().tex(fixedU, D_I8dV).lightedAndEnd();
        buffer.pos(hXmd, MYL, hZPd).colored().tex(fixedU, D_minV).lightedAndEnd();

        //Bottom
        buffer.pos(hXmd, MYL, hZmd).colored().tex(D_minU, D_I8dV).lightedAndEnd();
        buffer.pos(hXPd, MYL, hZmd).colored().tex(D_minU, drillStripe.getMaxV()).lightedAndEnd();
        buffer.pos(hXPd, MYL, hZPd).colored().tex(D_maxU, drillStripe.getMaxV()).lightedAndEnd();
        buffer.pos(hXmd, MYL, hZPd).colored().tex(D_maxU, D_I8dV).lightedAndEnd();

        //Drill
        double xm = headPosX - d4 / 2;
        double xP = headPosX + d4 / 2;
        double zm = headPosZ - d4 / 2;
        double zP = headPosZ + d4 / 2;
        double yT = headPosY + 1;
        double yB = headPosY + 0;
        var hmU = headSprite.getMinU();
        var hMU = headSprite.getMaxU();
        var hmV = headSprite.getMinV();
        var hMV = headSprite.getFrameV(4);
        buffer.pos(xP, yT, zm).colored().tex(hmU, hmV).lightedAndEnd();
        buffer.pos(xP, yB, zm).colored().tex(hMU, hmV).lightedAndEnd();
        buffer.pos(xm, yB, zm).colored().tex(hMU, hMV).lightedAndEnd();
        buffer.pos(xm, yT, zm).colored().tex(hmU, hMV).lightedAndEnd();

        buffer.pos(xm, yT, zP).colored().tex(hmU, hmV).lightedAndEnd();
        buffer.pos(xm, yB, zP).colored().tex(hMU, hmV).lightedAndEnd();
        buffer.pos(xP, yB, zP).colored().tex(hMU, hMV).lightedAndEnd();
        buffer.pos(xP, yT, zP).colored().tex(hmU, hMV).lightedAndEnd();

        buffer.pos(xP, yT, zP).colored().tex(hmU, hmV).lightedAndEnd();
        buffer.pos(xP, yB, zP).colored().tex(hMU, hmV).lightedAndEnd();
        buffer.pos(xP, yB, zm).colored().tex(hMU, hMV).lightedAndEnd();
        buffer.pos(xP, yT, zm).colored().tex(hmU, hMV).lightedAndEnd();

        buffer.pos(xm, yT, zm).colored().tex(hmU, hmV).lightedAndEnd();
        buffer.pos(xm, yB, zm).colored().tex(hMU, hmV).lightedAndEnd();
        buffer.pos(xm, yB, zP).colored().tex(hMU, hMV).lightedAndEnd();
        buffer.pos(xm, yT, zP).colored().tex(hmU, hMV).lightedAndEnd();
    }

    private static void zLineDrill(Buffer buffer, boolean plus, int floor, double length, double headPosZ, Sprite drillStripe,
                                   double hXmd, double hXPd, double MYmd, double MYPd, float D_minU, float D_maxU, float D_minV, float D_maxV) {
        for (int i1 = 0; i1 < floor; i1++) {
            int i2 = i1 + 1;
            var fZ1 = plus ? headPosZ + (d4 + i1) : headPosZ - (d4 + i1);
            var fZ2 = plus ? headPosZ + (d4 + i2) : headPosZ - (d4 + i2);
            if (plus) {
                buffer.pos(hXmd, MYPd, fZ1).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(hXmd, MYPd, fZ2).colored().tex(D_minU, D_maxV).lightedAndEnd();
                buffer.pos(hXPd, MYPd, fZ2).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(hXPd, MYPd, fZ1).colored().tex(D_maxU, D_minV).lightedAndEnd();

                buffer.pos(hXmd, MYmd, fZ1).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(hXPd, MYmd, fZ1).colored().tex(D_maxU, D_minV).lightedAndEnd();
                buffer.pos(hXPd, MYmd, fZ2).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(hXmd, MYmd, fZ2).colored().tex(D_minU, D_maxV).lightedAndEnd();

                buffer.pos(hXmd, MYPd, fZ2).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(hXmd, MYPd, fZ1).colored().tex(D_maxU, D_minV).lightedAndEnd();
                buffer.pos(hXmd, MYmd, fZ1).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(hXmd, MYmd, fZ2).colored().tex(D_minU, D_maxV).lightedAndEnd();

                buffer.pos(hXPd, MYPd, fZ2).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(hXPd, MYmd, fZ2).colored().tex(D_minU, D_maxV).lightedAndEnd();
                buffer.pos(hXPd, MYmd, fZ1).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(hXPd, MYPd, fZ1).colored().tex(D_maxU, D_minV).lightedAndEnd();
            } else {
                buffer.pos(hXmd, MYPd, fZ1).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(hXPd, MYPd, fZ1).colored().tex(D_maxU, D_minV).lightedAndEnd();
                buffer.pos(hXPd, MYPd, fZ2).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(hXmd, MYPd, fZ2).colored().tex(D_minU, D_maxV).lightedAndEnd();

                buffer.pos(hXmd, MYmd, fZ1).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(hXmd, MYmd, fZ2).colored().tex(D_minU, D_maxV).lightedAndEnd();
                buffer.pos(hXPd, MYmd, fZ2).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(hXPd, MYmd, fZ1).colored().tex(D_maxU, D_minV).lightedAndEnd();

                buffer.pos(hXmd, MYPd, fZ2).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(hXmd, MYmd, fZ2).colored().tex(D_minU, D_maxV).lightedAndEnd();
                buffer.pos(hXmd, MYmd, fZ1).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(hXmd, MYPd, fZ1).colored().tex(D_maxU, D_minV).lightedAndEnd();

                buffer.pos(hXPd, MYPd, fZ2).colored().tex(D_maxU, D_maxV).lightedAndEnd();
                buffer.pos(hXPd, MYPd, fZ1).colored().tex(D_maxU, D_minV).lightedAndEnd();
                buffer.pos(hXPd, MYmd, fZ1).colored().tex(D_minU, D_minV).lightedAndEnd();
                buffer.pos(hXPd, MYmd, fZ2).colored().tex(D_minU, D_maxV).lightedAndEnd();
            }
        }
        var fixedV = drillStripe.getFrameV((length - floor) * 16);
        var zF = plus ? headPosZ + (d4 + floor) : headPosZ - (d4 + floor);
        var zL = plus ? headPosZ + (d4 + length) : headPosZ - (d4 + length);
        if (plus) {
            buffer.pos(hXmd, MYPd, zF).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXmd, MYPd, zL).colored().tex(D_minU, fixedV).lightedAndEnd();
            buffer.pos(hXPd, MYPd, zL).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(hXPd, MYPd, zF).colored().tex(D_maxU, D_minV).lightedAndEnd();

            buffer.pos(hXmd, MYmd, zF).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXPd, MYmd, zF).colored().tex(D_maxU, D_minV).lightedAndEnd();
            buffer.pos(hXPd, MYmd, zL).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(hXmd, MYmd, zL).colored().tex(D_minU, fixedV).lightedAndEnd();

            buffer.pos(hXmd, MYPd, zL).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(hXmd, MYPd, zF).colored().tex(D_maxU, D_minV).lightedAndEnd();
            buffer.pos(hXmd, MYmd, zF).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXmd, MYmd, zL).colored().tex(D_minU, fixedV).lightedAndEnd();

            buffer.pos(hXPd, MYPd, zL).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(hXPd, MYmd, zL).colored().tex(D_minU, fixedV).lightedAndEnd();
            buffer.pos(hXPd, MYmd, zF).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXPd, MYPd, zF).colored().tex(D_maxU, D_minV).lightedAndEnd();
        } else {
            buffer.pos(hXmd, MYPd, zF).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXPd, MYPd, zF).colored().tex(D_maxU, D_minV).lightedAndEnd();
            buffer.pos(hXPd, MYPd, zL).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(hXmd, MYPd, zL).colored().tex(D_minU, fixedV).lightedAndEnd();

            buffer.pos(hXmd, MYmd, zF).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXmd, MYmd, zL).colored().tex(D_minU, fixedV).lightedAndEnd();
            buffer.pos(hXPd, MYmd, zL).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(hXPd, MYmd, zF).colored().tex(D_maxU, D_minV).lightedAndEnd();

            buffer.pos(hXmd, MYPd, zL).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(hXmd, MYmd, zL).colored().tex(D_minU, fixedV).lightedAndEnd();
            buffer.pos(hXmd, MYmd, zF).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXmd, MYPd, zF).colored().tex(D_maxU, D_minV).lightedAndEnd();

            buffer.pos(hXPd, MYPd, zL).colored().tex(D_maxU, fixedV).lightedAndEnd();
            buffer.pos(hXPd, MYPd, zF).colored().tex(D_maxU, D_minV).lightedAndEnd();
            buffer.pos(hXPd, MYmd, zF).colored().tex(D_minU, D_minV).lightedAndEnd();
            buffer.pos(hXPd, MYmd, zL).colored().tex(D_minU, fixedV).lightedAndEnd();
        }
    }
}
