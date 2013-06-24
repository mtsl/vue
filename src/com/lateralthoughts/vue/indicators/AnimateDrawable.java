package com.lateralthoughts.vue.indicators;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

public class AnimateDrawable extends /*ProxyDrawable */ Drawable{

    private Animation mAnimation;
    private Transformation mTransformation = new Transformation();
 
    private Drawable proxy;
 //calls super
    public AnimateDrawable(Drawable target) {
       // super(target);
    }

    public AnimateDrawable(Drawable target, Animation animation) {
       // super(target);
        proxy = target;
        mAnimation = animation;
    }
/*
    public Animation getAnimation() {
        return mAnimation;
    }

    public void setAnimation(Animation anim) {
        mAnimation = anim;
    }

    public boolean hasStarted() {
        return mAnimation != null && mAnimation.hasStarted();
    }

    public boolean hasEnded() {
        return mAnimation == null || mAnimation.hasEnded();
    }*/

    @Override
    public void draw(Canvas canvas) {
       // Drawable dr = getProxy();
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		
	}
}

