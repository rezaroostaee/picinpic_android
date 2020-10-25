package ir.pxmaster.www.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;


/**
 * Created by reza on 8/16/2016.
 */
public class NormalTextView extends AppCompatTextView {

    public NormalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),
                String.format("fonts/%s", "IRANSans.ttf"));
        this.setTypeface(typeface);
        this.setText(FormatHelper.toPersianNumber(this.getText().toString()));
    }

    public NormalTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),
                String.format("fonts/%s", "IRANSans.ttf"));
        this.setTypeface(typeface);
        this.setText(FormatHelper.toPersianNumber(this.getText().toString()));
    }

    public NormalTextView(Context context) {
        super(context);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),
                String.format("fonts/%s", "IRANSans.ttf"));
        this.setTypeface(typeface);
        this.setText(FormatHelper.toPersianNumber(this.getText().toString()));
    }

}