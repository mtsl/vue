package com.lateralthoughts.vue;

//internal imports
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.ScaleImageView;

public interface IAisleContentAdapter {    
    public void setContentSource(String uniqueAisleId, AisleWindowContent windowContent);
    
    public void releaseContentSource();
    
    public ScaleImageView getItemAt(int index, boolean isPivot);
    
    public void setPivot(int index);
    
    public void registerAisleDataObserver(IAisleDataObserver observer);
    
    public void unregisterAisleDataObserver(IAisleDataObserver observer);
    
    public int getAisleItemsCount();
    
    public boolean setAisleContent(AisleContentBrowser contentBrowser,ScaleImageView reuseImageView, int currentIndex, int wantedIndex, boolean shiftPivot);

	public void setSourceName(String sourceName);
}
