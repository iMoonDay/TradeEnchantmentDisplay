package com.imoonday.tradeenchantmentdisplay.command;

import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferCache;

public class ReloadCommand implements Command {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void run() {
        MerchantOfferCache.getInstance().load();
    }
}
