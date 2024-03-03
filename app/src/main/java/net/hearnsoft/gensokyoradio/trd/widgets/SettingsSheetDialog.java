package net.hearnsoft.gensokyoradio.trd.widgets;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.databinding.SettingsEditServerBinding;
import net.hearnsoft.gensokyoradio.trd.databinding.SettingsSheetBinding;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;

public class SettingsSheetDialog extends BottomSheetDialogFragment {

    private static final String TAG = SettingsSheetDialog.class.getSimpleName();

    private Context context;
    private SettingsSheetBinding binding;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String serverName;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    public SettingsSheetDialog(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsSheetBinding.inflate(inflater);
        preferences = context.getSharedPreferences(Constants.PREF_GLOBAL_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        binding.socketId.setText(String.valueOf(preferences.getInt("clientId", 0)));
        switch (preferences.getInt("server", 0)) {
            case 1:
                serverName = getString(R.string.server_mobile);
                break;
            case 2:
                serverName = getString(R.string.server_enhanced);
                break;
            case 3:
                serverName = getString(R.string.server_custom);
                break;
            case 0:
            default:
                serverName = getString(R.string.server_default);
                break;
        }
        binding.serverUsing.setText(serverName);
        binding.settingsEditServer.setOnClickListener(v -> editServer());
    }

    private void editServer() {
        SettingsEditServerBinding editServerBinding = SettingsEditServerBinding.inflate(getLayoutInflater());
        if (preferences.getInt("server", 0) == 3) {
            editServerBinding.serverUrlInput.setEnabled(true);
        }
        switch (preferences.getInt("server", 0)) {
            case 1:
                editServerBinding.serverMobile.setChecked(true);
                break;
            case 2:
                editServerBinding.serverEnhanced.setChecked(true);
                break;
            case 3:
                editServerBinding.serverCustom.setChecked(true);
                break;
            case 0:
            default:
                editServerBinding.serverDefault.setChecked(true);
                break;
        }
        editServerBinding.serverDefault.setOnClickListener(v -> writeIntSettings("server", 0));
        editServerBinding.serverMobile.setOnClickListener(v -> writeIntSettings("server", 1));
        editServerBinding.serverEnhanced.setOnClickListener(v -> writeIntSettings("server", 2));
        editServerBinding.serverCustom.setOnClickListener(v -> {
            editServerBinding.serverUrlInput.setEnabled(true);
            writeIntSettings("server", 3);
        });
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_edit_server_title)
                .setView(editServerBinding.getRoot())
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (preferences.getInt("server", 0) == 3 &&
                            !TextUtils.isEmpty(editServerBinding.serverUrlInput.getEditableText().toString())) {
                        writeStringSettings("custom_server", "");
                    }
                    Toast.makeText(requireContext(), R.string.settings_saved_toast, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void writeIntSettings(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    private void writeStringSettings(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
