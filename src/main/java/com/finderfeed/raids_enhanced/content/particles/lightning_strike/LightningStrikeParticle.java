package com.finderfeed.raids_enhanced.content.particles.lightning_strike;

import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.fdlib.util.rendering.FDRenderUtil;
import com.finderfeed.raids_enhanced.content.particles.AnimatedSpriteParticle;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.finderfeed.raids_enhanced.content.particles.lightning_explosion.LightningExplosionParticle;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class LightningStrikeParticle extends AnimatedSpriteParticle {

    private LightningStrikeParticleOptions options;


    public LightningStrikeParticle(SpriteSet spriteSet, LightningStrikeParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(spriteSet, options, level, x, y, z, xd, yd, zd);
        this.options = options;
    }

    @Override
    public void render(VertexConsumer vertex, Camera camera, float pticks) {
        float size = this.getQuadSize(pticks);
        float u1 = this.getU0();
        float u2 = this.getU1();
        float v1 = this.getV0();
        float v2 = this.getV1();

        Vec3 pos = new Vec3(
                Mth.lerp(pticks,this.xo,this.x),
                Mth.lerp(pticks,this.yo,this.y),
                Mth.lerp(pticks,this.zo,this.z)
        );

        Vec3 b = this.options.direction;
        pos = pos.subtract(camera.getPosition());

        Matrix4f mat = new Matrix4f();
        mat.translate((float)pos.x,(float)pos.y,(float)pos.z);
        FDRenderUtil.applyMovementMatrixRotations(mat,b);


        Vec3 n = FDMathUtil.getNormalVectorFromLineToPoint(pos,b.add(pos),Vec3.ZERO);


        Matrix4f mt2 = new Matrix4f();
        FDRenderUtil.applyMovementMatrixRotations(mt2,b);
        Vector4f up = new Vector4f(0,0,1,1); mt2.transform(up);
        Vector4f left = new Vector4f(1,0,0,1); mt2.transform(left);
        Vec3 vup = new Vec3(up.x / up.w,up.y / up.w,up.z / up.w);
        Vec3 vleft = new Vec3(left.x,left.y,left.z);

        float angle = (float) FDMathUtil.angleBetweenVectors(n,vup);

        if (vleft.dot(n) > 0) {
            mat.rotateY(angle);
        }else{
            mat.rotateY(-angle);
        }



        vertex.addVertex(mat, -size/2,0,0).setUv(u2,v2).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(LightTexture.FULL_BRIGHT);
        vertex.addVertex(mat, -size/2,size,0).setUv(u1,v2).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(LightTexture.FULL_BRIGHT);
        vertex.addVertex(mat, size/2,size,0).setUv(u1,v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(LightTexture.FULL_BRIGHT);
        vertex.addVertex(mat, size/2,0,0).setUv(u2,v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(LightTexture.FULL_BRIGHT);

        vertex.addVertex(mat, size/2,0,0).setUv(u2,v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(LightTexture.FULL_BRIGHT);
        vertex.addVertex(mat, size/2,size,0).setUv(u1,v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(LightTexture.FULL_BRIGHT);
        vertex.addVertex(mat, -size/2,size,0).setUv(u1,v2).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(LightTexture.FULL_BRIGHT);
        vertex.addVertex(mat, -size/2,0,0).setUv(u2,v2).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(LightTexture.FULL_BRIGHT);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    protected int getLightColor(float p_107249_) {
        return LightTexture.FULL_BRIGHT;
    }

    public static class Provider implements ParticleProvider<LightningStrikeParticleOptions>{

        private SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet){
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(LightningStrikeParticleOptions p_107421_, ClientLevel p_107422_, double p_107423_, double p_107424_, double p_107425_, double p_107426_, double p_107427_, double p_107428_) {
            return new LightningStrikeParticle(spriteSet, p_107421_, p_107422_, p_107423_, p_107424_, p_107425_, p_107426_, p_107427_, p_107428_);
        }
    }

}
