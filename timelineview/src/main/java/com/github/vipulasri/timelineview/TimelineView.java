package com.github.vipulasri.timelineview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Vipul Asri on 05-12-2015.
 */
public class TimelineView extends View {

    public static final String TAG = TimelineView.class.getSimpleName();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LineOrientation.HORIZONTAL, LineOrientation.VERTICAL})
    public @interface LineOrientation {
        int HORIZONTAL = 0;
        int VERTICAL = 1;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LineType.NORMAL, LineType.START, LineType.END, LineType.ONLYONE})
    public @interface LineType {
        int NORMAL = 0;
        int START = 1;
        int END = 2;
        int ONLYONE = 3;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LineStyle.NORMAL, LineStyle.DASHED})
    public @interface LineStyle {
        int NORMAL = 0;
        int DASHED = 1;
    }

    private Drawable mMarker;
    private int mMarkerSize;
    private boolean mMarkerInCenter;
    private Paint mLinePaint = new Paint();
    private boolean mDrawStartLine = false;
    private boolean mDrawEndLine = false;
    private float mStartLineStartX, mStartLineStartY, mStartLineStopX, mStartLineStopY;
    private float mEndLineStartX, mEndLineStartY, mEndLineStopX, mEndLineStopY;
    private int mStartLineColor;
    private int mEndLineColor;
    private int mLineSize;
    private int mLineOrientation;
    private int mLineStyle;
    private int mLineStyleDashLength;
    private int mLineStyleDashGap;
    private int mLinePadding;

    private Rect mBounds;

    public TimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs,R.styleable.TimelineView);
        mMarker = typedArray.getDrawable(R.styleable.TimelineView_marker);
        mMarkerSize = typedArray.getDimensionPixelSize(R.styleable.TimelineView_markerSize, Utils.dpToPx(20, getContext()));
        mMarkerInCenter = typedArray.getBoolean(R.styleable.TimelineView_markerInCenter, true);
        mStartLineColor = typedArray.getColor(R.styleable.TimelineView_startLineColor, getResources().getColor(android.R.color.darker_gray));
        mEndLineColor = typedArray.getColor(R.styleable.TimelineView_endLineColor, getResources().getColor(android.R.color.darker_gray));
        mLineSize = typedArray.getDimensionPixelSize(R.styleable.TimelineView_lineSize, Utils.dpToPx(2, getContext()));
        mLineOrientation = typedArray.getInt(R.styleable.TimelineView_lineOrientation, LineOrientation.VERTICAL);
        mLinePadding = typedArray.getDimensionPixelSize(R.styleable.TimelineView_linePadding, 0);
        mLineStyle = typedArray.getInt(R.styleable.TimelineView_lineStyle, LineStyle.NORMAL);
        mLineStyleDashLength = typedArray.getDimensionPixelSize(R.styleable.TimelineView_lineStyleDashLength, Utils.dpToPx(8f, getContext()));
        mLineStyleDashGap = typedArray.getDimensionPixelSize(R.styleable.TimelineView_lineStyleDashGap, Utils.dpToPx(4f, getContext()));
        typedArray.recycle();

        if(isInEditMode()) {
            mDrawStartLine = true;
            mDrawEndLine = true;
        }

        if(mMarker == null) {
            mMarker = getResources().getDrawable(R.drawable.marker);
        }

        mLinePaint.setAlpha(0);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(mStartLineColor);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mLineSize);

        if (mLineStyle == LineStyle.DASHED)
            mLinePaint.setPathEffect(new DashPathEffect(new float[]{(float) mLineStyleDashLength, (float) mLineStyleDashGap}, 0.0f));
        else
            mLinePaint.setPathEffect(new PathEffect());

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //Width measurements of the width and height and the inside view of child controls
        int w = mMarkerSize + getPaddingLeft() + getPaddingRight();
        int h = mMarkerSize + getPaddingTop() + getPaddingBottom();

        // Width and height to determine the final view through a systematic approach to decision-making
        int widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
        int heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);

        setMeasuredDimension(widthSize, heightSize);
        initDrawable();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // When the view is displayed when the callback
        // Positioning Drawable coordinates, then draw
        initDrawable();
    }

    private void initDrawable() {
        int pLeft = getPaddingLeft();
        int pRight = getPaddingRight();
        int pTop = getPaddingTop();
        int pBottom = getPaddingBottom();

        int width = getWidth();// Width of current custom view
        int height = getHeight();

        int cWidth = width - pLeft - pRight;// Circle width
        int cHeight = height - pTop - pBottom;

        int markSize = Math.min(mMarkerSize, Math.min(cWidth, cHeight));

        if(mMarkerInCenter) { //Marker in center is true

            if(mMarker != null) {
                mMarker.setBounds((width/2) - (markSize/2),(height/2) - (markSize/2), (width/2) + (markSize/2),(height/2) + (markSize/2));
                mBounds = mMarker.getBounds();
            }

        } else { //Marker in center is false

            if(mMarker != null) {
                mMarker.setBounds(pLeft,pTop,pLeft + markSize,pTop + markSize);
                mBounds = mMarker.getBounds();
            }
        }

        if(mLineOrientation == LineOrientation.HORIZONTAL) {

            if(mDrawStartLine) {
                mStartLineStartX = pLeft;
                mStartLineStartY = mBounds.centerY();
                mStartLineStopX = mBounds.left;
                mStartLineStopY = mBounds.centerY();
            }

            if(mDrawEndLine) {
                mEndLineStartX = mBounds.right;
                mEndLineStartY = mBounds.centerY();
                mEndLineStopX = getWidth();
                mEndLineStopY = mBounds.centerY();
            }
        } else {

            if(mDrawStartLine) {
                mStartLineStartX = mBounds.centerX();

                if(mLineStyle == LineStyle.DASHED) {
                    mStartLineStartY = pTop - mLineStyleDashLength;
                } else {
                    mStartLineStartY = pTop;
                }

                mStartLineStopX = mBounds.centerX();
                mStartLineStopY = mBounds.top;
            }

            if(mDrawEndLine) {
                mEndLineStartX = mBounds.centerX();
                mEndLineStartY = mBounds.bottom;
                mEndLineStopX = mBounds.centerX();
                mEndLineStopY = getHeight();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mMarker != null) {
            mMarker.draw(canvas);
        }

        if(mDrawStartLine) {
            canvas.drawLine(mStartLineStartX, mStartLineStartY, mStartLineStopX, mStartLineStopY, mLinePaint);
        }

        if(mDrawEndLine) {
            canvas.drawLine(mEndLineStartX, mEndLineStartY, mEndLineStopX, mEndLineStopY, mLinePaint);
        }
    }

    /**
     * Sets marker.
     *
     * @param marker will set marker drawable to timeline
     */
    public void setMarker(Drawable marker) {
        mMarker = marker;
        initDrawable();
    }

    /**
     * Sets marker.
     *
     * @param marker will set marker drawable to timeline
     * @param color  with a color
     */
    public void setMarker(Drawable marker, int color) {
        mMarker = marker;
        mMarker.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        initDrawable();
    }

    /**
     * Sets marker color.
     *
     * @param color the color
     */
    public void setMarkerColor(int color) {
        mMarker.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        initDrawable();
    }

    /**
     * Sets start line.
     *
     * @param color    the color
     * @param viewType the view type
     */
    public void setStartLine(int color, int viewType) {
        //mStartLine = new ColorDrawable(color);
        initLine(viewType);
    }

    /**
     * Sets end line.
     *
     * @param color    the color
     * @param viewType the view type
     */
    public void setEndLine(int color, int viewType) {
        //mEndLine = new ColorDrawable(color);
        initLine(viewType);
    }

    /**
     * Sets marker size.
     *
     * @param markerSize the marker size
     */
    public void setMarkerSize(int markerSize) {
        mMarkerSize = markerSize;
        initDrawable();
    }

    /**
     * Sets line size.
     *
     * @param lineSize the line size
     */
    public void setLineSize(int lineSize) {
        mLineSize = lineSize;
        initDrawable();
    }

    /**
     * Sets line padding
     * @param padding the line padding
     */
    public void setLinePadding(int padding) {
        mLinePadding = padding;
        initDrawable();
    }

    private void setStartLine(boolean show) {
        mDrawStartLine = show;
        initDrawable();
    }

    private void setEndLine(boolean show) {
        mDrawEndLine = show;
        initDrawable();
    }

    /**
     * Init line.
     *
     * @param viewType the view type
     */
    public void initLine(int viewType) {
        if(viewType == LineType.START) {
            setStartLine(false);
            setEndLine(true);
        } else if(viewType == LineType.END) {
            setStartLine(true);
            setEndLine(false);
        } else if(viewType == LineType.ONLYONE) {
            setStartLine(false);
            setEndLine(false);
        } else {
            setStartLine(true);
            setEndLine(true);
        }

        initDrawable();
    }

    /**
     * Gets timeline view type.
     *
     * @param position   the position
     * @param total_size the total size
     * @return the time line view type
     */
    public static int getTimeLineViewType(int position, int total_size) {
        if(total_size == 1) {
            return LineType.ONLYONE;
        } else if(position == 0) {
            return LineType.START;
        } else if(position == total_size - 1) {
            return LineType.END;
        } else {
            return LineType.NORMAL;
        }
    }
}
