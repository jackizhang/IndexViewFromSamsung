package code;


import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class IndexViewFromSamsung extends View{

	private static final String TAG = IndexViewFromSamsung.class.getSimpleName();
	
	private String[] mAlphabets = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
	
	private int mIndexWidth = ConvertUtils.toPx(30);//默认index的宽度30dp
	private int mIndexRightPadding = ConvertUtils.toPx(10);//默认index的距离View右边的边距
	private Paint mPaint;
	private int mIndexRectRadius = mIndexWidth/2;  //IndexView的圆角半径
	private int mFloatingIndexRadius = ConvertUtils.toPx(40); 
	
	private int mIndexTextColor = 0xffC0C0C0;   
	private int mIndexStrokeColor = 0xff969696;
	private int mPopTextColor = 0xf8;
	private int mPopBackColor = 0xcfFF9966; //半透明
	private float mSingleHeight;
	
	private int mIndexStrokeX; // indexView 的外边框的起始x
	private int mIndexStrokeY; // indexView 的外边框的起始y
	
	private int mIndexTextX;  //inexView 的当前在画的index的x坐标
	private float mIndexTextY;  //inexView 的当前在画的index的y坐标
	
	private Point mTopSupportPoint1,mBottomSupportPoint1,mTopSupportPoint2,mBottomSupportPoint2; //上下两个辅助点
	private Point mTopEndPoint,mBottomEndPoint;			//上下两个终点(在波浪上)
	private int mTopStartY;			//水波纹上面的起点的Y坐标
	private int mBottomStartY;     //水波纹下面起点的Y坐标
	private Point mPopCenter;
	private int touchY;
	private int mChosedIndex = -1;
	
	private ValueAnimator waveAnim,ballAnim;
	
	private OnAlphbetTouchListener mListener;
	
	public IndexViewFromSamsung(Context context) {
		super(context);
		initPaint();
	}
	
	public IndexViewFromSamsung(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}
	
	public IndexViewFromSamsung(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initPaint();
	}

	public void initPaint(){
		mPaint = new Paint();
		mPaint.setAntiAlias(true);//抗锯齿
	
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mSingleHeight = (float)(getHeight() - 2*mIndexRectRadius)/mAlphabets.length;
		float textSize = mSingleHeight*0.5f;
		mPaint.setTextSize(textSize);
		mIndexStrokeX = getRight() - mIndexRightPadding - mIndexWidth;
		mIndexTextX = mIndexStrokeX + (mIndexWidth - (int)textSize)/2 ;
		mIndexTextY = mIndexRectRadius;
		
		mTopSupportPoint1 = new Point(getRight(),0);
		mTopSupportPoint2 = new Point(getRight(),0); 
		mBottomSupportPoint1 = new Point(getRight(),0);
		mBottomSupportPoint2 = new Point(getRight(),0);
		mTopEndPoint = new Point(getRight(),0);
		mBottomEndPoint = new Point(getRight(),0);
		mPopCenter = new Point(getRight()+mFloatingIndexRadius,0);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//画边框
		mPaint.setColor(mIndexStrokeColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(3);
		RectF rect = new RectF(mIndexStrokeX, 0, mIndexStrokeX+mIndexWidth,2*mIndexRectRadius);
		canvas.drawArc(rect, 180, 180, false, mPaint);
		
		canvas.drawLine(mIndexStrokeX, mIndexRectRadius, mIndexStrokeX, getHeight()-mIndexRectRadius, mPaint);
		canvas.drawLine(mIndexStrokeX+mIndexWidth, mIndexRectRadius, mIndexStrokeX+mIndexWidth, getHeight()-mIndexRectRadius, mPaint);
		
		rect.set(mIndexStrokeX, getHeight() - 2*mIndexRectRadius, mIndexStrokeX+mIndexWidth, getHeight());
		canvas.drawArc(rect, 0, 180, false, mPaint);
		//画字符
		mPaint.setColor(mIndexTextColor);
		Log.i(TAG,"getHeight():"+getHeight()+",singleHeight:"+mSingleHeight+",rectRadius:"+mIndexRectRadius);
		for(int i =0 ;i<mAlphabets.length;i++){
			mIndexTextY = (i+1)*mSingleHeight+mIndexRectRadius-20;
			canvas.drawText(mAlphabets[i], mIndexTextX, mIndexTextY, mPaint);
		}
		
		//画画波浪
		mPaint.setColor(mPopBackColor);
		mPaint.setStyle(Paint.Style.FILL);
		Path path = new Path();
		path.moveTo(getRight(),mTopStartY);
		path.cubicTo(mTopSupportPoint1.x,mTopSupportPoint1.y, mTopSupportPoint2.x, mTopSupportPoint2.y, mTopEndPoint.x, mTopEndPoint.y);
		path.cubicTo(mBottomSupportPoint1.x,mBottomSupportPoint1.y,mBottomSupportPoint2.x,mBottomSupportPoint2.y,getRight(),mBottomStartY);
//		path.quadTo(mTopSupportPoint.x,mTopSupportPoint.y,mTopEndPoint.x,mTopEndPoint.y);
//		path.quadTo(mBottomSupportPoint.x,mBottomSupportPoint.y,getRight(),mBottomStartY);
//		path.moveTo(getRight(),mBottomStartY);
//		path.quadTo(mBottomSupportPoint.x,mBottomSupportPoint.y,mBottomEndPoint.x,mBottomEndPoint.y);
//		path.moveTo(getRight(),mBottomStartY);
		path.lineTo(getRight(),mTopStartY);
		path.close();
		canvas.drawPath(path, mPaint);
		//波浪上面的球体
		canvas.drawCircle(mPopCenter.x, mPopCenter.y, mFloatingIndexRadius, mPaint);
		//高亮字体
		for(int i =0 ;i<mAlphabets.length;i++){
			if(i == mChosedIndex){
				mIndexTextY = (i+1)*mSingleHeight+mIndexRectRadius-20;
				mPaint.setColor(mPopTextColor);
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setStrokeWidth(3);
				canvas.drawText(mAlphabets[i], mIndexTextX, mIndexTextY, mPaint);
			}
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				Log.i(TAG,"action_down,x:"+event.getX()+",y:"+event.getY());
				if(event.getX()<mIndexStrokeX)
					return super.dispatchTouchEvent(event);
				touchY = (int)event.getY();
				
				mTopStartY = touchY - (int)(2.5*mFloatingIndexRadius);
				mBottomStartY = touchY + (int)(2.5*mFloatingIndexRadius);
				mChosedIndex = getIndexByPosition(touchY);
				if(mListener!=null){
					mListener.onAlphbetTouch(mAlphabets[mChosedIndex]);
				}
				//动画效果
				performTouchDownAnim(event.getY());
				return true;
			case MotionEvent.ACTION_MOVE:
				Log.i(TAG,"action_move,x:"+event.getX()+",y:"+event.getY());
				//取消动画
				if(Math.abs(event.getY() - touchY)<30)
					cancelAnim();
				//获得当前index
				touchY = (int)event.getY();
				mTopStartY = touchY - 2*mFloatingIndexRadius;
				mBottomStartY = touchY + 2*mFloatingIndexRadius;
				//终点
				mTopEndPoint.x = mBottomEndPoint.x = (int)getRight()-(int)(1.1*mFloatingIndexRadius);
				mTopEndPoint.y = mBottomEndPoint.y = (int)touchY;
				//辅助点
				mTopSupportPoint1.y = (int)(touchY - mFloatingIndexRadius*1f);
				mTopSupportPoint1.x = getRight();
				mTopSupportPoint2.y = (int)(touchY - mFloatingIndexRadius*0.8f);
				mTopSupportPoint2.x = mTopEndPoint.x;
				mBottomSupportPoint1.y = (int)(touchY + mFloatingIndexRadius*0.8f);
				mBottomSupportPoint1.x =  mBottomEndPoint.x;
				mBottomSupportPoint2.y = (int)(touchY + mFloatingIndexRadius*1f);
				mBottomSupportPoint2.x = getRight();
				//球心
				mPopCenter.x = (int)(getRight() - 3.2 * mFloatingIndexRadius);
				mPopCenter.y = touchY;
				
				if(mListener!=null){
					mListener.onAlphbetTouch(mAlphabets[mChosedIndex]);
				}
				mChosedIndex = getIndexByPosition(touchY);
				invalidate();
				return true;
			case MotionEvent.ACTION_UP:
				return true;
			case MotionEvent.ACTION_CANCEL:
				return true;
			default:
				return false;
		}
		
	}
	
	

	public void performTouchDownAnim(final float position){
		
		waveAnim = ValueAnimator.ofInt(getRight(),getRight()-(int)(1.1*mFloatingIndexRadius));
		waveAnim.setInterpolator(new OvershootInterpolator(2.0f));
		waveAnim.addUpdateListener(new AnimatorUpdateListener() {
			//水波纹起来的动画
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				//终点
				mTopEndPoint.x = mBottomEndPoint.x = (Integer)animation.getAnimatedValue();
				mTopEndPoint.y = mBottomEndPoint.y = (int)position;
				//辅助点
				mTopSupportPoint1.y = (int)(position - mFloatingIndexRadius*1f);
				mTopSupportPoint1.x = getRight();
				mTopSupportPoint2.y = (int)(position - mFloatingIndexRadius*0.8f);
				mTopSupportPoint2.x = mTopEndPoint.x;
				mBottomSupportPoint1.y = (int)(position + mFloatingIndexRadius*0.8f);
				mBottomSupportPoint1.x = (Integer)animation.getAnimatedValue();
				mBottomSupportPoint2.y = (int)(position + mFloatingIndexRadius*1f);
				mBottomSupportPoint2.x = getRight();
				invalidate();
			}
		});
		
		ballAnim = ValueAnimator.ofFloat(-1f,3.2f);
		ballAnim.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mPopCenter.x = getRight() - (int)((Float)animation.getAnimatedValue()*mFloatingIndexRadius);
				mPopCenter.y = (int)position;
			}
		});
		
		waveAnim.setDuration(1000);
		waveAnim.start();
		ballAnim.setDuration(1000);
		ballAnim.start();
	}
	
	
	private void cancelAnim() {
		if(waveAnim != null)
			waveAnim.cancel();
		if(ballAnim != null)
			ballAnim.cancel();
	}
	
	private int getIndexByPosition(int positionY){
		int index = (int)((positionY - mIndexRectRadius)/mSingleHeight);
		if (index>mAlphabets.length -1 )
			index = mAlphabets.length -1 ;
		if(index<0)
			index = 0;
		return index;
	}
	
	//给外部的回调
	public interface OnAlphbetTouchListener{
		public void onAlphbetTouch(String alphbet);
	}
	public void setOnAlphbetTouchListener(OnAlphbetTouchListener l){
		mListener = l;
	}
	
}
