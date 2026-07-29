package com.qituo.dcc.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class PufferFishCastingScreen extends Screen {
    private final int castingTime;
    private final int startTime;

    public PufferFishCastingScreen(int castingTime) {
        super(Text.translatable("item.dcc.uncles_dried_puffer_fish"));
        this.castingTime = castingTime;
        this.startTime = (int) System.currentTimeMillis();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int screenWidth = this.width;
        int screenHeight = this.height;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int elapsedTime = (int) (System.currentTimeMillis() - startTime);
        float progress = Math.min((float) elapsedTime / (castingTime * 50), 1.0f);

        int progressBarWidth = 200;
        int progressBarHeight = 24;
        int progressBarX = centerX - progressBarWidth / 2;
        int progressBarY = centerY;

        context.fill(progressBarX - 2, progressBarY - 2, progressBarX + progressBarWidth + 2, progressBarY + progressBarHeight + 2, 0xFF000000);
        context.fill(progressBarX - 1, progressBarY - 1, progressBarX + progressBarWidth + 1, progressBarY + progressBarHeight + 1, 0xFF888888);
        context.fill(progressBarX, progressBarY, progressBarX + progressBarWidth, progressBarY + progressBarHeight, 0xFF333333);

        int filledWidth = (int) (progressBarWidth * progress);
        if (filledWidth > 0) {
            for (int i = 0; i < filledWidth; i++) {
                float ratio = (float) i / filledWidth;
                int r = (int) (0 + ratio * 0);
                int g = (int) (255 - ratio * 100);
                int b = (int) (0 + ratio * 0);
                int color = (0xFF << 24) | (r << 16) | (g << 8) | b;
                context.fill(progressBarX + i, progressBarY, progressBarX + i + 1, progressBarY + progressBarHeight, color);
            }
        }

        context.fill(progressBarX, progressBarY, progressBarX + 1, progressBarY + progressBarHeight, 0xFFAAAAAA);
        context.fill(progressBarX + progressBarWidth, progressBarY, progressBarX + progressBarWidth + 1, progressBarY + progressBarHeight, 0xFF555555);
        context.fill(progressBarX, progressBarY, progressBarX + progressBarWidth, progressBarY + 1, 0xFFAAAAAA);
        context.fill(progressBarX, progressBarY + progressBarHeight, progressBarX + progressBarWidth, progressBarY + progressBarHeight + 1, 0xFF555555);

        context.drawText(this.textRenderer, Text.translatable("casting.dcc.casting"), centerX - this.textRenderer.getWidth(Text.translatable("casting.dcc.casting")) / 2, centerY - 40, 0xFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    @Override
    public boolean isFocused() {
        return false;
    }
}