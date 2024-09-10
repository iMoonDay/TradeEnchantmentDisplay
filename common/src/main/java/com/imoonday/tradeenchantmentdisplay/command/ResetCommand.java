package com.imoonday.tradeenchantmentdisplay.command;

import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferCache;

public class ResetCommand implements Command {

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public void run() {
        MerchantOfferCache.getInstance().clear();
    }
}
