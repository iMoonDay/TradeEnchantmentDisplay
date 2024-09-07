package com.imoonday.tradeenchantmentdisplay.command;

import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferCache;

public class SaveCommand implements Command {

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public void run() {
        MerchantOfferCache.getInstance().save();
    }
}
