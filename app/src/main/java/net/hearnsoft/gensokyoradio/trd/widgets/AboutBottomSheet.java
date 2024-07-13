package net.hearnsoft.gensokyoradio.trd.widgets;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import net.hearnsoft.gensokyoradio.trd.BuildConfig;
import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.databinding.AboutSheetBinding;

public class AboutBottomSheet extends BaseSheetDialog {

    private final String TAG = this.getClass().getSimpleName();
    private AboutSheetBinding binding;

    public AboutBottomSheet() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AboutSheetBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.icon.setImageDrawable(getSelfIcon(requireContext()));
        binding.aboutContent.setMovementMethod(LinkMovementMethod.getInstance());
        binding.versionText.setText("version: " + BuildConfig.VERSION_NAME);
    }

    private Drawable getSelfIcon(Context context) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo info;
        try {
            info = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
        } catch (Resources.NotFoundException | PackageManager.NameNotFoundException e) {
            Log.e(TAG, "get res err.");
            return AppCompatResources.getDrawable(requireContext(), R.mipmap.ic_launcher);
        }
        return info.loadIcon(pm);
    }
}
