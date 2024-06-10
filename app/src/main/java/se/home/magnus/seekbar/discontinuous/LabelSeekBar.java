package se.home.magnus.seekbar.discontinuous;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

import se.home.magnus.seekbar.R;

/**
 * This class is a seek bar displaying string labels.
 */
public class LabelSeekBar extends AppCompatSeekBar {

    /**
     * Tells whether or not the left and right margin of this seek bar is adjusted.
     */
    private boolean _isMarginAdjusted;

    /**
     * Tells whether or not the seek bar change listener is set. This is a solution to make the
     * "seek bar change listener dependency" mandatory. If the seek bar change listener isn't set
     * (when needed) an exception is thrown. The "motivation" for this solution is that it isn't
     * possible to use "constructor dependency injection".
     */
    private boolean _isSeekBarChangeListenerSet;

    /**
     * The current progress value (divided by the value increment, i.e. the position index in the
     * "labels array").
     */
    private int _value;

    /**
     * Attribute controlling the amount to increment or decrement the seek bar value when the user
     * moves the thumb.
     */
    private int _valueIncrement;

    /**
     * A "mandatory" progress change listener.
     */
    private OnProgressListener _progressListener;

    /**
     * The context this seek bar is running in, through which it can access the current theme,
     * resources, etc.
     */
    private final Context _context;

    /**
     * The labels of this seek bar.
     */
    private CharSequence[] _labelArray;

    /**
     * The label views of this seek bar.
     */
    private final List<TextView> _labelViewList;

