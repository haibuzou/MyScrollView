package com.haibuzou.myscrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.OverScroller;

/**
 * 使用自定义触摸处理的ScrollView
 * Created by Administrator on 2015/8/13.
 */
public class MyScrollView extends FrameLayout {

    //处理滑动的Scroller 这里如果是api 9以上最好使用OverScroller
    //Scroller的速滑效果很差
    private OverScroller mScroller;
    //判断滑动速度
    private VelocityTracker mVelocityTracker;
    //滑动的阀值
    private int mTouchSlop;
    //滑动速度
    private int mMaxVelocity, mMinVelocity;
    //滑动锁
    private boolean mDragging = false;

    //上一次移动事件的位置
    private float mLastX, mLastY;


    public MyScrollView(Context context) {
        super(context);
        init(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        mScroller = new OverScroller(context);
        mVelocityTracker = VelocityTracker.obtain();
        //获取系统的触摸阀值
        //可以认为用户是在滑动的距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //滑动的最快速度
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        //滑动的最慢速度
        mMinVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
    }

    //这里的方案是不测量保证视图尽可能按自己的大小来 如果不复写父视图的默认方案会强制子视图和父视图一样大 也就是按父视图的方案来实现
    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    //与上面一样
    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.leftMargin + lp.rightMargin, MeasureSpec.UNSPECIFIED);
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    //computeScroll会被定期调用 判断滑动状态
    @Override
    public void computeScroll() {
        //判断滑动状态 返回true表示没有完成
        //使用这个方法保证滑动动画的完成
        if (mScroller.computeScrollOffset()) {
            int oldx = getScrollX();
            int oldy = getScrollY();
            //现在滚动到的x的位置
            int x = mScroller.getCurrX();
            //现在滚总到的y位置
            int y = mScroller.getCurrY();

            if (getChildCount() > 0) {
                View child = getChildAt(0);
                x = clamp(x, getWidth() - getPaddingLeft() - getPaddingRight(), child.getWidth());
                y = clamp(y, getHeight() - getPaddingTop() - getPaddingBottom(), child.getHeight());
                if (x != oldx || y != oldy) {
                    scrollTo(x, y);
                }
            }
            //滑动完成之前一直绘制 就是保证这个方法还会进来
            postInvalidate();
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        //依赖View.ScrollBy方法调用ScrollTo
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            //边界检查
            x = clamp(x, getWidth() - getPaddingLeft() - getPaddingRight(), child.getWidth());
            y = clamp(y, getHeight() - getPaddingTop() - getPaddingBottom(), child.getHeight());
            //如果x== getScrollX()滚动已经完成??
            if (x != getScrollX() || y != getScrollY()) {
                super.scrollTo(x, y);
            }
        }

    }

    //处理快速滑动的方法 参数是滑动速度
    public void fling(int VelocityX, int VelocityY) {
        if (getChildCount() > 0) {
            int height = getHeight() - getPaddingTop() - getPaddingBottom();
            int width = getWidth() - getPaddingLeft() - getPaddingRight();
            int bottom = getChildAt(0).getHeight();
            int right = getChildAt(0).getWidth();

            mScroller.fling(getScrollX(), getScrollY(), VelocityX, VelocityY, 0, Math.max(0, right - width), 0, Math.max(0, bottom - height));
            invalidate();
        }
    }

    //辅助方法判断是否超过边界
    private int clamp(int n, int my, int child) {
        //子View小于父视图或者滑动小于0 不滑动
        if (my >= child || n < 0) {
            return 0;
        }
        //滚动超过了子View的边界,直接滑到边界
        if ((my + n) > child) {
            return child - my;
        }
        return n;
    }

    //监控传递给子视图的触摸事件 一旦进行拖拽就拦截
    //如果子视图是可交互的，允许子视图接收事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:
                //终止正在进行的滑动
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                //还原速度追踪器
                mVelocityTracker.clear();
                mVelocityTracker.addMovement(ev);
                //保存初始触点
                mLastX = ev.getX();
                mLastY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float x = ev.getX();
                final float y = ev.getY();
                final int DiffX = (int) Math.abs(x - mLastX);
                final int DiffY = (int) Math.abs(y - mLastY);
                //检查x或者Y方向是否达到了滑动的阀值
                if (DiffX > mTouchSlop || DiffY > mTouchSlop) {
                    mDragging = true;
                    mVelocityTracker.addMovement(ev);
                    //开始自己捕捉触摸事件
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mDragging = false;
                mVelocityTracker.clear();
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //这里所有的事件都会交给检测器
        mVelocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //已经保留了初始的触点，如果这里不返回true，后续的触摸事件就不会再传递
                return true;
            case MotionEvent.ACTION_MOVE:
                final float x = event.getX();
                final float y = event.getY();
                final float DeltaX = mLastX - x;
                final float DeltaY = mLastY- y;
                //判断阀值
                if ((Math.abs(DeltaX) > mTouchSlop || Math.abs(DeltaY) > mTouchSlop) && !mDragging) {
                    mDragging = true;
                }
                if(mDragging){
                    //滚动视图
                    scrollBy((int)DeltaX,(int)DeltaY);
                    //更新坐标
                    mLastX = x;
                    mLastY = y;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                //终止滑动
                mDragging = false;
                if(!mScroller.isFinished()){
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_UP:
                mDragging = false;
                //处理快速滑动的情况
                mVelocityTracker.computeCurrentVelocity(1000,mMaxVelocity);
                final int VelocityX = (int)mVelocityTracker.getXVelocity();
                final int VelocityY = (int)mVelocityTracker.getYVelocity();
                if(Math.abs(VelocityX) > mMinVelocity || Math.abs(VelocityY) > mMinVelocity){
                    //为什么要取负值？ 因为滑动的时候 正值是向上滑动 负值是向下滑动
                    fling(-VelocityX,-VelocityY);
                }
                break;
        }

        return super.onTouchEvent(event);
    }
}
