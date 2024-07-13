package net.hearnsoft.gensokyoradio.trd.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.databinding.CustomSettingsViewBinding;

public class CustomSettingsView extends LinearLayout {

    private CustomSettingsViewBinding binding;
    private OnClickListener mClickListener;

    public CustomSettingsView(Context context) {
        this(context, null);
    }

    public CustomSettingsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        binding = CustomSettingsViewBinding.inflate(LayoutInflater.from(context), this, true);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomSettingsView);
        try {
            int iconSrc = typedArray.getResourceId(R.styleable.CustomSettingsView_iconSrc, 0);
            String titleText = typedArray.getString(R.styleable.CustomSettingsView_titleText);

            setIcon(iconSrc);
            setTitle(titleText);
        } finally {
            typedArray.recycle();
        }

        binding.settingsRootView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onClick(CustomSettingsView.this);
            }
        });
    }

    public void setIcon(int resourceId) {
        binding.headerIcon.setImageResource(resourceId);
    }

    public void setTitle(String title) {
        binding.settingsTitleText.setText(title);
    }

    public void setOnClickListener(OnClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }
}
