package net.hearnsoft.gensokyoradio.trd;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.hearnsoft.gensokyoradio.trd.beans.SongHistoryBean;
import net.hearnsoft.gensokyoradio.trd.databinding.ActivitySongHistoryBinding;
import net.hearnsoft.gensokyoradio.trd.db.SongHistoryDbHelper;
import net.hearnsoft.gensokyoradio.trd.utils.CarUtils;
import net.hearnsoft.gensokyoradio.trd.widgets.EmptyDataAdapter;
import net.hearnsoft.gensokyoradio.trd.widgets.SongHistoryAdapter;

import java.util.List;

public class SongHistoryActivity extends AppCompatActivity {

    private ActivitySongHistoryBinding binding;
    private List<SongHistoryBean> beanList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivitySongHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //设置View top padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            if (CarUtils.isAutomotiveOS(this)) {
                Insets nav = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, statusBar.top, 0, nav.top);
            } else {
                v.setPadding(0, statusBar.top, 0, 0);
            }
            return insets;
        });
        binding.historyList.setLayoutManager(new LinearLayoutManager(this));
        beanList = SongHistoryDbHelper.getInstance(this).getAllSongs();
        if (beanList != null && beanList.size() == 0) {
            binding.historyList.setAdapter(new EmptyDataAdapter());
        } else {
            binding.historyList.setAdapter(new SongHistoryAdapter(beanList));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_clear_history) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.clear_history_text)
                    .setMessage(R.string.clear_history_msg)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        SongHistoryDbHelper.getInstance(SongHistoryActivity.this)
                                .clearDatabase();
                        Toast.makeText(SongHistoryActivity.this,
                                R.string.history_cleared_toast, Toast.LENGTH_SHORT).show();
                        binding.historyList.setAdapter(new EmptyDataAdapter());
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}
