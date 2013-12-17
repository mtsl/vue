package com.lateralthoughts.vue.utils;

import java.util.Comparator;

public class SortBasedOnImageWidthHeight implements
        Comparator<OtherSourceImageDetails> {
    @Override
    public int compare(OtherSourceImageDetails lhs, OtherSourceImageDetails rhs) {
        return lhs.getWidthHeightMultipliedValue()
                - rhs.getWidthHeightMultipliedValue();
    }
    
}
