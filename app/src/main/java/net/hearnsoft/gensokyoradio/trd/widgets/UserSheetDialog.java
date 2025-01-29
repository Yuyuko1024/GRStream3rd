package net.hearnsoft.gensokyoradio.trd.widgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SPStaticUtils;

import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.databinding.UserSheetBinding;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;

public class UserSheetDialog extends BaseSheetDialog {
    private final String TAG = this.getClass().getSimpleName();

    private UserSheetBinding binding;

    public UserSheetDialog() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = UserSheetBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.usernameText.setText(
                SPStaticUtils.getString(Constants.PREF_USERNAME_KEY)
        );
        binding.useridText.setText(
                SPStaticUtils.getString(Constants.PREF_USERID_KEY)
        );

        binding.logoutButton.setOnClickListener(v -> {
            SPStaticUtils.remove(Constants.PREF_USERID_KEY);
            SPStaticUtils.remove(Constants.PREF_USERNAME_KEY);
            SPStaticUtils.remove(Constants.PREF_APPSESSIONID_KEY);
            SPStaticUtils.remove(Constants.PREF_API_KEY);

            if (!SPStaticUtils.contains(Constants.PREF_USERID_KEY) &&
                    !SPStaticUtils.contains(Constants.PREF_USERNAME_KEY) &&
                    !SPStaticUtils.contains(Constants.PREF_APPSESSIONID_KEY) &&
                    !SPStaticUtils.contains(Constants.PREF_API_KEY)) {
                Toast.makeText(requireContext(), R.string.user_sheet_logout_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.user_sheet_logout_err, Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });
    }
}
