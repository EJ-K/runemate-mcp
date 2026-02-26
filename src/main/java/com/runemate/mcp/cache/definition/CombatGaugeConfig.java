package com.runemate.mcp.cache.definition;

import lombok.*;

@Data
public class CombatGaugeConfig {

    public int id;
//    public int field3276;
//    public int field3277 = 255;
//    public int field3278 = 255;
//    public int field3283 = -1;
//    public int field3272 = 1;
//    public int field3275 = 70;
    public int healthBarFrontSpriteId = -1;
    public int healthBarBackSpriteId = -1;
    public int healthScale = 30;
    public int healthBarPadding = 0;
}
