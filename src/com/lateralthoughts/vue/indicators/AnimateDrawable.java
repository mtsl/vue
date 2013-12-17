package com.lateralthoughts.vue.indicators;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

public class AnimateDrawable extends Drawable {
    
    private Animation mAnimation;
    private Transformation mTransformation = new Transformation();
    
    private Drawable proxy;
    
    // calls super
    public AnimateDrawable(Drawable target) {
    }
    
    public AnimateDrawable(Drawable target, Animation animation) {
        proxy = target;
        mAnimation = animation;
    }
    
    @Override
    public void draw(Canvas canvas) {
        Drawable dr = proxy;
        if (dr != null) {
            int sc = canvas.save();
            Animation anim = mAnimation;
            if (anim != null) {
                anim.getTransformation(
                        AnimationUtils.currentAnimationTimeMillis(),
                        mTransformation);
                canvas.concat(mTransformation.getMatrix());
            }
            dr.draw(canvas);
            canvas.restoreToCount(sc);
        }
    }
    
    @Override
    public int getOpacity() {
        return 0;
    }
    
    @Override
    public void setAlpha(int alpha) {
        
    }
    
    @Override
    public void setColorFilter(ColorFilter cf) {
        
    }
}
