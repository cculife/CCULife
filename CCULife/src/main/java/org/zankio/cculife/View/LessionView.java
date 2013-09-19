package org.zankio.cculife.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.zankio.cculife.R;

import java.util.Calendar;

public class LessionView extends View {


    private Calendar mStartTime;
    private Calendar mEndTime;

    private String mClassName;
    private String mClassRoom;
    private String mClassTeacher;

    private boolean isShowName;
    private boolean isShowRoom;
    private boolean isShowTeacher;
    private boolean isShowStartTime;
    private boolean isShowEndTime;

    private RectF mBound = new RectF();

    private int mBackgroundColor = 0xFFFFBB33;
    private int mTextColor = 0xFF000000;
    private Paint mBackgroundPaint;
    private Paint mTextPaint;

    private float mClassNameX = 0.0f;
    private float mClassNameY = 0.0f;
    private float mClassNameWidth = 0.0f;
    private float mClassNameHeight = 0.0f;

    public LessionView(Context context) {
        super(context);
        init();
    }

    public LessionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LessionView,
                0, 0
        );

        try {
            assert a != null;
            mClassName = a.getString(R.styleable.LessionView_ClassName);
            mTextColor = a.getColor(R.styleable.LessionView_TextColor, 0xFF000000);
            mBackgroundColor = a.getColor(R.styleable.LessionView_BackgroundColor, 0xFFFFBB33);
        } finally {
            a.recycle();
        }

        init();
    }


    private void init() {
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(20);
        measureTextSize();

    }

    public void measureTextSize(){
        if(mClassName != null) {
            Rect rect = new Rect();
            mTextPaint.getTextBounds(mClassName, 0, mClassName.length(), rect);
            mClassNameHeight = rect.height();
            mClassNameWidth = rect.width();
            mClassNameX = mBound.centerX() - mClassNameWidth / 2.0f;
            mClassNameY = mBound.centerY();
        }
    }

    public void setClassName(String className) {
        mClassName = className;
        measureTextSize();
        invalidate();
    }

    public void setClassRoom(String classRoom) {
        mClassRoom = classRoom;
        invalidate();
    }

    public void setClassTeacher(String classTeacher) {
        mClassTeacher = classTeacher;
        invalidate();
    }

    public void setStartTime(Calendar startTime) {
        mStartTime = startTime;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        mBound = new RectF(0.0f, 0.0f, w, h);

        mClassNameX = mBound.centerX() - mClassNameWidth / 2.0f;
        mClassNameY = mBound.centerY();
    }


    // ToDo 換行
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mClassName != null)
            canvas.drawText(mClassName, mClassNameX, mClassNameY, mTextPaint);

    }
}
