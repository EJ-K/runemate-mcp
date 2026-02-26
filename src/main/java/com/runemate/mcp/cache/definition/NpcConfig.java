package com.runemate.mcp.cache.definition;

import java.util.*;
import lombok.*;

@Data
@EqualsAndHashCode
@ToString(of = {"id", "name"})
public class NpcConfig {

    private final int id;
    private String name = "null";
    private int size = 1;
    private int[] models;
    private int[] chatheadModels;
    private int standingAnimation = -1;
    private int idleRotateLeftAnimation = -1;
    private int idleRotateRightAnimation = -1;
    private int walkingAnimation = -1;
    private int rotate180Animation = -1;
    private int rotateLeftAnimation = -1;
    private int rotateRightAnimation = -1;
    private int runAnimation = -1;
    private int runRotate180Animation = -1;
    private int runRotateLeftAnimation = -1;
    private int runRotateRightAnimation = -1;
    private int crawlAnimation = -1;
    private int crawlRotate180Animation = -1;
    private int crawlRotateLeftAnimation = -1;
    private int crawlRotateRightAnimation = -1;
    private short[] recolorToFind;
    private short[] recolorToReplace;
    private short[] retextureToFind;
    private short[] retextureToReplace;
    private String[] actions = new String[5];
    private boolean isMinimapVisible = true;
    private int combatLevel = -1;
    private int widthScale = 128;
    private int heightScale = 128;
    private int renderPriority = 0;
    private int ambient;
    private int contrast;
    private int[] headIconArchiveIds;
    private short[] headIconSpriteIndex;
    private int rotationSpeed = 32;
    private int[] configs;
    private int varbitId = -1;
    private int varpIndex = -1;
    private boolean isInteractable = true;
    private boolean rotationFlag = true;
    private boolean isFollower;
    private boolean lowPriorityFollowerOps;
    private Map<Integer, Object> params;
    private int category;
    private int height = -1;
    private int[] stats = { 1, 1, 1, 1, 1, 1 };
    private int footprintSize = -1;
    private boolean canHideForOverlap;
    private int overlapTintHSL = 39188;
    private boolean unknown1 = false;
    private boolean zbuf = true;
}
