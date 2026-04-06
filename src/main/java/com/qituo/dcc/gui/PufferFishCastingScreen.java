package com.qituo.dcc.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PufferFishCastingScreen extends Screen {
    private final int castingTime;
    private final int startTime;
    
    public PufferFishCastingScreen(int castingTime) {
        super(Component.translatable("item.dcc.uncles_dried_puffer_fish"));
        this.castingTime = castingTime;
        this.startTime = (int) System.currentTimeMillis();
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 不调用super.render，这样就不会绘制背景，玩家可以看到游戏世界
        // super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int screenWidth = this.width;
        int screenHeight = this.height;
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        
        // 计算进度
        int elapsedTime = (int) (System.currentTimeMillis() - startTime);
        float progress = Math.min((float) elapsedTime / (castingTime * 50), 1.0f); // 50ms per tick
        
        // 绘制进度条
        int progressBarWidth = 200;
        int progressBarHeight = 24;
        int progressBarX = centerX - progressBarWidth / 2;
        int progressBarY = centerY;
        
        // 绘制进度条背景
        guiGraphics.fill(progressBarX - 2, progressBarY - 2, progressBarX + progressBarWidth + 2, progressBarY + progressBarHeight + 2, 0xFF000000);
        guiGraphics.fill(progressBarX - 1, progressBarY - 1, progressBarX + progressBarWidth + 1, progressBarY + progressBarHeight + 1, 0xFF888888);
        guiGraphics.fill(progressBarX, progressBarY, progressBarX + progressBarWidth, progressBarY + progressBarHeight, 0xFF333333);
        
        // 绘制进度条填充
        int filledWidth = (int) (progressBarWidth * progress);
        if (filledWidth > 0) {
            // 渐变效果
            for (int i = 0; i < filledWidth; i++) {
                float ratio = (float) i / filledWidth;
                int r = (int) (0 + ratio * 0);
                int g = (int) (255 - ratio * 100);
                int b = (int) (0 + ratio * 0);
                int color = (0xFF << 24) | (r << 16) | (g << 8) | b;
                guiGraphics.fill(progressBarX + i, progressBarY, progressBarX + i + 1, progressBarY + progressBarHeight, color);
            }
        }
        
        // 绘制进度条边框
        guiGraphics.fill(progressBarX, progressBarY, progressBarX + 1, progressBarY + progressBarHeight, 0xFFAAAAAA);
        guiGraphics.fill(progressBarX + progressBarWidth, progressBarY, progressBarX + progressBarWidth + 1, progressBarY + progressBarHeight, 0xFF555555);
        guiGraphics.fill(progressBarX, progressBarY, progressBarX + progressBarWidth, progressBarY + 1, 0xFFAAAAAA);
        guiGraphics.fill(progressBarX, progressBarY + progressBarHeight, progressBarX + progressBarWidth, progressBarY + progressBarHeight + 1, 0xFF555555);
        
        // 绘制文字
        guiGraphics.drawString(this.font, Component.translatable("casting.dcc.casting"), centerX - this.font.width(Component.translatable("casting.dcc.casting")) / 2, centerY - 40, 0xFFFFFF, false);
        // 移除百分比显示
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 允许玩家在施法过程中点击右键取消施法
        if (button == 1) { // 右键
            return true;
        }
        return false; // 不处理其他鼠标点击，让游戏处理
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // 不处理鼠标滚轮，让游戏处理
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 不处理键盘按键，让游戏处理
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // 不处理鼠标拖动，让游戏处理
        return false;
    }
    
    @Override
    public void tick() {
        // 允许游戏继续运行，这样玩家可以移动和转动视角
        super.tick();
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // 允许鼠标事件传递给游戏
        return false;
    }
    
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        // 允许鼠标移动事件传递给游戏，这样玩家可以转动视角
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // 不处理键盘释放，让游戏处理
        return false;
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // 不处理字符输入，让游戏处理
        return false;
    }
    
    @Override
    public boolean isFocused() {
        // 不获取焦点，让游戏获取焦点
        return false;
    }
}
