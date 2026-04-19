package com.qituo.dcc.client.renderer;

import com.qituo.dcc.entity.DragonFireball;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DragonFireballRenderer extends EntityRenderer<DragonFireball> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/fireball.png");

    public DragonFireballRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonFireball entity) {
        return TEXTURE;
    }
}