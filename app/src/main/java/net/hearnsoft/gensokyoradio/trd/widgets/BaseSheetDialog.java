package net.hearnsoft.gensokyoradio.trd.widgets;

import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import net.hearnsoft.gensokyoradio.trd.R;

public abstract class BaseSheetDialog extends BottomSheetDialogFragment {

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

}
