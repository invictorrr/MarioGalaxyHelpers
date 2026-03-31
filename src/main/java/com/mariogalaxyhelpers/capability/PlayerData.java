package com.mariogalaxyhelpers.capability;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class PlayerData {

    private int coins;
    private final Set<String> hiredNPCs = new HashSet<>();
    private int killCount;
    private int eggsCollected;
    private int starBitsCollected;
    private int powerStarsCollected;

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    public void addCoins(int amount) {
        coins += amount;
    }

    public boolean spendCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }

    public boolean isHired(String npcId) {
        return hiredNPCs.contains(npcId);
    }

    public void hire(String npcId) {
        hiredNPCs.add(npcId);
    }

    public int getHiredCount() {
        return hiredNPCs.size();
    }

    public void incrementKills() {
        killCount++;
    }

    public int getKillCount() {
        return killCount;
    }

    public void incrementEggsCollected(int n) {
        eggsCollected += n;
    }

    public void incrementStarBitsCollected(int n) {
        starBitsCollected += n;
    }

    public void incrementPowerStarsCollected(int n) {
        powerStarsCollected += n;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("coins", coins);
        tag.putInt("killCount", killCount);
        tag.putInt("eggsCollected", eggsCollected);
        tag.putInt("starBitsCollected", starBitsCollected);
        tag.putInt("powerStarsCollected", powerStarsCollected);
        ListTag hiredList = new ListTag();
        for (String npc : hiredNPCs) {
            hiredList.add(StringTag.valueOf(npc));
        }
        tag.put("hiredNPCs", hiredList);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        coins = tag.getInt("coins");
        killCount = tag.getInt("killCount");
        eggsCollected = tag.getInt("eggsCollected");
        starBitsCollected = tag.getInt("starBitsCollected");
        powerStarsCollected = tag.getInt("powerStarsCollected");
        hiredNPCs.clear();
        ListTag hiredList = tag.getList("hiredNPCs", Tag.TAG_STRING);
        for (int i = 0; i < hiredList.size(); i++) {
            hiredNPCs.add(hiredList.getString(i));
        }
    }
}
