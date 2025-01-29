package net.hearnsoft.gensokyoradio.trd.widgets;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.hearnsoft.gensokyoradio.trd.LoginActivity;
import net.hearnsoft.gensokyoradio.trd.databinding.DialogLoginLoadingBinding;

public class LoginLoadingDialog extends MaterialAlertDialogBuilder {
    private final Context mContext;

    public LoginLoadingDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    public LoginLoadingDialog(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
        mContext = context;
        init();
    }

    private void init() {
        DialogLoginLoadingBinding binding = DialogLoginLoadingBinding.inflate(
                ((LoginActivity) mContext).getLayoutInflater()
        );
        setView(binding.getRoot());
        setCancelable(false);
    }
}
