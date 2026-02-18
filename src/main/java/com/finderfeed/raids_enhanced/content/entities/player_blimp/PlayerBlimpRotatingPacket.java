package com.finderfeed.raids_enhanced.content.entities.player_blimp;

import com.finderfeed.fdlib.network.FDPacket;
import com.finderfeed.fdlib.network.RegisterFDPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

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
    public void write(FriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeInt(entityId);
        registryFriendlyByteBuf.writeByte(rotationDirection);
    }


    @Override
    public void clientAction(Supplier<NetworkEvent.Context> supplier) {

    }

    @Override
    public void serverAction(Supplier<NetworkEvent.Context> supplier) {
        var player = supplier.get().getSender();
        if (player.level().getEntity(this.entityId) instanceof PlayerBlimpEntity playerBlimp) {
            playerBlimp.setRotatingState(rotationDirection);
        }
    }
}
