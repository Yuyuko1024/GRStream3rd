package net.hearnsoft.gensokyoradio.trd;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.hearnsoft.gensokyoradio.trd.beans.NowPlayingBean;
import net.hearnsoft.gensokyoradio.trd.beans.SongDataBean;
import net.hearnsoft.gensokyoradio.trd.databinding.ActivityMainBinding;
import net.hearnsoft.gensokyoradio.trd.databinding.DialogNoticeBinding;
import net.hearnsoft.gensokyoradio.trd.databinding.DialogNowPlayingBinding;
import net.hearnsoft.gensokyoradio.trd.model.SongDataModel;
import net.hearnsoft.gensokyoradio.trd.service.GRStreamPlayerService;
import net.hearnsoft.gensokyoradio.trd.service.WebSocketService;
import net.hearnsoft.gensokyoradio.trd.service.WsServiceInterface;
import net.hearnsoft.gensokyoradio.trd.utils.AudioSessionManager;
import net.hearnsoft.gensokyoradio.trd.utils.CarUtils;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;
import net.hearnsoft.gensokyoradio.trd.utils.GlobalTimer;
import net.hearnsoft.gensokyoradio.trd.utils.SettingsPrefUtils;
import net.hearnsoft.gensokyoradio.trd.utils.TimerUpdateListener;
import net.hearnsoft.gensokyoradio.trd.utils.ViewModelUtils;
import net.hearnsoft.gensokyoradio.trd.widgets.SettingsSheetDialog;
import net.hearnsoft.gensokyoradio.trd.widgets.VisualizerView;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity
        implements WsServiceInterface, TimerUpdateListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    // requestCode
    public static final int RC_PERM_DEFAULT = 1;
    private final ExecutorService signalThreadPool = Executors.newSingleThreadExecutor();
    private ActivityMainBinding binding;
    private SongDataModel songDataModel;
    private Intent WsIntent;
    private ListenableFuture<MediaController> playerServiceFuture;
    private boolean isBound = false;
    private boolean isUiPaused = false;
    private boolean visualizerUsable = false;
    private VisualizerView visualizerView;
    private SongDataBean dataBean;
    private String nowPlayingTitle,nowPlayingArtist,nowPlayingAlbum,nowPlayingYears,nowPlayingCircle;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service bind ready");
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Service disconnect ready");
            isBound = false;
        }
    };

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                buildNowPlayingDialog();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Debug.startMethodTracing("app_trace");
        //设置Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppbar);
        //设置View top padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            if (CarUtils.isAutomotiveOS(this)) {
                Log.d(TAG, "is Automotive OS");
                Insets nav = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, statusBar.top, 0, nav.top);
            } else {
                v.setPadding(0, statusBar.top, 0, 0);
            }
            return insets;
        });
        WebSocketService.setCallback(this);
        binding.play.setEnabled(false);
        // 获取全局ViewModel
        songDataModel = ViewModelUtils.getViewModel(getApplication(), SongDataModel.class);
        requestPermissions();
        initVisualizer();
        GlobalTimer.getInstance().addListener(this);
        binding.songInfoBtn.setOnClickListener(v -> {
            CompletableFuture<Boolean> future = getNowPlaying();
            Toast.makeText(this, R.string.fetch_song_data_toast, Toast.LENGTH_SHORT).show();
            future.thenAccept(isOK -> {
                if (isOK) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onCreate: " + dataBean.getSongInfo().getTitle());
                    }
                    nowPlayingTitle = dataBean.getSongInfo().getTitle() == null ?
                            "null" : dataBean.getSongInfo().getTitle();
                    nowPlayingArtist = dataBean.getSongInfo().getArtist() == null ?
                            "null" : dataBean.getSongInfo().getArtist();
                    nowPlayingAlbum = dataBean.getSongInfo().getAlbum() == null ?
                            "null" : dataBean.getSongInfo().getAlbum();
                    nowPlayingYears = dataBean.getSongInfo().getYear() == null ?
                            "null" : dataBean.getSongInfo().getYear();
                    nowPlayingCircle = dataBean.getSongInfo().getCircle() == null ?
                            "null" : dataBean.getSongInfo().getCircle();
                    handler.sendEmptyMessage(1);
                }
            });
        });
        binding.play.setOnClickListener(v -> {
            if (playerServiceFuture.isDone() && !playerServiceFuture.isCancelled()) {
                try {
                    MediaController player = playerServiceFuture.get();
                    if (player.isPlaying()) {
                        player.pause();
                    } else player.play();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        songDataModel.getBufferingState().observe(this, bufferingState -> {
            switch (bufferingState) {
                case 0:
                    binding.bufferState.setText(getString(R.string.status_idle));
                    break;
                case 1:
                    binding.bufferState.setText(getString(R.string.status_buffering));
                    break;
                case 2:
                    binding.bufferState.setText(getString(R.string.status_ready));
                    break;
            }
        });
        songDataModel.getPlayerStatus().observe(this, isPlaying -> {
            if (isPlaying) {
                binding.play.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause, getTheme()));
            } else {
                binding.play.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play, getTheme()));
            }
        });
        showNoticeDialog();
        startSocketService();
        songDataModel.getShowVisualizer().observe(this, show -> {
            visualizerUsable = show;
            if (visualizerView != null) {
                if (show) {
                    visualizerView.setPlaying(true);
                    visualizerView.setVisible(true);
                    visualizerView.setPowerSaveMode(false);
                } else {
                    visualizerView.setPlaying(false);
                    visualizerView.setVisible(false);
                    visualizerView.setPowerSaveMode(true);
                }
            } else {
                initVisualizer();
            }
        });
        //Debug.stopMethodTracing();
    }

    private void requestPermissions() {
        String[] perms;
        if (Build.VERSION.SDK_INT >= 33) {
             perms = new String[]{Manifest.permission.POST_NOTIFICATIONS,
                     Manifest.permission.READ_PHONE_STATE};
        } else {
            perms = new String[]{Manifest.permission.READ_PHONE_STATE};
        }
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.perm_need_basic),
                    RC_PERM_DEFAULT, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initVisualizer() {
        boolean showVisualizer = SettingsPrefUtils.getInstance(this)
                .readBooleanSettings("visualizer");
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            if (showVisualizer) {
                showVisualizer();
            }
            songDataModel.getShowVisualizer().postValue(showVisualizer);
        } else {
            if (showVisualizer) {
                SettingsPrefUtils.getInstance(this).writeBooleanSettings("visualizer", false);
            }
        }
    }

    private void showVisualizer() {
        int audioSessionId = AudioSessionManager.getInstance().getAudioSessionId();
        visualizerView = new VisualizerView(this, audioSessionId);
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
        params.gravity = Gravity.BOTTOM;
        binding.container.addView(visualizerView, 0, params);
        visualizerUsable = true;
        visualizerView.initialize(this);
        visualizerView.setPlaying(true);
        visualizerView.setVisible(true);
        visualizerView.setColor(ContextCompat.getColor(this, R.color.system_accent));
        visualizerView.setPowerSaveMode(false);
    }

    private void startSocketService(){
        signalThreadPool.submit(() -> {
            if (!isBound && WsIntent == null) {
                WsIntent = new Intent(MainActivity.this, WebSocketService.class);
                startService(WsIntent);
                bindService(WsIntent, connection, BIND_AUTO_CREATE);
            }
            if (playerServiceFuture == null) {
                playerServiceFuture = new MediaController.Builder(MainActivity.this,
                        new SessionToken(MainActivity.this,
                                new ComponentName(MainActivity.this, GRStreamPlayerService.class)
                        )).buildAsync();
            }
        });
    }

    private void showNoticeDialog() {
        DialogNoticeBinding dialogBinding = DialogNoticeBinding.inflate(getLayoutInflater());
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.notice_titie)
                .setMessage(R.string.dialog_notice_message)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
        dialogBinding.playBadge.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=net.gensokyoradio.app"));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=net.gensokyoradio.app"));
                startActivity(intent);
            }
        });
    }

    @Override
    public void beanReceived(NowPlayingBean bean) {
        if (BuildConfig.DEBUG) Log.d(TAG, "beanReceived: " +
                bean.getSongId() + " " + bean.getTitle());
        runOnUiThread(() -> {
            binding.title.setText(bean.getTitle());
            binding.artist.setText(bean.getArtist());
            Glide.with(this)
                    .load(bean.getAlbumArt())
                    .placeholder(R.drawable.ic_album)
                    .into(binding.cover);
            //showProgress();
            binding.play.setEnabled(true);
        });
    }

    private void buildNowPlayingDialog() {
        DialogNowPlayingBinding nowPlayingBinding = DialogNowPlayingBinding.inflate(
                LayoutInflater.from(this), null, false);
        nowPlayingBinding.infoTitle.setText(nowPlayingTitle);
        nowPlayingBinding.infoArtist.setText(nowPlayingArtist);
        nowPlayingBinding.infoAlbum.setText(nowPlayingAlbum);
        nowPlayingBinding.infoYears.setText(nowPlayingYears);
        nowPlayingBinding.infoCircle.setText(nowPlayingCircle);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle(R.string.song_info_title)
                .setView(nowPlayingBinding.getRoot())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onTimeUpdate(int duration, int played, int remaining) {
        if (BuildConfig.DEBUG && !isUiPaused) {
            Log.d(TAG, "onTimeUpdate: " + played + " " + duration + " " + remaining);
        }
        if (!isUiPaused) {
            //仅在UI在前台时才进行更新
            binding.seekBar.setMax(duration);
            binding.totalTime.post(() -> {
                binding.totalTime.setText(formatTime(duration));
            });
            binding.seekBar.setProgress(played);
            binding.playedTime.post(() -> {
                binding.playedTime.setText(formatTime(played));
            });
        }
    }

    public static String formatTime(int seconds) {
        if (Objects.isNull(seconds)) {
            return "--:--";
        }
        // 分钟数
        int minutes = seconds / 60;
        // 余下秒数
        int remainingSeconds = seconds % 60;

        String minutesStr;
        if (minutes < 10) {
            minutesStr = "0" + minutes;
        } else {
            minutesStr = String.valueOf(minutes);
        }

        String secondsStr;
        if (remainingSeconds < 10) {
            secondsStr = "0" + remainingSeconds;
        } else {
            secondsStr = String.valueOf(remainingSeconds);
        }

        return minutesStr + ":" + secondsStr;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            new SettingsSheetDialog(getApplication(), this)
                    .show(getSupportFragmentManager(), "settings");
        } else if (item.getItemId() == R.id.menu_history) {
            Intent intent = new Intent(this, SongHistoryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume: ");
        isUiPaused = false;
        if (Boolean.TRUE.equals(songDataModel.getIsUpdatedInfo().getValue())) {
            binding.title.setText(songDataModel.getTitle().getValue());
            binding.artist.setText(songDataModel.getArtist().getValue());
            Glide.with(this)
                    .load(songDataModel.getCoverUrl().getValue())
                    .placeholder(R.drawable.ic_album)
                    .into(binding.cover);
            songDataModel.getIsUpdatedInfo().postValue(false);
        }
        if (visualizerUsable && visualizerView != null) {
            visualizerView.setPlaying(true);
            visualizerView.setVisible(true);
            visualizerView.setPowerSaveMode(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        isUiPaused = false;
        //无论是否更新过数据
        binding.title.setText(songDataModel.getTitle().getValue());
        binding.artist.setText(songDataModel.getArtist().getValue());
        Glide.with(this)
                .load(songDataModel.getCoverUrl().getValue())
                .placeholder(R.drawable.ic_album)
                .into(binding.cover);
        songDataModel.getIsUpdatedInfo().postValue(false);
        if (visualizerUsable && visualizerView != null) {
            visualizerView.setPlaying(true);
            visualizerView.setVisible(true);
            visualizerView.setPowerSaveMode(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause: ");
        isUiPaused = true;
        if (visualizerUsable && visualizerView != null) {
            visualizerView.setPlaying(false);
            visualizerView.setVisible(false);
            visualizerView.setPowerSaveMode(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "onDestroy: ");
        stopService(WsIntent);
        unbindService(connection);
        if (playerServiceFuture.isDone() && !playerServiceFuture.isCancelled()) {
            try {
                playerServiceFuture.get().release();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            playerServiceFuture.cancel(true);
        }
        GlobalTimer.getInstance().stopTimer();
        GlobalTimer.getInstance().removeListener(this);
    }

    private CompletableFuture<Boolean> getNowPlaying() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Constants.NOW_PLAYING_JSON)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "fetch json data failed. reason: " + e);
                future.complete(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String body = "";
                    if (response.body() != null) {
                        body = response.body().string();
                    }
                    if (!TextUtils.isEmpty(body)) {
                        Gson gson = new GsonBuilder()
                                .disableHtmlEscaping()
                                .setLenient()
                                .serializeNulls()
                                .setPrettyPrinting()
                                .enableComplexMapKeySerialization()
                                .create();
                        dataBean = gson.fromJson(body, SongDataBean.class);
                        future.complete(true);
                    }
                    future.complete(false);
                } catch (Exception e) {
                    Log.e(TAG, "Fetch data error, "+ e);
                    future.complete(false);
                }
            }
        });
        return future;
    }
}