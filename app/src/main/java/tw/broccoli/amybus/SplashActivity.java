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

/**
 * Created by Broccoli on 2015/9/27.
 */
public class SplashActivity extends Activity{
    private boolean debugMode = true;
    private boolean hasUmbrella = false;

    private int hasUmbrellaCount = 0;

    private int animateCount = 0;
    private PathView mPathViewA = null;
    private PathView mPathViewM = null;
    private PathView mPathViewY = null;
    private PathView mPathViewB = null;
    private PathView mPathViewU = null;
    private PathView mPathViewS = null;
    private PathView mPathViewUmbrella = null;

    private TextView mTextViewADot = null;
    private TextView mTextViewBDot = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        if(debugMode){
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }else {
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
                    if (hasUmbrellaCount >= 3) hasUmbrella = true;
                }
            });

            goSVG(mPathViewA);
            goSVG(mPathViewM);
            goSVG(mPathViewY);
            goSVG(mPathViewB);
            goSVG(mPathViewU);
            goSVG(mPathViewS);
        }
    }

    private void goSVG(PathView pathView){
        animateCount++;

        pathView.getPathAnimator()
                .delay(200)
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
                                                Animation translateAnimation_A = new TranslateAnimation(mPathViewA.getScaleX(), mPathViewA.getScaleX()+200, mPathViewA.getScaleY(), mPathViewA.getScaleY());
                                                Animation translateAnimation_B = new TranslateAnimation(mPathViewB.getScaleX(), mPathViewB.getScaleX()+70, mPathViewB.getScaleY(), mPathViewB.getScaleY());
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
                                                        mTextViewADot.setX(510);
                                                        mTextViewADot.setY(1018);
                                                        mTextViewADot.setVisibility(View.VISIBLE);
                                                        mTextViewBDot.setX(1010);
                                                        mTextViewBDot.setY(1018);
                                                        mTextViewBDot.setVisibility(View.VISIBLE);
                                                        mTextViewADot.setAnimation(alphaAnimation);
                                                        mTextViewBDot.setAnimation(alphaAnimation);
                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animation animation) {
                                                        startMainActivity();
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
                                startMainActivity();
                            }
                        }
                    }
                })
                .interpolator(new AccelerateDecelerateInterpolator())
                .start();
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
                1000
        );
    }
}
