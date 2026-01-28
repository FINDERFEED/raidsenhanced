package com.finderfeed.raids_enhanced.content.entities.raid_blimp.raid_airship_parts;

import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import net.minecraft.resources.ResourceLocation;

public class RaidBlimpPartDefinition {

    public FDModel fdModel;
    public ResourceLocation texture;

    public RaidBlimpPartDefinition(FDModel fdModel, ResourceLocation texture) {
        this.fdModel = fdModel;
        this.texture = texture;
    }

}
