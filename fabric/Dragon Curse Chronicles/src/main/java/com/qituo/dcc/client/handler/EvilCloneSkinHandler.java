package com.qituo.dcc.client.handler;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.qituo.dcc.util.ReflectionCache;
import com.qituo.dcc.util.ExceptionHandler;

@Environment(EnvType.CLIENT)
public class EvilCloneSkinHandler {

    private static final Map<UUID, Identifier> processedEvilClones = new HashMap<>();

    public static void init() {
    }

    public static void onRenderPlayer(AbstractClientPlayerEntity player) {
        if (!player.getName().getString().contains("的恶人格")) {
            return;
        }

        Identifier currentSkin = player.getSkinTexture();
        if (currentSkin == null) {
            return;
        }

        Identifier lastSkin = processedEvilClones.get(player.getUuid());
        if (lastSkin != null && lastSkin.equals(currentSkin)) {
            return;
        }

        if (setup3DLayers(player, currentSkin)) {
            processedEvilClones.put(player.getUuid(), currentSkin);
        }
    }

    private static boolean setup3DLayers(AbstractClientPlayerEntity player, Identifier skinLocation) {
        try {
            Class<?> playerSettingsClass = ReflectionCache.getClass("dev.tr7zw.skinlayers.accessor.PlayerSettings");
            Class<?> skinLayersAPIClass = ReflectionCache.getClass("dev.tr7zw.skinlayers.api.SkinLayersAPI");
            Class<?> meshHelperClass = ReflectionCache.getClass("dev.tr7zw.skinlayers.api.MeshHelper");
            Class<?> meshClass = ReflectionCache.getClass("dev.tr7zw.skinlayers.api.Mesh");

            if (!playerSettingsClass.isInstance(player)) {
                System.out.println("[十二符咒] 恶人格未实现PlayerSettings接口: " + player.getName().getString());
                return false;
            }

            Object settings = playerSettingsClass.cast(player);

            java.lang.reflect.Method getCurrentSkinMethod = ReflectionCache.getMethod(playerSettingsClass, "getCurrentSkin");
            Identifier currentSkinSetting = (Identifier) ReflectionCache.invokeMethod(settings, getCurrentSkinMethod);

            if (currentSkinSetting != null && currentSkinSetting.equals(skinLocation)) {
                return true;
            }

            NativeImage skinImage = getSkinImage(skinLocation);
            if (skinImage == null) {
                return false;
            }

            if (skinImage.getWidth() != 64 || skinImage.getHeight() != 64) {
                System.out.println("[十二符咒] 恶人格皮肤图片尺寸无效: " + skinImage.getWidth() + "x" + skinImage.getHeight());
                java.lang.reflect.Method setCurrentSkinMethod = ReflectionCache.getMethod(playerSettingsClass, "setCurrentSkin", Identifier.class);
                java.lang.reflect.Method setThinArmsMethod = ReflectionCache.getMethod(playerSettingsClass, "setThinArms", boolean.class);
                java.lang.reflect.Method clearMeshesMethod = ReflectionCache.getMethod(playerSettingsClass, "clearMeshes");

                ReflectionCache.invokeMethod(settings, setCurrentSkinMethod, skinLocation);
                ReflectionCache.invokeMethod(settings, setThinArmsMethod, false);
                ReflectionCache.invokeMethod(settings, clearMeshesMethod);
                return true;
            }

            System.out.println("[十二符咒] 正在为恶人格设置3D皮肤层: " + player.getName().getString() + ", 皮肤: " + skinLocation);

            java.lang.reflect.Method getMeshHelperMethod = ReflectionCache.getMethod(skinLayersAPIClass, "getMeshHelper");
            Object meshHelper = ReflectionCache.invokeMethod(null, getMeshHelperMethod);

            java.lang.reflect.Method create3DMeshMethod = ReflectionCache.getMethod(meshHelperClass, "create3DMesh",
                    NativeImage.class, int.class, int.class, int.class, int.class, int.class, boolean.class, float.class);

            Object leftLegMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 4, 12, 4, 0, 48, true, 0f);
            java.lang.reflect.Method setLeftLegMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setLeftLegMesh", meshClass);
            ReflectionCache.invokeMethod(settings, setLeftLegMeshMethod, leftLegMesh);

            Object rightLegMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 4, 12, 4, 0, 32, true, 0f);
            java.lang.reflect.Method setRightLegMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setRightLegMesh", meshClass);
            ReflectionCache.invokeMethod(settings, setRightLegMeshMethod, rightLegMesh);

