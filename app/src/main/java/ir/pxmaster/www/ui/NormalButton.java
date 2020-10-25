package ir.pxmaster.www.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;


/**
 * Created by reza on 8/16/2016.
 */
public class NormalButton extends AppCompatButton {

    public NormalButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),
                String.format("fonts/%s", "IRANSans.ttf"));
        this.setTypeface(typeface);
    }

    public NormalButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),
                String.format("fonts/%s", "IRANSans.ttf"));
        this.setTypeface(typeface);
    }

    public NormalButton(Context context) {
        super(context);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),
                String.format("fonts/%s", "IRANSans.ttf"));
        this.setTypeface(typeface);
    }

}