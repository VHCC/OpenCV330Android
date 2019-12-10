package acl.siot.opencvwpc20191007noc.view.overLay;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Created by IChen.Chu on 2018/10/31
 */
public class OverLayLinearLayout extends LinearLayout {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());


    private int frameType = 0;
    private Bitmap bitmap;

    private final int BOUND_WIDTH = 8;

    public OverLayLinearLayout(Context context) {
        super(context);
        mLog.d(TAG, "OverLayLinearLayout 1");
    }

    public OverLayLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mLog.d(TAG, "OverLayLinearLayout 2");
        TypedArray typeAttr = context.obtainStyledAttributes(attrs, R.styleable.OverLayLinearLayout);
        try {
            frameType = typeAttr.getInt(R.styleable.OverLayLinearLayout_frame_color, 0);
        } finally {
            typeAttr.recycle();
        }
    }

    public OverLayLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLog.d(TAG, "OverLayLinearLayout 3");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public OverLayLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mLog.d(TAG, "OverLayLinearLayout 4");

    }

    protected void createQRCodeFrame() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mainCanvas = new Canvas(bitmap);
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;
        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.qrCodeScanLayer));
        paint.setAlpha(100);
        mainCanvas.drawRect(outerRectangle, paint);

        // Blue Rect
        Paint paintBlue = new Paint();
        paintBlue.setColor(Color.BLUE);
        RectF rectangleBlue1 = new RectF(centerX - 210, centerY - 410, centerX - 180, centerY - 380);
        mainCanvas.drawRect(rectangleBlue1, paintBlue);
        RectF rectangleBlue2 = new RectF(centerX + 210, centerY - 410, centerX + 180, centerY - 380);
        mainCanvas.drawRect(rectangleBlue2, paintBlue);
        RectF rectangleBlue3 = new RectF(centerX - 210, centerY - 20, centerX - 180, centerY + 10);
        mainCanvas.drawRect(rectangleBlue3, paintBlue);
        RectF rectangleBlue4 = new RectF(centerX + 210, centerY - 20, centerX + 180, centerY + 10);
        mainCanvas.drawRect(rectangleBlue4, paintBlue);

        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

//        float radius = getResources().getDimensionPixelSize(R.dimen.common_corner_radius);
//        mainCanvas.drawCircle(centerX, centerY, radius, paint);
        RectF rectangle = new RectF(centerX - 200,centerY - 400,centerX + 200, centerY);
        mainCanvas.drawRect(rectangle, paint);
    }

    protected void createDetectFrame() {
        mLog.d(TAG, "createDetectFrame, frameType= " + frameType);
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mainCanvas = new Canvas(bitmap);
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;
        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.qrCodeScanLayer));
        paint.setAlpha(100);
        mainCanvas.drawRect(outerRectangle, paint);

        // Blue Rect
        Paint paintFrame = new Paint();
        switch (frameType) {
            case 0:
                paintFrame.setColor(getResources().getColor(R.color.qrScanBoundaryLayer_blue));
                break;
            case 1:
                paintFrame.setColor(getResources().getColor(R.color.qrScanBoundaryLayer_red));
                break;
            case 2:
                paintFrame.setColor(getResources().getColor(R.color.qrScanBoundaryLayer_green));
                break;
        }
//        paintFrame.setColor(getResources().getColor(R.color.qrScanBoundaryLayer_blue));
        RectF rectangleRed = new RectF(0, 0, getWidth(), getHeight());
        mainCanvas.drawRect(rectangleRed, paintFrame);

        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

        RectF rectangle = new RectF(0 + BOUND_WIDTH, 0 + BOUND_WIDTH, getWidth() - BOUND_WIDTH, getHeight() - BOUND_WIDTH);
        mainCanvas.drawRect(rectangle, paint);
    }

    @Override
    public boolean isInEditMode() {
        mLog.d(TAG, "isInEditMode");
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mLog.d(TAG, "onLayout");
        super.onLayout(changed, l, t, r, b);
        bitmap = null;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mLog.d(TAG, "dispatchDraw");
        super.dispatchDraw(canvas);
        if (bitmap == null) {
//            createQRCodeFrame();
            createDetectFrame();
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
    }
}