    /**
     * @param context      the context this seek bar is running in
     * @param attributeSet the attributes of the XML tag that is inflating this seek bar, which may
     *                     be null
     *
     * @throws IllegalArgumentException
     * @noinspection RedundantSuppression
     */
    @SuppressWarnings("JavaDoc")
    public LabelSeekBar(@NonNull Context context, @NonNull AttributeSet attributeSet) throws IllegalArgumentException {
        super(context, attributeSet);
        LayerDrawable progress;
        if ((progress = (LayerDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.label_seek_bar, null)) == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.label_seek_bar_constructor_error));
        }
        _isMarginAdjusted = _isSeekBarChangeListenerSet = false;
        _context = context;
        _labelViewList = new ArrayList<>();
        for (int i = 0; i < progress.getNumberOfLayers(); i++) {
            progress.setLayerHeight(i, 10);
        }
        // NOTE that this seek bar is listening on its "super class" returning integer values
        // between 0 and 100 and these values are forwarded to the listener of this seek bar
        // as position indices in the "labels array"
        setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            /**
             * Notification that the value has changed. Clients can use the fromUser parameter
             * to distinguish user-initiated changes from those that occurred programmatically.
             *
             * @param seekBar         the SeekBar whose value has changed
             * @param progressValue   the current progressValue, this will be in the range 0 and 100
             * @param fromUser        true if the value change was initiated by the user
             */
            @Override
            public void onProgressChanged(@NonNull SeekBar seekBar, int progressValue, boolean fromUser) {
            }

            /**
             * Notification that the user has started a touch gesture. Clients may want to use this to
             * disable advancing the SeekBar.
             *
             * @param seekBar the SeekBar in which the touch gesture began
             */
            @Override
            public void onStartTrackingTouch(@NonNull SeekBar seekBar) {
            }

            /**
             * Notification that the user has finished a touch gesture. Clients may want to use this to
             * re-enable advancing the SeekBar.
             *
             * @param seekBar the SeekBar in which the touch gesture began
             */
            @Override
            public void onStopTrackingTouch(@NonNull SeekBar seekBar) {
                int value;
                if (_isSeekBarChangeListenerSet) {
                    value = getLabelIndex(seekBar.getProgress());
                    setProgress(value * _valueIncrement);
                    _progressListener.onChanged(value);
                    _value = value;
                    setLabelColors();
                }
            }
        });
    }

    /**
     * Initializes this seek bar.
     *
     * @param labelArray an array of labels
     * @param listener   a progress listener
     * @param thumbColor a thumb color
     * @param color      a progress color
     * @param size       a thickness (height) of the seek bar (in pixels)
     * @param diameter   a thumb diameter (in pixels)
     *
     * @throws IllegalArgumentException
     * @noinspection RedundantSuppression
     */
    @SuppressWarnings("JavaDoc")
    public void initialize(@NonNull CharSequence[] labelArray, @NonNull LabelSeekBar.OnProgressListener listener, @ColorInt int thumbColor, @ColorInt int color, int size, int diameter) throws IllegalArgumentException {
        ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
        LayerDrawable progress = (LayerDrawable) getProgressDrawable();
        thumb.setIntrinsicWidth(diameter);
        thumb.setIntrinsicHeight(diameter);
        ((GradientDrawable) progress.findDrawableByLayerId(R.id.background)).setCornerRadius(size);
        for (int i = 0; i < progress.getNumberOfLayers(); i++) {
            progress.setLayerHeight(i, size);
        }
        setLabelColors();
        getProgressDrawable().setTint(color);
        thumb.setTint(thumbColor);
        setThumb(thumb);
        _isSeekBarChangeListenerSet = true;
        _progressListener = listener;
        if (labelArray.length < 2) {
            throw new IllegalArgumentException(_context.getString(R.string.label_seek_bar_initiation_error));
        } else {
            _valueIncrement = Math.round(100f / (labelArray.length - 1));
            _labelArray = labelArray;
        }
    }

    /**
     * Sets the value which should be a position index in the "label array".
     *
     * @param value a value
     *
     * @throws IllegalArgumentException
     * @noinspection JavadocDeclaration, RedundantSuppression
     */
    public void setValue(int value) throws IllegalArgumentException {
        if (value < 0 || value > _labelArray.length - 1) {
            throw new IllegalArgumentException(getContext().getString(R.string.label_seek_bar_value_error));
        }
        setProgress(value * _valueIncrement);
        _value = value;
        setLabelColors();
    }

    /**
     * Draws this view and adjusts the margins if the haven't been adjusted.
     *
     * @param canvas the canvas on which the this seek bar will be drawn
     */
    @Override
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int labelViewWidth;
        LinearLayout.LayoutParams layoutParameters;
        if (!_isMarginAdjusted) {
            if (!_labelViewList.isEmpty()) {
                labelViewWidth = Math.round(_labelViewList.get(0).getWidth() / 2f);
                layoutParameters = (LinearLayout.LayoutParams) getLayoutParams();
                layoutParameters.leftMargin += labelViewWidth;
                layoutParameters.rightMargin += labelViewWidth;
                setLayoutParams(layoutParameters);
            }
            _isMarginAdjusted = true;
        }
    }

    /**
     * This is called when this seek bar is attached to a window and creates a number "label
     * views".
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TextView labelView;
        LinearLayout labelContainer;
        LinearLayout.LayoutParams layoutParameters;
        if (_labelViewList.isEmpty()) {
            labelContainer = ((ViewGroup) getParent()).findViewById(R.id.seekbar_labels);
            for (CharSequence label : _labelArray) {
                labelView = new TextView(_context);
                labelView.setTypeface(null, Typeface.NORMAL);
                layoutParameters = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                labelView.setGravity(Gravity.CENTER);
                labelView.setText(label);
                labelView.setLayoutParams(layoutParameters);
                labelContainer.addView(labelView);
                _labelViewList.add(labelView);
            }
            setLabelColors();
        }
    }

    /**
     * Called when this seek bar has change its visibility. This "overridden method" is only used to
     * tell whether or not the margins have been adjusted.
     *
     * @param visibility the new visibility of this view which is either View.VISIBLE,
     *                   View.INVISIBLE, or View.GONE
     */
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        _isMarginAdjusted = visibility != View.VISIBLE;
    }

    /**
     * Sets the colors of the labels (selected and unselected) guided by the current progress
     * value.
     */
    private void setLabelColors() {
        for (int i = 0; i < _labelViewList.size(); i++) {
            if (_value != i) {
                _labelViewList.get(i).setTextColor(_context.getColor(R.color.color_secondary_light));
            } else {
                _labelViewList.get(i).setTextColor(_context.getColor(R.color.color_secondary_dark));
            }
        }
    }

    /**
     * Returns the label view list index as a function of a progress value.
     *
     * @param progressValue a progress value
     *
     * @return a label view list index
     */
    private int getLabelIndex(int progressValue) {
        float factor;
        for (int i = 0; i < _labelArray.length; i++) {
            factor = 2 * i - 1;
            if (progressValue >= factor * _valueIncrement / 2 && progressValue < (factor + 2) * _valueIncrement / 2) {
                return i;
            }
        }
        return 0;
    }

    /**
     * A callback that notifies clients when the progress value has been changed. This includes
     * changes that were initiated by the user through a touch gesture or arrow key/trackball as
     * well as changes that were initiated programmatically.
     */
    public interface OnProgressListener {
        /**
         * Notification that the progress value has changed.
         *
         * @param value a new progress value
         *
         * @throws IllegalArgumentException
         * @noinspection JavadocDeclaration, RedundantSuppression
         */
        void onChanged(int value) throws IllegalArgumentException;
    }

}
