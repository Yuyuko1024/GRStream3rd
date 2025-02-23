package net.hearnsoft.gensokyoradio.trd.widgets;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SPStaticUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.hearnsoft.gensokyoradio.trd.BuildConfig;
import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.databinding.SettingsEditServerBinding;
import net.hearnsoft.gensokyoradio.trd.databinding.SettingsSheetBinding;
import net.hearnsoft.gensokyoradio.trd.model.SongDataModel;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;
import net.hearnsoft.gensokyoradio.trd.utils.ViewModelUtils;

import pub.devrel.easypermissions.EasyPermissions;

public class SettingsSheetDialog extends BaseSheetDialog {

    private static final String TAG = SettingsSheetDialog.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private Context context;
    private SettingsSheetBinding binding;
    private String serverName;
    private SongDataModel songDataModel;

    public SettingsSheetDialog(Application application, Context context) {
        this.context = context;
        // 获取全局ViewModel
        songDataModel = ViewModelUtils.getViewModel(application, SongDataModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsSheetBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        int clientId = SPStaticUtils.getInt(Constants.PREF_CLIENT_ID, 0);
        boolean visualizerEnabled = SPStaticUtils.getBoolean(Constants.PREF_VISUALIZER, false);
        binding.socketId.setText(String.valueOf(clientId));
        binding.settingsVisualizerSwitch.setChecked(visualizerEnabled);
        switch (SPStaticUtils.getInt(Constants.PREF_SERVER, 0)) {
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
        binding.settingsVisualizerSwitch.setOnClickListener(v -> showVisualizerNotice());
        binding.settingsEditServer.setOnClickListener(v -> editServer());
        binding.settingsVisualizerSwitch.setOnCheckedListener(v -> enableVisualizer());
        binding.settingsAbout.setOnClickListener( v -> {
            new AboutBottomSheet().show(getParentFragmentManager(), "about");
            dismiss();
        });
    }

    private void showVisualizerNotice() {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.settings_ui_visualizer_title)
                .setMessage(R.string.settings_ui_visualizer_desc)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void enableVisualizer() {
        String perm = Manifest.permission.RECORD_AUDIO;
        boolean checked = binding.settingsVisualizerSwitch.isChecked();
        if (EasyPermissions.hasPermissions(context, perm)) {
            SPStaticUtils.put(Constants.PREF_VISUALIZER, checked);
            songDataModel.getShowVisualizer().postValue(checked);
        } else {
            EasyPermissions.requestPermissions(requireActivity(), getString(R.string.perm_need_record),
                    1, perm);
            if (EasyPermissions.somePermissionDenied(requireActivity(), perm)) {
                if (DEBUG) Log.e(TAG, "record permission denied.");
                Toast.makeText(context, R.string.perm_denied_record, Toast.LENGTH_SHORT).show();
                SPStaticUtils.put(Constants.PREF_VISUALIZER, false);
                songDataModel.getShowVisualizer().postValue(false);
            } else {
                SPStaticUtils.put(Constants.PREF_VISUALIZER, checked);
                songDataModel.getShowVisualizer().postValue(checked);
            }
        }
    }

    private void editServer() {
        SettingsEditServerBinding editServerBinding = SettingsEditServerBinding.inflate(getLayoutInflater());
        int server_settings = getIntPref("server");
        if (server_settings == 3) {
            editServerBinding.serverUrlInput.setEnabled(true);
            editServerBinding.serverUrlInput.setText(getStringPref("custom_server"));
        }
        switch (server_settings) {
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
            editServerBinding.serverUrlInput.setText(getStringPref("custom_server"));
            writeIntSettings("server", 3);
        });
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.settings_edit_server_title)
                .setView(editServerBinding.getRoot())
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (getIntPref("server") == 3 &&
                            !TextUtils.isEmpty(editServerBinding.serverUrlInput.getEditableText().toString())) {
                        writeStringSettings("custom_server", editServerBinding.serverUrlInput.getEditableText().toString());
                    }
                    Toast.makeText(context, R.string.settings_saved_toast, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void writeIntSettings(String key, int value) {
        SPStaticUtils.put(key, value);
    }

    private void writeStringSettings(String key, String value) {
        SPStaticUtils.put(key, value);
    }

    private String getStringPref(String key) {
        return SPStaticUtils.getString(key);
    }

    private int getIntPref(String key) {
        return SPStaticUtils.getInt(key);
    }
}
