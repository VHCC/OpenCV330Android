package acl.siot.opencvwpc20191007noc.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;

import acl.siot.opencvwpc20191007noc.R;
import androidx.viewpager.widget.ViewPager;


/**
 * Created by TsungMu on 2016/7/1.
 */
public class SwipeableViewPager extends ViewPager {

    protected boolean isSwipeable;

    public SwipeableViewPager(Context context) {
        super(context);
        setSwipeable(true);
    }

    public SwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typeAttr = context.obtainStyledAttributes(attrs, R.styleable.SwipeableViewPager);
        try {
            setSwipeable(typeAttr.getBoolean(R.styleable.SwipeableViewPager_swipeable, false));
        } finally {
            typeAttr.recycle();
        }
    }

    /** Set to enable or disable swipe. **/
    public void setSwipeable(boolean flag) {
        isSwipeable = flag;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isSwipeable ? super.onInterceptTouchEvent(ev) : false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isSwipeable ? super.onTouchEvent(ev) : false;
    }
}
