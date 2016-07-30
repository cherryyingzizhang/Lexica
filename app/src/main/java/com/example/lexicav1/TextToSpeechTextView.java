package com.example.lexicav1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by Cherry_Zhang on 16-06-04.
 */
public class TextToSpeechTextView extends TextView
{
    private String touchedString;
    private int startIndexTouchedString, endIndexTouchedString;
    private boolean drawSelectedTextRect;

    private GestureDetector singleTapDetector;

    private OnWordClickedListener onWordClickedListener;
    public interface OnWordClickedListener
    {
        void onWordClicked(String string);
    }

    public TextToSpeechTextView(Context context)
    {
        super(context);
        singleTapDetector = new GestureDetector(context, new SingleTapConfirm());
    }

    public TextToSpeechTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        singleTapDetector = new GestureDetector(context, new SingleTapConfirm());
    }

    public TextToSpeechTextView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        singleTapDetector = new GestureDetector(context, new SingleTapConfirm());
    }

    public void setOnWordClickedListener(OnWordClickedListener onWordClickedListener) {
        this.onWordClickedListener = onWordClickedListener;
    }

    public String getTouchedString()
    {
        return touchedString;
    }

    public void clearSelectedTextRect()
    {
        drawSelectedTextRect = false;
        this.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        singleTapDetector.onTouchEvent(event);
        return true;
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            drawSelectedTextRect = true;
            int offset = getOffsetForPosition(event.getX(), event.getY());
            updateTouchedStringFromOffset(offset);
            TextToSpeechTextView.this.invalidate();
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (drawSelectedTextRect)
        {
            drawRectOnSelectedText(canvas);
        }
    }

    private void drawRectOnSelectedText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLUE);

        // Initialize global value
        TextView parentTextView = this;
        Rect parentTextViewRect = new Rect();

        // Initialize values for the computing of clickedText position
        Layout textViewLayout = parentTextView.getLayout();

        double startXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal(startIndexTouchedString);
        double endXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal(endIndexTouchedString);

        // Get the rectangle of the clicked text
        int currentLineStartOffset = textViewLayout.getLineForOffset(startIndexTouchedString);
        int currentLineEndOffset = textViewLayout.getLineForOffset(endIndexTouchedString);
        boolean keywordIsInMultiLine = currentLineStartOffset != currentLineEndOffset;
        textViewLayout.getLineBounds(currentLineStartOffset, parentTextViewRect);

        // Update the rectangle position to his real position on screen
        int[] parentTextViewLocation = {0,0};
        parentTextView.getLocationOnScreen(parentTextViewLocation);

        double parentTextViewTopAndBottomOffset = (
                //parentTextViewLocation[1] -
                parentTextView.getScrollY() +
                        parentTextView.getCompoundPaddingTop()
        );

        parentTextViewRect.top += parentTextViewTopAndBottomOffset;
        parentTextViewRect.bottom += parentTextViewTopAndBottomOffset;

        // In the case of multi line text, we have to choose what rectangle take
        if (keywordIsInMultiLine){

            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();

            int screenHeight = display.getHeight();
            int dyTop = parentTextViewRect.top;
            int dyBottom = screenHeight - parentTextViewRect.bottom;
            boolean onTop = dyTop > dyBottom;

            if (onTop){
                endXCoordinatesOfClickedText = textViewLayout.getLineRight(currentLineStartOffset);
            }
            else{
                parentTextViewRect = new Rect();
                textViewLayout.getLineBounds(currentLineEndOffset, parentTextViewRect);
                parentTextViewRect.top += parentTextViewTopAndBottomOffset;
                parentTextViewRect.bottom += parentTextViewTopAndBottomOffset;
                startXCoordinatesOfClickedText = textViewLayout.getLineLeft(currentLineEndOffset);
            }

        }

        parentTextViewRect.left += (
//                parentTextViewLocation[0] +
                        startXCoordinatesOfClickedText +
                        parentTextView.getCompoundPaddingLeft() -
                        parentTextView.getScrollX()
        );
        parentTextViewRect.right = (int) (
                parentTextViewRect.left +
                        endXCoordinatesOfClickedText -
                        startXCoordinatesOfClickedText
        );

        canvas.drawRect(parentTextViewRect, paint);
    }

    /////////////////////////////////////////////////////////////
    //START OF Methods for getting offset and word at offset of clicked position///
    ///////////////////////////////////////////////////////////
    public void updateTouchedStringFromOffset(int offset)
    {
        String text = this.getText().toString();
        String[] words = text.split("\\s+");
        int currIndexInText = 0;

        for (int i = 0; i < words.length; i++) {
            int beginningIndexOfCurrWord = text.indexOf(words[i], currIndexInText);
            currIndexInText += (beginningIndexOfCurrWord-currIndexInText) + words[i].length();
            if (beginningIndexOfCurrWord <= offset && offset <= currIndexInText)
            {
                this.touchedString = words[i];
                this.startIndexTouchedString = beginningIndexOfCurrWord;
                this.endIndexTouchedString = currIndexInText;
                this.onWordClickedListener.onWordClicked(this.touchedString);
                break; //break out of for-loop
            }
        }
    }

    public int getOffsetForPosition(float x, float y) {
        if (this.getLayout() == null) {
            return -1;
        }
        final int line = getLineAtCoordinate(y);
        final int offset = getOffsetAtCoordinate(line, x);
        return offset;
    }

    private int getOffsetAtCoordinate(int line, float x) {
        x = convertToLocalHorizontalCoordinate(x);
        return this.getLayout().getOffsetForHorizontal(line, x);
    }

    private float convertToLocalHorizontalCoordinate(float x) {
        x -= this.getTotalPaddingLeft();
        // Clamp the position to inside of the view.
        x = Math.max(0.0f, x);
        x = Math.min(this.getWidth() - this.getTotalPaddingRight() - 1, x);
        x += this.getScrollX();
        return x;
    }

    private int getLineAtCoordinate(float y) {
        y -= this.getTotalPaddingTop();
        // Clamp the position to inside of the view.
        y = Math.max(0.0f, y);
        y = Math.min(this.getHeight() - this.getTotalPaddingBottom() - 1, y);
        y += this.getScrollY();
        return this.getLayout().getLineForVertical((int) y);
    }
    //////////////////////////////////////////////////////////
    //END OF Methods for getting offset of clicked position//
    ////////////////////////////////////////////////////////
}
