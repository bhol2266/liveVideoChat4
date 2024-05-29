package com.bhola.livevideochat4.Models;

public class GiftItemModel {
    String giftName;
    int coin;
    boolean isSelected;


    public GiftItemModel() {
    }

    public GiftItemModel(String giftName, int coin, boolean isSelected) {
        this.giftName = giftName;
        this.coin = coin;
        this.isSelected = isSelected;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
