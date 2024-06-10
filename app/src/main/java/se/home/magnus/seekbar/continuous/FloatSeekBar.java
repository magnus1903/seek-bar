package se.home.magnus.seekbar.continuous;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.res.ResourcesCompat;

import se.home.magnus.seekbar.R;

/**
 * This class is a seek bar displaying float values between 0.0 and 1.0.
 */
public class FloatSeekBar extends AppCompatSeekBar {

    /**
     * Tells whether or not the seek bar change listener is set. This is a solution to make the
     * "seek bar change listener dependency" mandatory. If the seek bar change listener isn't set
     * (when needed) an exception is thrown. The "motivation" for this solution is that it isn't
     * possible to use "constructor dependency injection".
     */
    private boolean _isSeekBarChangeListenerSet;

    /**
     * Attribute controlling the amount to increment or decrement the seek bar value when the user
     * moves the thumb.
     */
    private float _valueIncrement;

    /**
     * A "mandatory" seek bar change listener.
     */
    private OnSeekBarChangeListener _seekBarChangeListener;

    /**
     * @param context      the context this view is running in, through which it can access the
     *                     current theme, resources, etc
     * @param attributeSet the attributes of the XML tag that is inflating the view, which may be
     *                     null
     *
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("JavaDoc")
    public FloatSeekBar(@NonNull Context context, @NonNull AttributeSet attributeSet) throws IllegalArgumentException {
        super(context, attributeSet);
        LayerDrawable progress;
        if ((progress = (LayerDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.float_seek_bar, null)) == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.float_seek_bar_constructor_error));
        }
        _isSeekBarChangeListenerSet = false;
        _valueIncrement = 1;
        for (int i = 0; i < progress.getNumberOfLayers(); i++) {
            progress.setLayerHeight(i, 10);
        }
        // NOTE that this float seek bar is listening on its "super class" returning integer values
        // between 0 and 100 which is transformed to values between 0.0 and 1.0,
        // and these values are forwarded to the listener of this float seek bar
        setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * Notification that the value has changed. Clients can use the fromUser parameter
             * to distinguish user-initiated changes from those that occurred programmatically.
             *
             * @param seekBar               the SeekBar whose value has changed
             * @param currentIntegerValue   the current value, this will be in the range 0 and 100
             * @param fromUser              true if the value change was initiated by the user
             */
            @Override
            public void onProgressChanged(@NonNull SeekBar seekBar, int currentIntegerValue, boolean fromUser) {
                float newFloatValue;
                if (_isSeekBarChangeListenerSet) {
                    newFloatValue = Math.round((currentIntegerValue / 100f) / _valueIncrement) * _valueIncrement;
                    setProgress(Math.round(newFloatValue * 100));
                    _seekBarChangeListener.onProgressChanged(newFloatValue, fromUser);
                } else {
                    throw new IllegalArgumentException(getContext().getString(R.string.generic_seek_bar_change_dependency_error));
                }
            }

            /**
             * Notification that the user has started a touch gesture. Clients may want to use this to
             * disable advancing the SeekBar.
             *
             * @param seekBar the SeekBar in which the touch gesture began
             */
            @Override
            public void onStartTrackingTouch(@NonNull SeekBar seekBar) {
                if (_isSeekBarChangeListenerSet) {
                    _seekBarChangeListener.onStartTrackingTouch();
                } else {
                    throw new IllegalArgumentException(getContext().getString(R.string.generic_seek_bar_change_dependency_error));
                }
            }

            /**
             * Notification that the user has finished a touch gesture. Clients may want to use this to
             * re-enable advancing the SeekBar.
             *
             * @param seekBar the SeekBar in which the touch gesture began
             */
            @Override
            public void onStopTrackingTouch(@NonNull SeekBar seekBar) {
                if (_isSeekBarChangeListenerSet) {
                    _seekBarChangeListener.onStopTrackingTouch();
                } else {
                    throw new IllegalArgumentException(getContext().getString(R.string.generic_seek_bar_change_dependency_error));
                }
            }
        });
    }

    /**
     * Initializes this float seek bar.
     *
     * @param listener   a seek bar notification listener
     * @param increment  a value increment which must be between 0.0 and 1.0 (inclusive)
     * @param thumbColor a thumb color
     * @param color      a progress color
     * @param size       a thickness (height) of the seek bar (in pixels)
     * @param diameter   a thumb diameter (in pixels)
     *
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("JavaDoc")
    public void initialize(@NonNull OnSeekBarChangeListener listener, float increment, @ColorInt int thumbColor, @ColorInt int color, int size, int diameter) throws IllegalArgumentException {
        int integerValue = Math.round(increment * 100f), thumbRadius = Math.round(diameter / 2f);
        ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
        LayerDrawable progress = (LayerDrawable) getProgressDrawable();
        LinearLayout.LayoutParams layoutParameters;
        thumb.setIntrinsicWidth(diameter);
        thumb.setIntrinsicHeight(diameter);
        ((GradientDrawable) progress.findDrawableByLayerId(R.id.background)).setCornerRadius(size);
        for (int i = 0; i < progress.getNumberOfLayers(); i++) {
            progress.setLayerHeight(i, size);
        }
        layoutParameters = (LinearLayout.LayoutParams) getLayoutParams();
        layoutParameters.leftMargin += thumbRadius;
        layoutParameters.rightMargin += thumbRadius;
        setLayoutParams(layoutParameters);
        getProgressDrawable().setTint(color);
        thumb.setTint(thumbColor);
        setThumb(thumb);
        if (integerValue < 0 || integerValue > 100) {
            throw new IllegalArgumentException(getContext().getString(R.string.float_seek_bar_set_value_error));
        }
        _isSeekBarChangeListenerSet = true;
        _valueIncrement = increment;
        _seekBarChangeListener = listener;
    }

    /**
     * Sets the value which must be between 0.0 and 1.0 (inclusive).
     *
     * @param value a value
     *
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("JavaDoc")
    public void setValue(float value) throws IllegalArgumentException {
        int integerValue = Math.round(value * 100f);
        if (integerValue < 0 || integerValue > 100f) {
            throw new IllegalArgumentException(getContext().getString(R.string.float_seek_bar_set_value_error));
        }
        setProgress(integerValue);
    }

    /**
     * A callback that notifies clients when the value has been changed. This includes changes that
     * were initiated by the user through a touch gesture or arrow key/trackball as well as changes
     * that were initiated programmatically.
     */
    public interface OnSeekBarChangeListener {
        /**
         * Notification that the value has changed. Clients can use the fromUser parameter to
         * distinguish user-initiated changes from those that occurred programmatically.
         *
         * @param floatValue the new value of the FloatSeekBar
         * @param fromUser   true if the value change was initiated by the user
         *
         * @throws IllegalArgumentException
         */
        @SuppressWarnings("JavaDoc")
        void onProgressChanged(float floatValue, boolean fromUser) throws IllegalArgumentException;

        /**
         * Notification that the user has started a touch gesture. Clients may want to use this to
         * disable advancing the FloatSeekBar.
         */
        void onStartTrackingTouch();

        /**
         * Notification that the user has finished a touch gesture. Clients may want to use this to
         * re-enable advancing the FloatSeekBar.
         */
        void onStopTrackingTouch();
    }

}
