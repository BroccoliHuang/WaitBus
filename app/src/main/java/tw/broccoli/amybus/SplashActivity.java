package tw.broccoli.amybus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.eftimoff.androipathview.PathView;

import java.io.IOException;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Broccoli on 2015/9/27.
 */
public class SplashActivity extends Activity{
    private boolean debugMode = false;
    private boolean hasUmbrella = false;

    private int hasUmbrellaCount = 0;

    private int animateCount = 0;
    private GifImageView mGifImageViewPlugin = null;
    private PathView mPathViewA = null;
    private PathView mPathViewM = null;
    private PathView mPathViewY = null;
    private PathView mPathViewB = null;
    private PathView mPathViewU = null;
    private PathView mPathViewS = null;
    private PathView mPathViewUmbrella = null;
    private TextView mTextViewADot = null;
    private TextView mTextViewBDot = null;

    private GifDrawable mGifDrawableA = null;
    private GifDrawable mGifDrawableB = null;
    private GifDrawable mGifDrawableC = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        if(debugMode){
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }else {
            mGifImageViewPlugin = (GifImageView) findViewById(R.id.gifImageView_plugin);
            mPathViewA = (PathView) findViewById(R.id.pathView_a);
            mPathViewM = (PathView) findViewById(R.id.pathView_m);
            mPathViewY = (PathView) findViewById(R.id.pathView_y);
            mPathViewB = (PathView) findViewById(R.id.pathView_b);
            mPathViewU = (PathView) findViewById(R.id.pathView_u);
            mPathViewS = (PathView) findViewById(R.id.pathView_s);
            mPathViewUmbrella = (PathView) findViewById(R.id.pathView_umbrella);
            mTextViewADot = (TextView) findViewById(R.id.textview_a_dot);
            mTextViewBDot = (TextView) findViewById(R.id.textview_b_dot);

            mPathViewB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hasUmbrellaCount++;
                    if (hasUmbrellaCount >= 6) hasUmbrella = true;
                }
            });


            try {
                mGifDrawableA = new GifDrawable(getResources(), R.mipmap.plugin_a);
                mGifImageViewPlugin.setImageDrawable(mGifDrawableA);
                mGifDrawableA.addAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationCompleted() {
                        mGifDrawableA.stop();
                        try {
                            mGifDrawableB = new GifDrawable(getResources(), R.mipmap.plugin_b);
                            mGifImageViewPlugin.setImageDrawable(mGifDrawableB);
                            mGifDrawableB.addAnimationListener(new AnimationListener() {
                                @Override
                                public void onAnimationCompleted() {
                                    mGifDrawableB.stop();
                                }
                            });
                            mGifDrawableB.start();

                            goSVG(mPathViewA);
                            goSVG(mPathViewM);
                            goSVG(mPathViewY);
                            goSVG(mPathViewB);
                            goSVG(mPathViewU);
                            goSVG(mPathViewS);
                        }catch(IOException ioe){
                        }
                    }
                });
                mGifDrawableA.start();
            }catch(IOException ioe){
            }
        }
    }

    private void goSVG(PathView pathView){
        animateCount++;

        pathView.getPathAnimator()
                .delay(300)
                .duration(2000)
                .listenerStart(null)
                .listenerEnd(new PathView.AnimatorBuilder.ListenerEnd() {
                    @Override
                    public void onAnimationEnd() {
                        animateCount--;
                        if (animateCount == 0) {
                            if (hasUmbrella) {
                                mPathViewUmbrella.getPathAnimator()
                                        .delay(200)
                                        .duration(1000)
                                        .listenerStart(new PathView.AnimatorBuilder.ListenerStart() {
                                            @Override
                                            public void onAnimationStart() {
                                                Animation alphaAnimation = new AlphaAnimation(1f, 0f);
                                                alphaAnimation.setDuration(1000);
                                                alphaAnimation.setFillAfter(true);
                                                mPathViewM.startAnimation(alphaAnimation);
                                                mPathViewY.startAnimation(alphaAnimation);
                                                mPathViewU.startAnimation(alphaAnimation);
                                                mPathViewS.startAnimation(alphaAnimation);
                                            }
                                        })
                                        .listenerEnd(new PathView.AnimatorBuilder.ListenerEnd() {
                                            @Override
                                            public void onAnimationEnd() {
                                                //Samsung S6
//                                                Animation translateAnimation_A = new TranslateAnimation(mPathViewA.getScaleX(), mPathViewA.getScaleX()+200, mPathViewA.getScaleY(), mPathViewA.getScaleY());
//                                                Animation translateAnimation_B = new TranslateAnimation(mPathViewB.getScaleX(), mPathViewB.getScaleX()+70, mPathViewB.getScaleY(), mPathViewB.getScaleY());
                                                //hTC Desire 610
                                                Animation translateAnimation_A = new TranslateAnimation(mPathViewA.getScaleX(), mPathViewA.getScaleX()+75, mPathViewA.getScaleY(), mPathViewA.getScaleY());
                                                Animation translateAnimation_B = new TranslateAnimation(mPathViewB.getScaleX(), mPathViewB.getScaleX()+27, mPathViewB.getScaleY(), mPathViewB.getScaleY());
                                                translateAnimation_A.setDuration(500);
                                                translateAnimation_B.setDuration(500);
                                                translateAnimation_A.setFillAfter(true);
                                                translateAnimation_B.setFillAfter(true);
                                                translateAnimation_B.setAnimationListener(new Animation.AnimationListener() {
                                                    @Override
                                                    public void onAnimationStart(Animation animation) {
                                                        Animation alphaAnimation = new AlphaAnimation(0f, 1f);
                                                        alphaAnimation.setDuration(500);
                                                        alphaAnimation.setFillAfter(true);
                                                        //Samsung S6
//                                                        mTextViewADot.setX(510);
//                                                        mTextViewADot.setY(1070);
                                                        //hTC Desire 610
                                                        mTextViewADot.setX(192);
                                                        mTextViewADot.setY(365);
                                                        mTextViewADot.setVisibility(View.VISIBLE);
                                                        //Samsung S6
//                                                        mTextViewBDot.setX(1010);
//                                                        mTextViewBDot.setY(1070);
                                                        //hTC Desire 610
                                                        mTextViewBDot.setX(379);
                                                        mTextViewBDot.setY(365);
                                                        mTextViewBDot.setVisibility(View.VISIBLE);
                                                        mTextViewADot.setAnimation(alphaAnimation);
                                                        mTextViewBDot.setAnimation(alphaAnimation);
                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animation animation) {
                                                        PluginC();
                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animation animation) {
                                                    }
                                                });
                                                mPathViewA.startAnimation(translateAnimation_A);
                                                mPathViewB.startAnimation(translateAnimation_B);
                                            }
                                        })
                                        .interpolator(new AccelerateDecelerateInterpolator())
                                        .start();
                            } else {
                                PluginC();
                            }
                        }
                    }
                })
                .interpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void PluginC(){
        try {
            mGifDrawableC = new GifDrawable(getResources(), R.mipmap.plugin_c);
            mGifImageViewPlugin.setImageDrawable(mGifDrawableC);
            mGifDrawableC.addAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationCompleted() {
                    mGifDrawableC.stop();
                    startMainActivity();
                }
            });
            mGifDrawableC.start();
        }catch(IOException ioe){
        }
    }

    private void startMainActivity(){
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                },
                400
        );
    }
}