            Object leftArmMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 4, 12, 4, 48, 48, true, -2f);
            Object rightArmMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 4, 12, 4, 40, 32, true, -2f);

            java.lang.reflect.Method setLeftArmMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setLeftArmMesh", meshClass);
            java.lang.reflect.Method setRightArmMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setRightArmMesh", meshClass);
            ReflectionCache.invokeMethod(settings, setLeftArmMeshMethod, leftArmMesh);
            ReflectionCache.invokeMethod(settings, setRightArmMeshMethod, rightArmMesh);

            Object torsoMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 8, 12, 4, 16, 32, true, 0f);
            java.lang.reflect.Method setTorsoMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setTorsoMesh", meshClass);
            ReflectionCache.invokeMethod(settings, setTorsoMeshMethod, torsoMesh);

            Object headMesh = ReflectionCache.invokeMethod(meshHelper, create3DMeshMethod, skinImage, 8, 8, 8, 32, 0, false, 0.6f);
            java.lang.reflect.Method setHeadMeshMethod = ReflectionCache.getMethod(playerSettingsClass, "setHeadMesh", meshClass);
            ReflectionCache.invokeMethod(settings, setHeadMeshMethod, headMesh);

            java.lang.reflect.Method setCurrentSkinMethod = ReflectionCache.getMethod(playerSettingsClass, "setCurrentSkin", Identifier.class);
            java.lang.reflect.Method setThinArmsMethod = ReflectionCache.getMethod(playerSettingsClass, "setThinArms", boolean.class);
            ReflectionCache.invokeMethod(settings, setCurrentSkinMethod, skinLocation);
            ReflectionCache.invokeMethod(settings, setThinArmsMethod, false);

            System.out.println("[十二符咒] 恶人格3D皮肤层设置成功: " + player.getName().getString());
            return true;

        } catch (Exception e) {
            System.out.println("[十二符咒] 3D皮肤层模组未安装或发生错误: " + e.getMessage());
            return true;
        }
    }

    private static NativeImage getSkinImage(Identifier skinLocation) {
        try {
            Optional<net.minecraft.resource.Resource> optionalRes =
                MinecraftClient.getInstance().getResourceManager().getResource(skinLocation);
            if (optionalRes.isPresent()) {
                try (var inputStream = optionalRes.get().getInputStream()) {
                    NativeImage image = NativeImage.read(inputStream);
                    System.out.println("[十二符咒] 从资源管理器获取到皮肤图片: " + skinLocation + ", 尺寸: " + image.getWidth() + "x" + image.getHeight());
                    return image;
                }
            }

            AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(skinLocation);
            if (texture == null) {
                System.out.println("[十二符咒] 皮肤纹理还未加载: " + skinLocation);
                return null;
            }

            System.out.println("[十二符咒] 找到皮肤纹理: " + skinLocation + ", 类型: " + texture.getClass().getName());

            if (texture instanceof NativeImageBackedTexture nativeImageBackedTexture) {
                NativeImage image = nativeImageBackedTexture.getImage();
                if (image != null) {
                    System.out.println("[十二符咒] 从NativeImageBackedTexture获取到皮肤图片: " + skinLocation + ", 尺寸: " + image.getWidth() + "x" + image.getHeight());
                }
                return image;
            }

            if (texture instanceof PlayerSkinTexture playerSkinTexture) {
                NativeImage image = getImageFromPlayerSkinTexture(playerSkinTexture);
                if (image != null) {
                    System.out.println("[十二符咒] 从PlayerSkinTexture获取到皮肤图片: " + skinLocation + ", 尺寸: " + image.getWidth() + "x" + image.getHeight());
                } else {
                    System.out.println("[十二符咒] PlayerSkinTexture图片还未下载完成: " + skinLocation);
                }
                return image;
            }

        } catch (Exception e) {
            System.err.println("[十二符咒] 获取皮肤图片时出错: " + skinLocation);
            e.printStackTrace();
        }
        return null;
    }

    private static NativeImage getImageFromPlayerSkinTexture(PlayerSkinTexture playerSkinTexture) {
        try {
            Field cacheFileField = ReflectionCache.getField(PlayerSkinTexture.class, "cacheFile");
            File file = (File) ReflectionCache.getFieldValue(playerSkinTexture, cacheFileField);

            if (file != null && file.isFile()) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    return NativeImage.read(fileInputStream);
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("[十二符咒] 从PlayerSkinTexture获取图片时发生错误: " + e.getMessage());
            ExceptionHandler.handleReflectionException("从PlayerSkinTexture获取图片", e);
            return null;
        }
    }
}
