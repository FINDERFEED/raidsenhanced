package com.finderfeed.raids_enhanced.content.packets;

import com.finderfeed.fdlib.network.FDPacket;
import com.finderfeed.fdlib.network.RegisterFDPacket;
import com.finderfeed.raids_enhanced.REClientUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@RegisterFDPacket("raidsenhanced:pos_event")
public class REPosEventPacket extends FDPacket {

    private Vec3 pos;
    private int event;
    private int data;

    public REPosEventPacket(Vec3 pos, int event, int data) {
        this.pos = pos;
        this.event = event;
        this.data = data;
    }

    public REPosEventPacket(FriendlyByteBuf buf){
        this.pos = new Vec3(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
        this.event = buf.readInt();
        this.data = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeDouble(pos.x);
        registryFriendlyByteBuf.writeDouble(pos.y);
        registryFriendlyByteBuf.writeDouble(pos.z);
        registryFriendlyByteBuf.writeInt(event);
        registryFriendlyByteBuf.writeInt(data);
    }

    @Override
    public void clientAction(Supplier<NetworkEvent.Context> supplier) {
        REClientUtil.handlePosEvent(pos, event, data);
    }

    @Override
    public void serverAction(Supplier<NetworkEvent.Context> supplier) {

    }

}
