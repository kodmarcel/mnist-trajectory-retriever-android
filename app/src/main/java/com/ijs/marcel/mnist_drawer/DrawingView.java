package com.ijs.marcel.mnist_drawer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.sql.Time;

/**
 * Created by marcel on 11/7/17.
 */

public class DrawingView extends android.support.v7.widget.AppCompatImageView {

    private Bitmap mBitmap;


    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Context context;
    private Paint circlePaint;
    private Path circlePath;
    private Paint mPaint;
    private Trajectory trajectory;
    private long touchStartTime;

    public DrawingView(Context c, AttributeSet attrs) {
        super(c, attrs);
        Log.w("View","Creating view");
        context = c;
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        setWillNotDraw(false);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.w("View","Drawing");
        super.onDraw(canvas);
        Log.w("Bitmap:", String.valueOf(mBitmap));
        if(mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            Log.w("View","bitmap drawn");
        }
        canvas.drawPath(mPath, mPaint);
        canvas.drawPath(circlePath, circlePaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        touchStartTime = System.currentTimeMillis();
        trajectory.addPoint(x,y, 0);
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            trajectory.addPoint(x,y,System.currentTimeMillis() - touchStartTime);
            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void touch_up() {
        Log.w("View","Touch up");
        mPath.lineTo(mX, mY);
        circlePath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void setmBitmap(Bitmap bitmap){
        Log.w("View","setting bitmap");
        trajectory = new Trajectory();
        mBitmap = bitmap;
        mPath.reset();
        invalidate();
    }

    public Trajectory retrievePath() {
        return trajectory;
    }
}