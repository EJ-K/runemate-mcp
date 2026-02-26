package com.runemate.mcp.cache.definition;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import lombok.*;
import org.jetbrains.annotations.*;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(of = {"id", "name"})
public class ObjectConfig {

    private final int id;
    private short[] retextureToFind;
    private int decorDisplacement = 16;
    private boolean isHollow = false;
    private String name = "null";
    private int[] objectModels;
    private int[] objectTypes;
    private short[] recolorToFind;
    private int mapAreaId = -1;
    private short[] retextureToReplace;
    private int sizeX = 1;
    private int sizeY = 1;
    private int ambientSoundDistance = 0;
    private int[] ambientSoundIds;
    private int ambientSoundRetain;
    private int offsetX = 0;
    private boolean mergeNormals = false;
    private int wallOrDoor = -1;
    private int animationID = -1;
    private int varbitID = -1;
    private int ambient = 0;
    private int contrast = 0;
    private String[] actions = new String[5];
    private int interactType = 2;
    private int mapSceneID = -1;
    private int blockingMask = 0;
    private short[] recolorToReplace;
    private boolean shadow = true;
    private int modelSizeX = 128;
    private int modelSizeHeight = 128;
    private int modelSizeY = 128;
    private int objectID;
    private int offsetHeight = 0;
    private int offsetY = 0;
    private boolean obstructsGround = false;
    private int contouredGround = -1;
    private int supportsItems = -1;
    private int[] configs;
    private int category;
    private boolean isRotated = false;
    private int varpID = -1;
    private int ambientSoundId = -1;
    private boolean modelClipped = false;
    private int soundDistanceFadeCurve;
    private int soundFadeInDuration = 300;
    private int soundFadeOutDuration = 300;
    private int soundFadeInCurve;
    private int soundFadeOutCurve;
    private int soundVisibility = 2;
    private int raise;
    private int ambientSoundChangeTicksMin = 0;
    private int ambientSoundChangeTicksMax = 0;
    private boolean blocksProjectile = true;
    private boolean randomizeAnimStart;
    private boolean deferAnimChange;
    private boolean unknown1 = false;
    private Map<Integer, Object> params = null;

}
