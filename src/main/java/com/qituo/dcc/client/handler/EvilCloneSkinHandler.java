package com.qituo.dcc.client.handler;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.qituo.dcc.util.ReflectionCache;
import com.qituo.dcc.util.ExceptionHandler;

/**
 * 恶人格皮肤处理器
 * 为恶人格设置3D皮肤层
 * 
 * 注意：3D皮肤层模组（3D Skin Layers）会自动处理所有AbstractClientPlayer实体。
 * 这个处理器的作用是确保恶人格的皮肤能够被3D皮肤层模组正确获取。
 */
@Mod.EventBusSubscriber(modid = "dcc", value = Dist.CLIENT)
public class EvilCloneSkinHandler {

    // 缓存已处理的恶人格
    private static final Map<UUID, ResourceLocation> processedEvilClones = new HashMap<>();

    /**
     * 在渲染玩家时触发
     */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        // 获取玩家实体
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }

        // 检查是否是恶人格
        if (!player.getName().getString().contains("的恶人格")) {
            return;
        }

        // 获取当前皮肤位置
        ResourceLocation currentSkin = player.getSkinTextureLocation();
        if (currentSkin == null) {
            return;
        }

        // 检查是否已经处理过这个皮肤
        ResourceLocation lastSkin = processedEvilClones.get(player.getUUID());
        if (lastSkin != null && lastSkin.equals(currentSkin)) {
            // 已经处理过这个皮肤，跳过
            return;
        }

        // 尝试设置3D皮肤层
        if (setup3DLayers(player, currentSkin)) {
            processedEvilClones.put(player.getUUID(), currentSkin);
        }
    }

    /**
     * 为恶人格设置3D皮肤层
     * @return 是否成功设置
     */
    private static boolean setup3DLayers(AbstractClientPlayer player, ResourceLocation skinLocation) {
        try {
            // 检查3D皮肤层模组是否存在
            Class<?> playerSettingsClass = ReflectionCache.getClass("dev.tr7zw.skinlayers.accessor.PlayerSettings");
            Class<?> skinLayersAPIClass = ReflectionCache.getClass("dev.tr7zw.skinlayers.api.SkinLayersAPI");
            Class<?> meshHelperClass = ReflectionCache.getClass("dev.tr7zw.skinlayers.api.MeshHelper");
            Class<?> meshClass = ReflectionCache.getClass("dev.tr7zw.skinlayers.api.Mesh");

            // 检查玩家是否实现了PlayerSettings
            if (!playerSettingsClass.isInstance(player)) {
                System.out.println("[十二符咒] 恶人格未实现PlayerSettings接口: " + player.getName().getString());
                return false;
            }

            Object settings = playerSettingsClass.cast(player);

            // 获取当前设置的皮肤
            java.lang.reflect.Method getCurrentSkinMethod = ReflectionCache.getMethod(playerSettingsClass, "getCurrentSkin");
            ResourceLocation currentSkinSetting = (ResourceLocation) ReflectionCache.invokeMethod(settings, getCurrentSkinMethod);

            // 如果皮肤已经设置且相同，跳过
            if (currentSkinSetting != null && currentSkinSetting.equals(skinLocation)) {
                return true;
            }

            // 获取皮肤图片
            NativeImage skinImage = getSkinImage(skinLocation);
            if (skinImage == null) {
                // 皮肤图片还未加载，稍后重试
                return false;
            }

            // 检查皮肤尺寸
            if (skinImage.getWidth() != 64 || skinImage.getHeight() != 64) {
                System.out.println("[十二符咒] 恶人格皮肤图片尺寸无效: " + skinImage.getWidth() + "x" + skinImage.getHeight());
                // 设置当前皮肤并清空网格
                java.lang.reflect.Method setCurrentSkinMethod = ReflectionCache.getMethod(playerSettingsClass, "setCurrentSkin", ResourceLocation.class);
                java.lang.reflect.Method setThinArmsMethod = ReflectionCache.getMethod(playerSettingsClass, "setThinArms", boolean.class);
                java.lang.reflect.Method clearMeshesMethod = ReflectionCache.getMethod(playerSettingsClass, "clearMeshes");

                ReflectionCache.invokeMethod(settings, setCurrentSkinMethod, skinLocation);
                ReflectionCache.invokeMethod(settings, setThinArmsMethod, false);
                ReflectionCache.invokeMethod(settings, clearMeshesMethod);
                return true;
            }

            System.out.println("[十二符咒] 正在为恶人格设置3D皮肤层: " + player.getName().getString() + ", 皮肤: " + skinLocation);

            // 获取MeshHelper
            java.lang.reflect.Method getMeshHelperMethod = ReflectionCache.getMethod(skinLayersAPIClass, "getMeshHelper");
            Object meshHelper = ReflectionCache.invokeMethod(null, getMeshHelperMethod);

            // 创建3D网格 - 使用与3D Skin Layers模组相同的参数
            java.lang.reflect.Method create3DMeshMethod = ReflectionCache.getMethod(meshHelperClass, "create3DMesh",
                    NativeImage.class, int.class, int.class, int.class, int.class, int.class, boolean.class, float.class);

            // 设置左腿网格
            Object leftLegMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 4, 12, 4, 0, 48, true, 0f);
            java.lang.reflect.Method setLeftLegMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setLeftLegMesh", meshClass);
            ReflectionCache.invokeMethod(settings, setLeftLegMeshMethod, leftLegMesh);

            // 设置右腿网格
            Object rightLegMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 4, 12, 4, 0, 32, true, 0f);
            java.lang.reflect.Method setRightLegMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setRightLegMesh", meshClass);
            ReflectionCache.invokeMethod(settings, setRightLegMeshMethod, rightLegMesh);

            // 设置手臂网格（使用普通手臂，宽度4）
            Object leftArmMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 4, 12, 4, 48, 48, true, -2f);
            Object rightArmMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 4, 12, 4, 40, 32, true, -2f);

            java.lang.reflect.Method setLeftArmMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setLeftArmMesh", meshClass);
            java.lang.reflect.Method setRightArmMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setRightArmMesh", meshClass);
            ReflectionCache.invokeMethod(settings, setLeftArmMeshMethod, leftArmMesh);
            ReflectionCache.invokeMethod(settings, setRightArmMeshMethod, rightArmMesh);

            // 设置躯干网格 (使用0f作为rotationOffset，与SkinUtil一致)
            Object torsoMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 8, 12, 4, 16, 32, true, 0f);
            java.lang.reflect.Method setTorsoMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setTorsoMesh", meshClass);
            ReflectionCache.invokeMethod(settings, setTorsoMeshMethod, torsoMesh);

            // 设置头部网格
            Object headMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 8, 8, 8, 32, 0, false, 0.6f);
            java.lang.reflect.Method setHeadMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setHeadMesh", meshClass);
            ReflectionCache.invokeMethod(settings, setHeadMeshMethod, headMesh);

            // 设置当前皮肤和手臂类型
            java.lang.reflect.Method setCurrentSkinMethod = ReflectionCache.getMethod(playerSettingsClass, "setCurrentSkin", ResourceLocation.class);
            java.lang.reflect.Method setThinArmsMethod = ReflectionCache.getMethod(playerSettingsClass, "setThinArms", boolean.class);
            ReflectionCache.invokeMethod(settings, setCurrentSkinMethod, skinLocation);
            ReflectionCache.invokeMethod(settings, setThinArmsMethod, false);

            System.out.println("[十二符咒] 恶人格3D皮肤层设置成功: " + player.getName().getString());
            return true;

        } catch (Exception e) {
            // 3D皮肤层模组未安装或其他错误，忽略
            System.out.println("[十二符咒] 3D皮肤层模组未安装或发生错误: " + e.getMessage());
            return true; // 返回true以避免重复尝试
        }
    }

    /**
     * 获取皮肤图片 - 使用与3D Skin Layers模组相同的方式
     */
    private static NativeImage getSkinImage(ResourceLocation skinLocation) {
        try {
            // 首先尝试从资源管理器获取（用于本地皮肤）
            Optional<net.minecraft.server.packs.resources.Resource> optionalRes = 
                Minecraft.getInstance().getResourceManager().getResource(skinLocation);
            if (optionalRes.isPresent()) {
                try (var inputStream = optionalRes.get().open()) {
                    NativeImage image = NativeImage.read(inputStream);
                    System.out.println("[十二符咒] 从资源管理器获取到皮肤图片: " + skinLocation + ", 尺寸: " + image.getWidth() + "x" + image.getHeight());
                    return image;
                }
            }

            // 从纹理管理器获取纹理
            AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(skinLocation);
            if (texture == null) {
                System.out.println("[十二符咒] 皮肤纹理还未加载: " + skinLocation);
                return null;
            }

            System.out.println("[十二符咒] 找到皮肤纹理: " + skinLocation + ", 类型: " + texture.getClass().getName());

            // 如果是DynamicTexture，直接获取pixels
            if (texture instanceof DynamicTexture dynamicTexture) {
                NativeImage image = dynamicTexture.getPixels();
                if (image != null) {
                    System.out.println("[十二符咒] 从DynamicTexture获取到皮肤图片: " + skinLocation + ", 尺寸: " + image.getWidth() + "x" + image.getHeight());
                }
                return image;
            }

            // 如果是HttpTexture，使用反射获取file字段，然后读取文件
            if (texture instanceof HttpTexture httpTexture) {
                NativeImage image = getImageFromHttpTexture(httpTexture);
                if (image != null) {
                    System.out.println("[十二符咒] 从HttpTexture获取到皮肤图片: " + skinLocation + ", 尺寸: " + image.getWidth() + "x" + image.getHeight());
                } else {
                    System.out.println("[十二符咒] HttpTexture图片还未下载完成: " + skinLocation);
                }
                return image;
            }

        } catch (Exception e) {
            System.err.println("[十二符咒] 获取皮肤图片时出错: " + skinLocation);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从HttpTexture获取图片 - 模仿3D Skin Layers模组的方式
     * 在1.20.1中，HttpTexture使用file字段来缓存下载的皮肤图片
     */
    private static NativeImage getImageFromHttpTexture(HttpTexture httpTexture) {
        try {
            // 尝试获取file字段 - 这是1.20.1中HttpTexture用来缓存皮肤文件的字段
            Field fileField = ReflectionCache.getField(HttpTexture.class, "file");
            File file = (File) ReflectionCache.getFieldValue(httpTexture, fileField);

            if (file != null && file.isFile()) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    return NativeImage.read(fileInputStream);
                }
            }

            // 如果file为null，说明皮肤还在下载中，返回null等待下次重试
            return null;

        } catch (Exception e) {
            System.err.println("[十二符咒] 从HttpTexture获取图片时发生错误: " + e.getMessage());
            ExceptionHandler.handleReflectionException("从HttpTexture获取图片", e);
            return null;
        }
    }
}
