package net.hearnsoft.gensokyoradio.trd.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.databinding.CustomSettingsSwitchViewBinding;

public class CustomSettingsSwitchView extends LinearLayout {

    private CustomSettingsSwitchViewBinding binding;
    private OnClickListener mClickListener;
    private OnClickListener mCheckedListener;


    public CustomSettingsSwitchView(Context context) {
        this(context, null);
    }

    public CustomSettingsSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        binding = CustomSettingsSwitchViewBinding.inflate(LayoutInflater.from(context), this, true);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomSettingsSwitchView);
        try {
            int iconSrc = typedArray.getResourceId(R.styleable.CustomSettingsSwitchView_icon, 0);
            String titleText = typedArray.getString(R.styleable.CustomSettingsSwitchView_title);
            boolean switchState = typedArray.getBoolean(R.styleable.CustomSettingsSwitchView_switchChecked, false);

            setIcon(iconSrc);
            setTitle(titleText);
            setSwitchState(switchState);
        } finally {
            typedArray.recycle();
        }

        binding.settingsRootView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onClick(CustomSettingsSwitchView.this);
            }
        });

        binding.settingsSwitch.setOnClickListener(v -> {
            if (mCheckedListener != null) {
                mCheckedListener.onClick(binding.settingsSwitch);
            }
        });

    }

    public void setIcon(int resourceId) {
        binding.headerIcon.setImageResource(resourceId);
    }

    public void setTitle(String title) {
        binding.settingsTitleText.setText(title);
    }

    public void setSwitchState(boolean state) {
        binding.settingsSwitch.setChecked(state);
    }

    public boolean isChecked() {
        return binding.settingsSwitch.isChecked();
    }

    public void setChecked(boolean isChecked) {
        binding.settingsSwitch.setChecked(isChecked);
    }

    public void setOnCheckedListener(OnClickListener mClickListener) {
        this.mCheckedListener = mClickListener;
    }

    public void setOnClickListener(OnClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

}
