package com.finderfeed.raids_enhanced.content.entities.player_blimp;

import com.finderfeed.fdlib.network.FDPacket;
import com.finderfeed.fdlib.network.RegisterFDPacket;
import com.finderfeed.raids_enhanced.REClientUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@RegisterFDPacket("raidsenhanced:player_blimp_rotating")
public class PlayerBlimpRotatingPacket extends FDPacket {

    private int entityId;
    private int rotationDirection;

    public PlayerBlimpRotatingPacket(int entityId, int rotationDirection){
        this.entityId = entityId;
        this.rotationDirection = rotationDirection;
    }

    public PlayerBlimpRotatingPacket(FriendlyByteBuf buf){
        this.entityId = buf.readInt();
        this.rotationDirection = buf.readByte();
    }

    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeInt(entityId);
        registryFriendlyByteBuf.writeByte(rotationDirection);
    }

    @Override
    public void clientAction(IPayloadContext iPayloadContext) {
    }

    @Override
    public void serverAction(IPayloadContext iPayloadContext) {
        var player = iPayloadContext.player();
        if (player.level().getEntity(this.entityId) instanceof PlayerBlimpEntity playerBlimp) {
            playerBlimp.setRotatingState(rotationDirection);
        }

    }
}
