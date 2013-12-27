package com.lateralthoughts.vue.utils;

public class UsedKeywordsOnUpgrade {
    
    public String lastUsedTime;
    public String numberOfTimesUsed;
    public String keyWord;
    
    public UsedKeywordsOnUpgrade(String keyWord, String lastUsedTime,
            String numberOfTimesUsed) {
        this.keyWord = keyWord;
        this.lastUsedTime = lastUsedTime;
        this.numberOfTimesUsed = numberOfTimesUsed;
    }
    
}
