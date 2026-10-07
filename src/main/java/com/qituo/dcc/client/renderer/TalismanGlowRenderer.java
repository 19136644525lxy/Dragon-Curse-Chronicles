package com.qituo.dcc.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.qituo.dcc.DragonCurseChronicles;
import com.qituo.dcc.talismans.TalismanBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * 符咒生肖边缘光效渲染器
 *
 * 核心设计：
 * 1. 完全接管 BEWLR 渲染，手动绘制 BakedQuad，避免 renderStatic 递归
 * 2. 渲染发光层时，将 quad 的 atlas UV 重映射到 0~1，匹配独立发光纹理
 * 3. 使用 RenderType.eyes() 实现加法混合发光
 * 4. 4秒周期呼吸式脉动，基础强度 0.30（淡），施法 0.50
 */
@OnlyIn(Dist.CLIENT)
public class TalismanGlowRenderer extends BlockEntityWithoutLevelRenderer {

    // 呼吸周期：4秒一次
    private static final long BREATH_CYCLE_MS = 4000L;

    // 调试日志（测试期间开启）
    private static final boolean DEBUG_LOG = true;
    private static long lastLogTime = 0L;
    private static final java.util.concurrent.atomic.AtomicInteger renderCount =
            new java.util.concurrent.atomic.AtomicInteger(0);

