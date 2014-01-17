package com.lateralthoughts.vue;

//internal imports
import com.lateralthoughts.vue.ui.AisleContentBrowser;
import com.lateralthoughts.vue.ui.ScaleImageView;

public interface IAisleContentAdapter {
    public void setContentSource(String uniqueAisleId,
            AisleWindowContent windowContent);
    
    public void releaseContentSource();
    
    public ScaleImageView getItemAt(int index, boolean isPivot);
    
    public void setPivot(int index);
    
    public void registerAisleDataObserver(IAisleDataObserver observer);
    
    public void unregisterAisleDataObserver(IAisleDataObserver observer);
    
    public int getAisleItemsCount();
    
    public boolean setAisleContent(AisleContentBrowser contentBrowser,
            int currentIndex, int wantedIndex, boolean shiftPivot);
    
    public boolean hasMostLikes(int i);
    
    public String getAisleId();
    
    public boolean hasSameLikes(int position);
    
    public String getImageLikesCount(int i);
    
    public boolean getBookmarkIndicator();
    
    public int getBookmarkCount();
    
    public void setAisleBookmarkIndicator(boolean b);
    
    public void setBookmarkCount(int mBookmarksCount);
    
    public boolean getImageLikeStatus(int i);
    
    public void setImageLikeStatus(boolean b, int mCurrentIndex);
    
    public void setImageLikesCount(int mCurrentIndex, int likesCount);
    
    public String getImageId(int mCurrentIndex);
    
}
