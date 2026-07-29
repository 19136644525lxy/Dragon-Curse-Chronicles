package com.qituo.dcc.talismans;

import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import java.util.Random;

public class MouseTalisman extends TalismanBase {
    public MouseTalisman(Item.Settings settings) {
        super(settings);
    }
    
    @Override
    protected void useTalisman(ServerWorld world, PlayerEntity player, net.minecraft.util.Hand hand) {
        // 获取玩家指向的方块
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = getLookVec(player);
        Vec3d endPos = eyePos.add(lookVec.x * 5, lookVec.y * 5, lookVec.z * 5);
        
        RaycastContext context = new RaycastContext(
            eyePos,
            endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        );
        BlockHitResult hitResult = world.raycast(context);
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(pos);
            
            // 检查方块类型并转化为相应生物
            Entity newEntity = null;
            
            if (blockState.isOf(Blocks.IRON_BLOCK)) {
                // 铁块 -> 铁傀儡
                newEntity = new IronGolemEntity(EntityType.IRON_GOLEM, world);
            } else if (isWoolBlock(blockState)) {
                // 羊毛 -> 羊
                newEntity = new SheepEntity(EntityType.SHEEP, world);
            } else if (isDirtBlock(blockState)) {
                // 泥土 -> 僵尸或骷髅（随机）
                Random random = new Random();
                if (random.nextBoolean()) {
                    newEntity = new ZombieEntity(EntityType.ZOMBIE, world);
                } else {
                    newEntity = new SkeletonEntity(EntityType.SKELETON, world);
                }
            } else if (blockState.isOf(Blocks.CARVED_PUMPKIN) || blockState.isOf(Blocks.JACK_O_LANTERN)) {
                // 雕刻南瓜/南瓜灯 -> 雪傀儡
                newEntity = new SnowGolemEntity(EntityType.SNOW_GOLEM, world);
            } else if (blockState.isOf(Blocks.TNT)) {
                // TNT -> 苦力怕
                newEntity = new CreeperEntity(EntityType.CREEPER, world);
            }
            
            if (newEntity != null) {
                // 移除原方块
                world.removeBlock(pos, false);
                
                // 设置新实体位置
                newEntity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                
                // 增强实体属性
                if (newEntity instanceof LivingEntity livingEntity) {
                    // 设置生命值为50
                    livingEntity.setHealth(50.0F);
                    
                    // 亡灵生物不会燃烧
                    if (livingEntity instanceof ZombieEntity zombie) {
                        // yarn 中无 setAttackDamage 方法，通过属性修改符增强攻击力
                        zombie.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                            .setBaseValue(5.0);
                    } else if (livingEntity instanceof SkeletonEntity skeleton) {
                        // 骷髅攻击力增强
                    } else if (livingEntity instanceof CreeperEntity creeper) {
                        // 苦力怕
                    } else if (livingEntity instanceof IronGolemEntity) {
                        // 铁傀儡攻击力增强
                    }
                }
                
                // 添加新实体到世界
                world.spawnEntity(newEntity);
            }
        }
    }
    
    /**
     * 获取玩家视线向量
     */
    private Vec3d getLookVec(PlayerEntity player) {
        float yawRad = player.getYaw() * ((float)Math.PI / 180.0f);
        float pitchRad = player.getPitch() * ((float)Math.PI / 180.0f);
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);
        return new Vec3d(x, y, z);
    }
    
    private boolean isWoolBlock(BlockState state) {
        return state.isOf(Blocks.WHITE_WOOL) || state.isOf(Blocks.ORANGE_WOOL) ||
               state.isOf(Blocks.MAGENTA_WOOL) || state.isOf(Blocks.LIGHT_BLUE_WOOL) ||
               state.isOf(Blocks.YELLOW_WOOL) || state.isOf(Blocks.LIME_WOOL) ||
               state.isOf(Blocks.PINK_WOOL) || state.isOf(Blocks.GRAY_WOOL) ||
               state.isOf(Blocks.LIGHT_GRAY_WOOL) || state.isOf(Blocks.CYAN_WOOL) ||
               state.isOf(Blocks.PURPLE_WOOL) || state.isOf(Blocks.BLUE_WOOL) ||
               state.isOf(Blocks.BROWN_WOOL) || state.isOf(Blocks.GREEN_WOOL) ||
               state.isOf(Blocks.RED_WOOL) || state.isOf(Blocks.BLACK_WOOL);
    }
    
    private boolean isDirtBlock(BlockState state) {
        return state.isOf(Blocks.DIRT) || state.isOf(Blocks.GRASS_BLOCK) ||
               state.isOf(Blocks.COARSE_DIRT) || state.isOf(Blocks.PODZOL);
    }
}
