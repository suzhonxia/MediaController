package com.sun.mediacontroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.sun.mediacontroller.databinding.ActivitySplashBinding;

/**
 * @author Sun
 * @date 2019/4/2 10:07
 * @desc
 */
public class SplashActivity extends Activity {
    ActivitySplashBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        binding.getRoot().postDelayed(this::launcherOptions, 1500);
    }

    private void launcherOptions() {
        float distance = ScreenUtils.getScreenHeight() - binding.achor.getTop() - SizeUtils.getMeasuredHeight(binding.achor) * 0.9f;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.achor, "scaleX", 1, 0.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.achor, "scaleY", 1, 0.8f);
        ObjectAnimator translation = ObjectAnimator.ofFloat(binding.achor, "translationY", 0, distance);

        AnimatorSet animator = new AnimatorSet();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                super.onAnimationEnd(animation, isReverse);
                binding.setHideAchor(true);
                binding.cover.setImageResource(R.mipmap.splash);
                binding.getRoot().postDelayed(() -> enter(binding.enter), 3000);
            }
        });
        animator.playTogether(scaleX, scaleY, translation);
        animator.setDuration(600);
        animator.start();
    }

    public void enter(View v) {
        startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }

    @Override
    public void onBackPressed() {

    }
}
