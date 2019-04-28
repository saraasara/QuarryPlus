package com.yogpc.qp.machines.workbench;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.fonts.Font;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class GuiWorkbench extends GuiContainer {
    private static final class MyFontRenderer extends FontRenderer {
        private FontRenderer p;

        public MyFontRenderer() {
            super(Minecraft.getInstance().getTextureManager(), new Font(Minecraft.getInstance().getTextureManager(), new ResourceLocation("textures/font/ascii.png")));
        }

        @Override
        public int drawStringWithShadow(String text, float x, float y, int color) {
            int l = this.p.getStringWidth(text);
            GL11.glPushMatrix();
            GL11.glTranslatef(x, y, 0);
            if (l > 16) {
                final float f = (float) 16 / l;
                GL11.glTranslatef(l - 16, this.p.FONT_HEIGHT * (1 - f), 0);
                GL11.glScalef(f, f, 1);
            }
            l = this.p.drawStringWithShadow(text, 0, 0, color);
            GL11.glPopMatrix();
            return l;
        }

        @Override
        public int getStringWidth(final String s) {
            return this.p.getStringWidth(s);
        }

        FontRenderer setParent(final FontRenderer r) {
            this.p = r;
            return this;
        }
    }

    private static final class MyRenderItem extends ItemRenderer {
        private static final MyFontRenderer myFont = new MyFontRenderer();

        public MyRenderItem(TextureManager textureManager, ModelManager modelManager, ItemColors itemColors) {
            super(textureManager, modelManager, itemColors);
        }

        boolean t = true;

        @Override
        public void renderItemAndEffectIntoGUI(@Nullable EntityLivingBase livingBase, ItemStack stack, int xPosition, int yPosition) {
            if (t)
                Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(livingBase, stack, xPosition, yPosition);
            else
                super.renderItemAndEffectIntoGUI(livingBase, stack, xPosition, yPosition);
        }

        @Override
        public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
            if (stack.getCount() > 64)
                super.renderItemOverlayIntoGUI(myFont.setParent(fr), stack, xPosition, yPosition, text);
            else
                Minecraft.getInstance().getItemRenderer().renderItemOverlayIntoGUI(fr, stack, xPosition, yPosition, text);
        }
    }

    private static final ItemRenderer myItem = new MyRenderItem(
        Minecraft.getInstance().getTextureManager(),
        Minecraft.getInstance().getItemRenderer().getItemModelMesher().getModelManager(),
        Minecraft.getInstance().getItemColors());

    private static final ResourceLocation gui = new ResourceLocation(QuarryPlus.modID, "textures/gui/workbench.png");
    private final TileWorkbench tile;

    public GuiWorkbench(EntityPlayer player, final TileWorkbench tw) {
        super(new ContainerWorkbench(player, tw));
        this.xSize = 176;
        this.ySize = 222;
        this.tile = tw;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(I18n.format(TranslationKeys.workbench), 8, 6, 0x404040);
        this.fontRenderer.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(gui);
        final int xf = this.width - this.xSize >> 1;
        final int yf = this.height - this.ySize >> 1;
        drawTexturedModalRect(xf, yf, 0, 0, this.xSize, this.ySize);
        if (tile.isWorking()) {
            drawTexturedModalRect(xf + 8, yf + 78, 0, this.ySize, this.tile.getProgressScaled(160), 4);
            int cur_recipe = 27 + tile.getRecipeIndex();
            int i = (tile.workContinue ? 16 : 0);
            drawTexturedModalRect(xf + 8 + cur_recipe % 9 * 18, yf + 90 + (cur_recipe / 9 - 3) * 18, this.xSize + i, 0, 16, 16);
        }
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        handlePre();
        this.drawDefaultBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        handlePost();
    }

    @Nullable
    private ItemRenderer preItem;

    public void handlePre() {
        preItem = itemRender;
        itemRender = myItem;
    }

    public void handlePost() {
        if (preItem != null) {
            itemRender = preItem;
            preItem = null;
        }
    }
}
