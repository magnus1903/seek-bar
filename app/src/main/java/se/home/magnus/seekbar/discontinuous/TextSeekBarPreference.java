/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.home.magnus.seekbar.discontinuous;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import se.home.magnus.seekbar.R;
import se.home.magnus.seekbar.utility.Constant;

/**
 * This class is a seek bar preference displaying string labels with associated float values. NOTE
 * that the number of labels and values MUST BE THE SAME and that the label with the position index
 * "i" is associated with the value with the same position index. To configure the seek bar of this
 * preference see "label_seek_bar_preference.xml".
 */
public class TextSeekBarPreference extends Preference implements LabelSeekBar.OnProgressListener {

    /**
     * The color of the seek bar of this preference.
     */
    private final int _color;

    /**
     * The color of the thumb of the seek bar of this preference.
     */
    private final int _thumbColor;

    /**
     * The diameter (in pixels) of the thumb of the seek bar of this preference.
     */
    private final int _diameter;

    /**
     * The height (in pixels) of the seek bar of this preference.
     */
    private final int _size;

    /**
     * The default value of the seek bar which must be an element in the "value array".
     */
    private final float _defaultValue;

    /**
     * The seek bar of this preference.
     */
    private LabelSeekBar _labelSeekBar;

    /**
     * The values of the "discontinuous choices" of the seek bar of this preference.
     */
    private final float[] _valueArray;

    /**
     * The labels of the "discontinuous choices" of the seek bar of this preference.
     */
    private final CharSequence[] _labelArray;

    /**
     * @param context      the context this preference is running in, through which it can access
     *                     the current theme, resources, etc
     * @param attributeSet the attributes of the XML tag that is inflating this preference, which
     *                     may be null
     *
     * @throws IllegalArgumentException
     * @noinspection resource, JavadocDeclaration, RedundantSuppression
     */
    public TextSeekBarPreference(@NonNull Context context, @Nullable AttributeSet attributeSet) throws IllegalArgumentException {
        super(context, attributeSet, 0);
        CharSequence[] valueArray;
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TextSeekBar, 0, 0);
        try {
            _thumbColor = typedArray.getColor(R.styleable.TextSeekBar_textThumbColor, context.getColor(R.color.color_primary_dark));
            _color = typedArray.getColor(R.styleable.TextSeekBar_textColor, context.getColor(R.color.color_primary_dark));
            _diameter = typedArray.getInt(R.styleable.TextSeekBar_textDiameter, context.getResources().getInteger(R.integer.text_seek_bar_ball_diameter_default_value));
            _size = typedArray.getInt(R.styleable.TextSeekBar_textSize, context.getResources().getInteger(R.integer.text_seek_bar_size_default_value));
            _defaultValue = typedArray.getFloat(R.styleable.TextSeekBar_textDefaultValue, 0);
            if ((valueArray = typedArray.getTextArray(R.styleable.TextSeekBar_textValues)) != null) {
                _valueArray = new float[valueArray.length];
                for (int i = 0; i < _valueArray.length; i++) {
                    try {
                        _valueArray[i] = Float.parseFloat((String) valueArray[i]);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(getContext().getString(R.string.text_seek_bar_value_array_error));
                    }
                }
            } else {
                throw new IllegalArgumentException(getContext().getString(R.string.generic_seek_bar_value_array_error));
            }
            _labelArray = typedArray.getTextArray(R.styleable.TextSeekBar_textLabels);
            if (_valueArray.length != _labelArray.length) {
                throw new IllegalArgumentException(getContext().getString(R.string.generic_seek_bar_value_label_array_error));
            }
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Binds the created View to the data for this preference. This is a good place to grab
     * references to custom Views in the layout and set properties on them. Make sure to call
     * through to the superclass implementation.
     *
     * @param preferenceViewHolder the ViewHolder that provides references to the views to fill in,
     *                             these views will be recycled, so you should not hold a reference
     *                             to them after this method returns.
     *
     * @throws IllegalArgumentException
     * @noinspection RedundantSuppression
     */
    @SuppressWarnings("JavaDoc")
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder preferenceViewHolder) throws IllegalArgumentException {
        super.onBindViewHolder(preferenceViewHolder);
        int index = -1;
        float value = getPersistedFloat(_defaultValue);
        // the next statement is crucial because, if the "preference view holder" isn't set
        // to be NOT recyclable, all "text seek bar preference" instances will share the same
        // "text seek bar" instance
        preferenceViewHolder.setIsRecyclable(false);
        _labelSeekBar = (LabelSeekBar) preferenceViewHolder.findViewById(R.id.seekbar);
        _labelSeekBar.initialize(_labelArray, this, _thumbColor, _color, _size, _diameter);
        for (int i = 0; i < _valueArray.length; i++) {
            if (Math.abs(_valueArray[i] - value) < Constant.FLOAT_EQUALITY_TOLERANCE) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.generic_seek_bar_value_error));
        }
        _labelSeekBar.setValue(index);
    }

    /**
     * Sets the initial value of this preference.
     *
     * @param defaultValue the default value for the preference if set, otherwise null XXX
     */
    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = _defaultValue;
        }
        setValue(getPersistedFloat(Float.parseFloat(defaultValue.toString())));
    }

    /**
     * Notification that the progress value has changed.
     *
     * @param value a new progress value (as an index in the label array and also in the value
     *              array)
     *
     * @throws IllegalArgumentException
     * @noinspection RedundantSuppression
     */
    @SuppressWarnings("JavaDoc")
    public void onChanged(int value) throws IllegalArgumentException {
        persistFloat(_valueArray[value]);
    }

    /**
     * Sets the current value (and implicitly the seek bar value).
     *
     * @param value the current value which must be an element in the "value array"
     */
    public void setValue(float value) {
        int index;
        if (_labelSeekBar != null) {
            index = -1;
            for (int i = 0; i < _valueArray.length; i++) {
                if (Math.abs(_valueArray[i] - value) < Constant.FLOAT_EQUALITY_TOLERANCE) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.generic_seek_bar_value_error));
            }
            _labelSeekBar.setValue(index);
        }
        persistFloat(value);
    }

}
