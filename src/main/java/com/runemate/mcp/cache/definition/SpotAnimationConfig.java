package com.runemate.mcp.cache.definition;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(of = { "id", "debugName" })
public class SpotAnimationConfig {

    public String debugName;
    public int rotation = 0;
    public short[] textureToReplace;
    public int id;
    public short[] textureToFind;
    public int resizeY = 128;
    public int animationId = -1;
    public short[] recolorToFind;
    public short[] recolorToReplace;
    public int resizeX = 128;
    public int modelId;
    public int ambient = 0;
    public int contrast = 0;
}
