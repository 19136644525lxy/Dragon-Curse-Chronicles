package com.qituo.dcc.client.renderer;

import com.qituo.dcc.entity.DragonFireball;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class DragonFireballRenderer extends EntityRenderer<DragonFireball> {
    private static final Identifier TEXTURE = new Identifier("dcc", "textures/entity/dragon_fireball.png");

    public DragonFireballRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(DragonFireball entity) {
        return TEXTURE;
    }
}
