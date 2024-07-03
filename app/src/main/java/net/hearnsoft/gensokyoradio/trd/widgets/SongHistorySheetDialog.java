package net.hearnsoft.gensokyoradio.trd.widgets;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.hearnsoft.gensokyoradio.trd.BuildConfig;
import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.beans.SongHistoryBean;
import net.hearnsoft.gensokyoradio.trd.databinding.SongHistorySheetBinding;
import net.hearnsoft.gensokyoradio.trd.db.SongHistoryDbHelper;

import java.util.List;

public class SongHistorySheetDialog extends BaseSheetDialog{

    private static final String TAG = SettingsSheetDialog.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private SongHistorySheetBinding binding;
    private List<SongHistoryBean> beanList;
    private Context context;

    public SongHistorySheetDialog(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SongHistorySheetBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.historyList.setLayoutManager(new LinearLayoutManager(context));
        beanList = SongHistoryDbHelper.getInstance(context).getAllSongs();
        if (beanList != null && beanList.isEmpty()) {
            binding.historyList.setAdapter(new EmptyDataAdapter());
        } else {
            binding.historyList.setAdapter(new SongHistoryAdapter(beanList));
        }
        binding.clearHistory.setOnClickListener(v -> clearHistory());
    }

    private void clearHistory() {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.clear_history_text)
                .setMessage(R.string.clear_history_msg)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    SongHistoryDbHelper.getInstance(context)
                            .clearDatabase();
                    Toast.makeText(context, R.string.history_cleared_toast, Toast.LENGTH_SHORT).show();
                    binding.historyList.setAdapter(new EmptyDataAdapter());
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
