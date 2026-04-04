package com.qituo.dcc.talismans;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class MouseTalisman extends TalismanBase {
    public MouseTalisman(Properties properties) {
        super(properties);
    }
    
    @Override
    protected void useTalisman(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        // 获取玩家指向的方块
        net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition(1.0F);
        net.minecraft.world.phys.Vec3 lookVec = player.getViewVector(1.0F);
        net.minecraft.world.phys.Vec3 endPos = eyePos.add(lookVec.x * 5, lookVec.y * 5, lookVec.z * 5);
        
        net.minecraft.world.phys.BlockHitResult hitResult = level.clip(
            new net.minecraft.world.level.ClipContext(
                eyePos,
                endPos,
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
            )
        );
        
        if (hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.core.BlockPos pos = hitResult.getBlockPos();
            net.minecraft.world.level.block.state.BlockState blockState = level.getBlockState(pos);
            
            // 检查方块类型并转化为相应生物
            net.minecraft.world.entity.Entity newEntity = null;
            
            if (blockState.is(net.minecraft.world.level.block.Blocks.IRON_BLOCK)) {
                // 铁块 -> 铁傀儡
                newEntity = new net.minecraft.world.entity.animal.IronGolem(net.minecraft.world.entity.EntityType.IRON_GOLEM, level);
            } else if (blockState.is(net.minecraft.world.level.block.Blocks.WHITE_WOOL) || 
                       blockState.is(net.minecraft.world.level.block.Blocks.ORANGE_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.MAGENTA_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.LIGHT_BLUE_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.YELLOW_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.LIME_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.PINK_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.GRAY_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.LIGHT_GRAY_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.CYAN_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.PURPLE_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.BLUE_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.BROWN_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.GREEN_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.RED_WOOL) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.BLACK_WOOL)) {
                // 羊毛 -> 羊
                newEntity = new net.minecraft.world.entity.animal.Sheep(net.minecraft.world.entity.EntityType.SHEEP, level);
            } else if (blockState.is(net.minecraft.world.level.block.Blocks.DIRT) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.PODZOL)) {
                // 泥土 -> 僵尸或骷髅（随机）
                java.util.Random random = new java.util.Random();
                if (random.nextBoolean()) {
                    newEntity = new net.minecraft.world.entity.monster.Zombie(net.minecraft.world.entity.EntityType.ZOMBIE, level);
                } else {
                    newEntity = new net.minecraft.world.entity.monster.Skeleton(net.minecraft.world.entity.EntityType.SKELETON, level);
                }
            } else if (blockState.is(net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN) ||
                       blockState.is(net.minecraft.world.level.block.Blocks.JACK_O_LANTERN)) {
                // 雕刻南瓜/南瓜灯 -> 雪傀儡
                newEntity = new net.minecraft.world.entity.animal.SnowGolem(net.minecraft.world.entity.EntityType.SNOW_GOLEM, level);
            } else if (blockState.is(net.minecraft.world.level.block.Blocks.TNT)) {
                // TNT -> 苦力怕
                newEntity = new net.minecraft.world.entity.monster.Creeper(net.minecraft.world.entity.EntityType.CREEPER, level);
            }
            
            if (newEntity != null) {
                // 移除原方块
                level.removeBlock(pos, false);
                
                // 设置新实体位置
                newEntity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                
                // 增强实体属性
                if (newEntity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                    // 设置生命值为50
                    livingEntity.setHealth(50.0F);
                    
                    // 对玩家友好
                    // livingEntity.setPersistenceRequired(); // 方法不存在
                    
                    // 亡灵生物不会燃烧
                    if (livingEntity instanceof net.minecraft.world.entity.monster.Zombie zombie) {
                        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(5.0);
                    } else if (livingEntity instanceof net.minecraft.world.entity.monster.Skeleton skeleton) {
                        skeleton.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(4.0);
                    } else if (livingEntity instanceof net.minecraft.world.entity.monster.Creeper creeper) {
                    } else if (livingEntity instanceof net.minecraft.world.entity.animal.IronGolem) {
                        // 铁傀儡攻击力增强
                    }
                }
                
                // 添加新实体到世界
                level.addFreshEntity(newEntity);
            }
        }
    }
}