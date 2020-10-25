package ir.pxmaster.www.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;


/**
 * Created by reza on 8/16/2016.
 */
public class NormalEditText extends AppCompatEditText {

    public NormalEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),
                String.format("fonts/%s", "IRANSans.ttf"));
        this.setTypeface(typeface);
    }

    public NormalEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),
                String.format("fonts/%s", "IRANSans.ttf"));
        this.setTypeface(typeface);
    }

    public NormalEditText(Context context) {
        super(context);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),
                String.format("fonts/%s", "IRANSans.ttf"));
        this.setTypeface(typeface);
    }

}