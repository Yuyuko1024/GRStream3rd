package net.hearnsoft.gensokyoradio.trd.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.hearnsoft.gensokyoradio.trd.R;

public class FooterTipsView extends LinearLayout {

    private TextView textView;

    public FooterTipsView(Context context) {
        super(context);
        init(context);
    }

    public FooterTipsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FooterTipsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public FooterTipsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.footer_tips, this, true);

        textView = findViewById(R.id.tips_string);
    }

    public void setText(String text) {
        if (text != null && !text.isEmpty()) {
            textView.setText(text);
        } else {
            textView.setText(R.string.default_footer_tips); // 这里可以设置默认文本
        }
    }
}