    public TalismanGlowRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
              Minecraft.getInstance().getEntityModels());
        System.out.println("[DCC Glow] 渲染器实例创建成功");
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 每2秒打印一次，确认 renderByItem 确实被调用
        renderCount.incrementAndGet();
        if (DEBUG_LOG && System.currentTimeMillis() - lastLogTime > 2000L) {
            lastLogTime = System.currentTimeMillis();
            System.out.println("[DCC Glow] renderByItem 调用: " + stack.getItem().getClass().getSimpleName()
                    + ", ctx=" + ctx + ", light=" + packedLight
                    + ", 累计=" + renderCount.get());
        }

        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);

        // ============= 第一步：渲染基础物品模型 =============
        poseStack.pushPose();
        // 1.20 API: apply(boolean leftHanded, PoseStack)
        model.getTransforms().getTransform(ctx).apply(false, poseStack);
        PoseStack.Pose basePose = poseStack.last();

        RenderType baseType = ItemBlockRenderTypes.getRenderType(stack, true);
        // 简化：统一用直接 buffer 获取
        VertexConsumer baseVc = bufferSource.getBuffer(baseType);
        renderQuadsPutBulk(basePose, baseVc, model, packedLight, packedOverlay);
        poseStack.popPose();

        // ============= 第二步：叠加发光层（仅符咒） =============
        if (stack.getItem() instanceof TalismanBase talisman) {
            poseStack.pushPose();
            model.getTransforms().getTransform(ctx).apply(false, poseStack);
            renderGlowLayer(talisman, stack, model, poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    /**
     * 用 putBulkData 正常渲染 BakedQuad
     * Forge 1.20 签名：putBulkData(Pose, BakedQuad, r, g, b, a, packedLight, packedOverlay, shaded)
     */
    private void renderQuadsPutBulk(PoseStack.Pose pose, VertexConsumer vc, BakedModel model,
                                     int packedLight, int packedOverlay) {
        var random = net.minecraft.util.RandomSource.create();

        for (var direction : net.minecraft.core.Direction.values()) {
            random.setSeed(42L);
            for (BakedQuad quad : model.getQuads(null, direction, random)) {
                // IForgeVertexConsumer：r,g,b,a + packedLight + packedOverlay + shaded
                vc.putBulkData(pose, quad,
                        1f, 1f, 1f, 1f,
                        packedLight, packedOverlay, true);
            }
        }
        random.setSeed(42L);
        for (BakedQuad quad : model.getQuads(null, null, random)) {
            vc.putBulkData(pose, quad,
                    1f, 1f, 1f, 1f,
                    packedLight, packedOverlay, true);
        }
    }

    /**
     * 渲染生肖发光层
     * 取原 quad 的 XYZ 空间坐标（与原图对齐），UV 重映射到 0~1 用于独立发光纹理
     */
    private void renderGlowLayer(TalismanBase talisman, ItemStack stack, BakedModel model,
                                  PoseStack poseStack, MultiBufferSource bufferSource,
                                  int packedLight, int packedOverlay) {
        float[] color = talisman.getZodiacColor();
        String texturePath = talisman.getGlowTexturePath();
        ResourceLocation glowTexture = DragonCurseChronicles.id(texturePath);

        // ===== 呼吸式脉动 =====
        long timeMs = System.currentTimeMillis() % BREATH_CYCLE_MS;
        float t = (float) timeMs / BREATH_CYCLE_MS;
        float breathPhase = t * Mth.TWO_PI;
        float easedCos = (1.0f + Mth.cos(breathPhase)) * 0.5f;
        float pulse = 0.65f + 0.35f * easedCos; // 0.65 ~ 1.0 平滑脉动

        // ===== 施法判断 =====
        boolean isCasting = mcPlayerUsing(stack);
        float baseIntensity = isCasting ? 0.50f : 0.30f;
        float r = color[0] * pulse * baseIntensity;
        float g = color[1] * pulse * baseIntensity;
        float b = color[2] * pulse * baseIntensity;
        float a = 1.0f;

        if (DEBUG_LOG && System.currentTimeMillis() - lastLogTime < 100L) {
            System.out.println("[DCC Glow] 发光层: zodiac=" + talisman.getZodiacName()
                    + ", glowTex=" + glowTexture
                    + ", rgb=(" + r + "," + g + "," + b + ")"
                    + ", pulse=" + pulse);
        }

        // RenderType.eyes()：加法混合，不受光照影响
        VertexConsumer glowVc = bufferSource.getBuffer(RenderType.eyes(glowTexture));

        // 向相机偏移 0.01 避免 Z-fighting
        poseStack.translate(0, 0, 0.01f);
        PoseStack.Pose pose = poseStack.last();

        var random = net.minecraft.util.RandomSource.create();
        int totalQuads = 0;

        for (var direction : net.minecraft.core.Direction.values()) {
            random.setSeed(42L);
            totalQuads += remapAndRender(pose, glowVc,
                    model.getQuads(null, direction, random), r, g, b, a, packedLight);
        }
        random.setSeed(42L);
        totalQuads += remapAndRender(pose, glowVc,
                model.getQuads(null, null, random), r, g, b, a, packedLight);

        if (DEBUG_LOG && totalQuads == 0) {
            System.out.println("[DCC Glow] 警告: 未找到任何 BakedQuad！");
        } else if (DEBUG_LOG && System.currentTimeMillis() - lastLogTime < 100L) {
            System.out.println("[DCC Glow] 发光层渲染四边形数: " + totalQuads);
        }
    }

    /**
     * 将 BakedQuad 的 atlas UV 重映射到 0~1 完整纹理范围后渲染
     *
     * 原理：
     *   原 quad.uv 是 atlas 中的子区域（如 0.12 ~ 0.18）
     *   发光纹理独立加载，无 atlas 概念，所以必须对每个 quad 的 UV 做线性归一化
     */
    private int remapAndRender(PoseStack.Pose pose, VertexConsumer vc,
                                List<BakedQuad> quads,
                                float r, float g, float b, float a,
                                int packedLight) {
        if (quads.isEmpty()) return 0;
        int count = 0;

        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices();
            int stride = 8; // x,y,z,color,u,v,light,normal  packed in 8 ints
            int vertexCount = vertices.length / stride;

            // 扫描 UV 边界
            float minU = Float.MAX_VALUE, maxU = -Float.MAX_VALUE;
            float minV = Float.MAX_VALUE, maxV = -Float.MAX_VALUE;

            for (int i = 0; i < vertexCount; i++) {
                int offset = i * stride;
                float u = Float.intBitsToFloat(vertices[offset + 4]);
                float v = Float.intBitsToFloat(vertices[offset + 5]);
                if (u < minU) minU = u;
                if (u > maxU) maxU = u;
                if (v < minV) minV = v;
                if (v > maxV) maxV = v;
            }

            float uRange = maxU - minU;
            float vRange = maxV - minV;
            if (uRange < 1e-5f) uRange = 1e-5f;
            if (vRange < 1e-5f) vRange = 1e-5f;

            // 逐顶点重映射 UV 并写入
            for (int i = 0; i < vertexCount; i++) {
                int offset = i * stride;
                float x = Float.intBitsToFloat(vertices[offset]);
                float y = Float.intBitsToFloat(vertices[offset + 1]);
                float z = Float.intBitsToFloat(vertices[offset + 2]);
                float u = Float.intBitsToFloat(vertices[offset + 4]);
                float v = Float.intBitsToFloat(vertices[offset + 5]);

                // 线性映射 → 0.0 ~ 1.0
                float remappedU = (u - minU) / uRange;
                float remappedV = (v - minV) / vRange;
                remappedU = Mth.clamp(remappedU, 0.0f, 1.0f);
                remappedV = Mth.clamp(remappedV, 0.0f, 1.0f);

                vc.vertex(pose.pose(), x, y, z)
                        .color(r, g, b, a)
                        .uv(remappedU, remappedV)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(packedLight)
                        .normal(pose.normal(),
                                quad.getDirection().getStepX(),
                                quad.getDirection().getStepY(),
                                quad.getDirection().getStepZ())
                        .endVertex();
            }
            count++;
        }
        return count;
    }

    /** 判断玩家是否正在使用该物品（施法中） */
    private static boolean mcPlayerUsing(ItemStack stack) {
        var player = Minecraft.getInstance().player;
        return player != null && player.isUsingItem() && player.getUseItem() == stack;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }
}
