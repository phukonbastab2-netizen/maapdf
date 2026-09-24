package in.sahayak.documents;
import android.content.Context;import android.graphics.Color;import android.view.*;import android.widget.*;import android.animation.*;import android.provider.Settings;

/** Lightweight 2D mascot animation; respects Android's animation scale setting. */
public class MaaHeader extends LinearLayout {
 private AnimatorSet idle; private ImageView mother;
 public MaaHeader(Context context){super(context);setGravity(Gravity.CENTER_VERTICAL);int d=(int)(110*getResources().getDisplayMetrics().density);mother=new ImageView(context);mother.setImageResource(R.drawable.maa);mother.setScaleType(ImageView.ScaleType.FIT_CENTER);mother.setContentDescription("Maa, your friendly document guide");addView(mother,new LinearLayout.LayoutParams(d,d));LinearLayout words=new LinearLayout(context);words.setOrientation(LinearLayout.VERTICAL);TextView brand=new TextView(context);brand.setText("maapdf");brand.setTextSize(28);brand.setTextColor(Color.rgb(42,78,60));words.addView(brand);TextView greeting=new TextView(context);greeting.setText("Let's do it together.\nआओ, आसान बनाते हैं।");greeting.setTextSize(17);greeting.setTextColor(Color.rgb(94,80,63));words.addView(greeting);addView(words,new LinearLayout.LayoutParams(0,-2,1));mother.setOnClickListener(v->{if(idle!=null&&idle.isRunning()){idle.cancel();mother.setRotation(0);mother.setTranslationY(0);}else animateMaa();});}
 private void animateMaa(){if(Settings.Global.getFloat(getContext().getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,1)==0)return;ObjectAnimator sway=ObjectAnimator.ofFloat(mother,"rotation",-1.5f,1.5f,-1.5f);sway.setDuration(3200);sway.setRepeatCount(ValueAnimator.INFINITE);ObjectAnimator breathe=ObjectAnimator.ofFloat(mother,"translationY",0,-4,0);breathe.setDuration(2400);breathe.setRepeatCount(ValueAnimator.INFINITE);idle=new AnimatorSet();idle.playTogether(sway,breathe);idle.start();}
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();animateMaa();}
 @Override protected void onDetachedFromWindow(){if(idle!=null)idle.cancel();super.onDetachedFromWindow();}
}
