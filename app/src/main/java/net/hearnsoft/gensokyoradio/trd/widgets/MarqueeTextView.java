package net.hearnsoft.gensokyoradio.trd.widgets;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class MarqueeTextView extends AppCompatTextView {

    public MarqueeTextView(Context context) {
        super(context);
        init();
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        this.setSingleLine(true);
        this.setMarqueeRepeatLimit(-1);
    }

    public boolean isFocused() {
        return true;
    }
}
