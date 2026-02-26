package com.runemate.mcp.cache.definition;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import lombok.*;
import org.jetbrains.annotations.*;

@Data
@ToString(of = {"id", "name"})
@EqualsAndHashCode(callSuper = false)
public class ItemConfig {

    public final int id;

    public String name = "null";
    public String examine;
    public String unknown1;

    public int resizeX = 128;
    public int resizeY = 128;
    public int resizeZ = 128;

    public int xan2d = 0;
    public int yan2d = 0;
    public int zan2d = 0;

    public int cost = 1;
    public boolean isTradeable;
    public int stackable = 0;
    public int inventoryModel;

    public int wearPos1 = -1;
    public int wearPos2 = -1;
    public int wearPos3 = -1;

    public boolean members = false;

    public short[] colorFind;
    public short[] colorReplace;
    public short[] textureFind;
    public short[] textureReplace;

    public int zoom2d = 2000;
    public int xOffset2d = 0;
    public int yOffset2d = 0;

    public int ambient;
    public int contrast;

    public int[] countCo;
    public int[] countObj;

    public String[] actions = new String[] { null, null, "Take", null, null };
    public String[][] subops;

    public String[] inventoryActions = new String[] { null, null, null, null, "Drop" };

    public int maleModel0 = -1;
    public int maleModel1 = -1;
    public int maleModel2 = -1;
    public int maleOffset;
    public int maleHeadModel = -1;
    public int maleHeadModel2 = -1;

    public int femaleModel0 = -1;
    public int femaleModel1 = -1;
    public int femaleModel2 = -1;
    public int femaleOffset;
    public int femaleHeadModel = -1;
    public int femaleHeadModel2 = -1;

    public int category;

    public int notedId = -1;
    public int notedTemplate = -1;

    public int team;
    public int weight;

    public int shiftClickDropIndex = -2;

    public int cosmeticId = -1;
    public int cosmeticTemplateId = -1;

    public int placeholderId = -1;
    public int placeholderTemplateId = -1;

    public Map<Integer, Object> params = null;

    public void linkNote(ItemConfig notedItem, ItemConfig unnotedItem) {
        if (notedItem == null || unnotedItem == null ) {
            return;
        }

        this.inventoryModel = notedItem.inventoryModel;
        this.zoom2d = notedItem.zoom2d;
        this.xan2d = notedItem.xan2d;
        this.yan2d = notedItem.yan2d;
        this.zan2d = notedItem.zan2d;
        this.xOffset2d = notedItem.xOffset2d;
        this.yOffset2d = notedItem.yOffset2d;
        this.colorFind = notedItem.colorFind;
        this.colorReplace = notedItem.colorReplace;
        this.textureFind = notedItem.textureFind;
        this.textureReplace = notedItem.textureReplace;
        this.name = unnotedItem.name;
        this.members = unnotedItem.members;
        this.cost = unnotedItem.cost;
        this.stackable = 1;
    }

    public void linkBought(ItemConfig var1, ItemConfig var2) {
        if (var1 == null || var2 == null) {
            return;
        }

        this.inventoryModel = var1.inventoryModel;
        this.zoom2d = var1.zoom2d;
        this.xan2d = var1.xan2d;
        this.yan2d = var1.yan2d;
        this.zan2d = var1.zan2d;
        this.xOffset2d = var1.xOffset2d;
        this.yOffset2d = var1.yOffset2d;
        this.colorFind = var2.colorFind;
        this.colorReplace = var2.colorReplace;
        this.textureFind = var2.textureFind;
        this.textureReplace = var2.textureReplace;
        this.name = var2.name;
        this.members = var2.members;
        this.stackable = var2.stackable;
        this.maleModel0 = var2.maleModel0;
        this.maleModel1 = var2.maleModel1;
        this.maleModel2 = var2.maleModel2;
        this.femaleModel0 = var2.femaleModel0;
        this.femaleModel1 = var2.femaleModel1;
        this.femaleModel2 = var2.femaleModel2;
        this.maleHeadModel = var2.maleHeadModel;
        this.maleHeadModel2 = var2.maleHeadModel2;
        this.femaleHeadModel = var2.femaleHeadModel;
        this.femaleHeadModel2 = var2.femaleHeadModel2;
        this.team = var2.team;
        this.actions = var2.actions;
        this.inventoryActions = new String[5];
        if (var2.inventoryActions != null) {
            for (int var3 = 0; var3 < 4; ++var3) {
                this.inventoryActions[var3] = var2.inventoryActions[var3];
            }
        }

        this.inventoryActions[4] = "Discard";
        this.cost = 0;
    }

    public void linkPlaceholder(ItemConfig var1, ItemConfig var2) {
        if (var1 == null || var2 == null) {
            return;
        }

        this.inventoryModel = var1.inventoryModel;
        this.zoom2d = var1.zoom2d;
        this.xan2d = var1.xan2d;
        this.yan2d = var1.yan2d;
        this.zan2d = var1.zan2d;
        this.xOffset2d = var1.xOffset2d;
        this.yOffset2d = var1.yOffset2d;
        this.colorFind = var1.colorFind;
        this.colorReplace = var1.colorReplace;
        this.textureFind = var1.textureFind;
        this.textureReplace = var1.textureReplace;
        this.stackable = var1.stackable;
        this.name = var2.name;
        this.cost = 0;
        this.members = false;
        this.isTradeable = false;
    }
}
