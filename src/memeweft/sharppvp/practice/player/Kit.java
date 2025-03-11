package me.memeweft.sharppvp.practice.player;

import me.memeweft.sharppvp.practice.util.MiscUtil;
import me.memeweft.sharppvp.practice.util.PlyInv;

public class Kit 
{
    private String name;
    private PlyInv inv;
    
    public Kit(final String name, final PlyInv inv) {
        this.name = name;
        this.inv = inv;
    }
    
    @Override
    public String toString() {
        return this.name + "|" + MiscUtil.playerInventoryToString(this.inv);
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public PlyInv getInv() {
        return this.inv;
    }
    
    public void setInv(final PlyInv inv) {
        this.inv = inv;
    }
}
