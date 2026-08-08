package com.qituo.dcc.network;

import com.qituo.dcc.DragonCurseChronicles;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = DragonCurseChronicles.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {

    public static final String TAG_AURA_DISABLED = "dcc_aura_disabled";

    public static void register() {
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                ToggleAuraPayload.TYPE,
                ToggleAuraPayload.STREAM_CODEC,
                ToggleAuraPayload::handle
        );
    }

    public record ToggleAuraPayload() implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<ToggleAuraPayload> TYPE = new CustomPacketPayload.Type<>(
                ResourceLocation.fromNamespaceAndPath(DragonCurseChronicles.MODID, "aura_toggle")
        );

        public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ToggleAuraPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> {},
                buf -> new ToggleAuraPayload()
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(ToggleAuraPayload payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (!(ctx.player() instanceof ServerPlayer player)) return;

                CompoundTag data = player.getPersistentData();
                boolean currentlyDisabled = data.getBoolean(TAG_AURA_DISABLED);
                boolean newState = !currentlyDisabled;
                data.putBoolean(TAG_AURA_DISABLED, newState);

                String key = newState ? "dcc.message.aura.disabled" : "dcc.message.aura.enabled";
                player.displayClientMessage(
                        Component.translatable(key).withStyle(ChatFormatting.GREEN), true);
            });
        }
    }
}
