package com.finderfeed.raids_enhanced.content.packets;

import com.finderfeed.fdlib.network.FDPacket;
import com.finderfeed.fdlib.network.RegisterFDPacket;
import com.finderfeed.raids_enhanced.REClientUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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
        this.pos = buf.readVec3();
        this.event = buf.readInt();
        this.data = buf.readInt();
    }

    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeVec3(pos);
        registryFriendlyByteBuf.writeInt(event);
        registryFriendlyByteBuf.writeInt(data);
    }

    @Override
    public void clientAction(IPayloadContext iPayloadContext) {
        REClientUtil.handlePosEvent(pos, event, data);
    }

    @Override
    public void serverAction(IPayloadContext iPayloadContext) {

    }
}
