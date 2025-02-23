package net.hearnsoft.gensokyoradio.trd;

import android.Manifest;
import android.app.Dialog;
import android.app.WallpaperColors;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import androidx.palette.graphics.Palette;

import com.blankj.utilcode.util.SPStaticUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
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
import net.hearnsoft.gensokyoradio.trd.utils.Constants;
import net.hearnsoft.gensokyoradio.trd.utils.GlobalTimer;
import net.hearnsoft.gensokyoradio.trd.utils.TimerUpdateListener;
import net.hearnsoft.gensokyoradio.trd.utils.ViewModelUtils;
import net.hearnsoft.gensokyoradio.trd.widgets.SettingsSheetDialog;
import net.hearnsoft.gensokyoradio.trd.widgets.SongHistorySheetDialog;
import net.hearnsoft.gensokyoradio.trd.widgets.UserSheetDialog;
import net.hearnsoft.gensokyoradio.trd.widgets.VisualizerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    private ActivityMainBinding binding;
    private SongDataModel songDataModel;
    private ListenableFuture<MediaController> playerServiceFuture;
    private int screenOrientation;
    private ArrayList<BottomSheetDialogFragment> fragmentArrayList = new ArrayList<>();
    private boolean isUiPaused = false;
    private VisualizerView visualizerView;
    private SongDataBean dataBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Debug.startMethodTracing("app_trace");
        //设置Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppbar);
        screenOrientation = getResources().getConfiguration().orientation;
        //设置View top padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar, (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBar.top, 0, 0);
            binding.cover.setPadding(0, statusBar.top,0,0);
            return insets;
        });
        // 设置全屏和透明状态栏
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WebSocketService.setCallback(this);
        // 获取全局ViewModel
        songDataModel = ViewModelUtils.getViewModel(getApplication(), SongDataModel.class);
        requestPermissions();
        initVisualizer();
        GlobalTimer.getInstance().addListener(this);
        binding.songInfoBtn.setOnClickListener(v -> {
            Toast.makeText(this, R.string.fetch_song_data_toast, Toast.LENGTH_SHORT).show();
            getNowPlaying().thenAcceptAsync(isOK -> {
                if (isOK) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onCreate: " + dataBean.getSongInfo().getTitle());
                    }
                    runOnUiThread(this::buildNowPlayingDialog);
                }
            }, AsyncTask.THREAD_POOL_EXECUTOR);
        });
        songDataModel.getPlayBtnStatus().observe(this, status -> {
            binding.play.setEnabled(status);
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
        showNoticeDialog();
        startAppService();
        setViewObserve();
        //Debug.stopMethodTracing();
    }

    private void setViewObserve() {
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
            // 检查是否正在缓冲
            if (songDataModel.getBufferingState().getValue() != null &&
                    songDataModel.getBufferingState().getValue() == 1) {
                // 创建并设置圆形进度条
                IndeterminateDrawable<CircularProgressIndicatorSpec> progressIndicator =
                        IndeterminateDrawable.createCircularDrawable(this,
                                new CircularProgressIndicatorSpec(this, null));
                binding.play.setIcon(progressIndicator);
            } else {
                // 根据播放状态设置播放/暂停图标
                binding.play.setIcon(ResourcesCompat.getDrawable(getResources(),
                        isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                        getTheme()));
            }
        });

        songDataModel.getShowVisualizer().observe(this, show -> {
            songDataModel.getVisualizerUsable().postValue(show);
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

        //无论是否更新过数据
        binding.title.setText(songDataModel.getTitle().getValue());
        binding.artist.setText(songDataModel.getArtist().getValue());
        Glide.with(this)
                .load(songDataModel.getCoverUrl().getValue())
                .placeholder(R.drawable.ic_album)
                .into(binding.cover);
        songDataModel.getIsUpdatedInfo().postValue(false);
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
        boolean showVisualizer = SPStaticUtils.getBoolean("visualizer", false);
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            if (showVisualizer) {
                showVisualizer();
            }
            songDataModel.getShowVisualizer().postValue(showVisualizer);
            songDataModel.getVisualizerUsable().postValue(showVisualizer);
        } else {
            if (showVisualizer) {
                SPStaticUtils.put("visualizer", false);
            }
            songDataModel.getVisualizerUsable().postValue(false);
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
        visualizerView.initialize(this);
        visualizerView.setPlaying(true);
        visualizerView.setVisible(true);
        visualizerView.setColor(ContextCompat.getColor(this, R.color.system_accent));
        visualizerView.setPowerSaveMode(false);
        songDataModel.getVisualizerUsable().postValue(true);
    }

    private void startAppService(){
        CompletableFuture.runAsync(() -> {
            if (playerServiceFuture == null) {
                playerServiceFuture = new MediaController.Builder(MainActivity.this,
                        new SessionToken(MainActivity.this,
                                new ComponentName(MainActivity.this, GRStreamPlayerService.class)
                        )).buildAsync();
            }
        });
    }

    private void showNoticeDialog() {
        if (!SPStaticUtils.getBoolean(Constants.PREF_SHOWED_NOTICE_DIALOG, false)) {
            DialogNoticeBinding dialogBinding = DialogNoticeBinding.inflate(getLayoutInflater());
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.notice_titie)
                    .setMessage(R.string.dialog_notice_message)
                    .setView(dialogBinding.getRoot())
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        SPStaticUtils.put(Constants.PREF_SHOWED_NOTICE_DIALOG, true);
                        dialog.dismiss();
                    })
                    .show();
            dialogBinding.webappLink.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Constants.GR_PWA_APP_URL);
                startActivity(intent);
            });
        }
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
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            binding.cover.setImageDrawable(resource);
                            int colorInt = Color.TRANSPARENT;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                // Android 12+ 取色方法
                                if (resource instanceof BitmapDrawable) {
                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                    WallpaperColors colors = WallpaperColors.fromBitmap(bitmap);

                                    // 获取主色调
                                    Color primaryColor = colors.getPrimaryColor();
                                    colorInt = primaryColor.toArgb();
                                }
                            } else {
                                // Android 11- 取色方法
                                if (resource instanceof BitmapDrawable) {
                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                    Palette palette = Palette.from(bitmap).generate();
                                    colorInt = palette.getDominantColor(Color.TRANSPARENT);
                                }
                            }

                            // 计算颜色亮度
                            double luminance = ColorUtils.calculateLuminance(colorInt);

                            // 根据亮度决定文字和图标颜色
                            int textIconColor = luminance > 0.5 ? Color.BLACK : Color.WHITE;

                            if (luminance > 0.5) {
                                // 亮色背景下，状态栏文字颜色为黑色
                                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                            } else {
                                // 暗色背景下，状态栏文字颜色为白色
                                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                            }

                            // 应用到 Toolbar
                            binding.topAppbar.setTitleTextColor(textIconColor);
                            binding.topAppbar.setNavigationIconTint(textIconColor);

                            // 更新菜单项图标颜色
                            Menu menu = binding.topAppbar.getMenu();
                            for (int i = 0; i < menu.size(); i++) {
                                MenuItem item = menu.getItem(i);
                                if (item.getIcon() != null) {
                                    item.getIcon().setTint(textIconColor);
                                }
                            }

                            // Cover 渐变设置 - 使用更多的渐变点实现平滑过渡
                            Configuration config = getResources().getConfiguration();
                            int orientation = config.orientation;
                            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                // 横屏
                                // 设置状态栏颜色 (100透明度)
                                int systemBarColor = ColorUtils.setAlphaComponent(colorInt, 225);
                                getWindow().setStatusBarColor(systemBarColor);

                                GradientDrawable gradient = new GradientDrawable(
                                        GradientDrawable.Orientation.LEFT_RIGHT,
                                        new int[] {
                                                ColorUtils.setAlphaComponent(colorInt, 255),  // 左侧
                                                ColorUtils.setAlphaComponent(colorInt, 210),  // 左侧中部
                                                ColorUtils.setAlphaComponent(colorInt, 180),   // 左侧中部
                                                ColorUtils.setAlphaComponent(colorInt, 100),   // 中部
                                                ColorUtils.setAlphaComponent(colorInt, 80),   // 右侧中部
                                                ColorUtils.setAlphaComponent(colorInt, 0)     // 右侧完全透明
                                        }
                                );
                                binding.coverGradient.setBackground(gradient);
                                // 设置UI容器背景颜色
                                binding.uiContainer.setBackgroundColor(colorInt);
                                // 设置所有TextView的颜色
                                setTextViewsColor((ViewGroup) binding.uiContainer, textIconColor);
                            } else {
                                // 竖屏
                                // 设置状态栏颜色 (220透明度)
                                int systemBarColor = ColorUtils.setAlphaComponent(colorInt, 220);
                                getWindow().setStatusBarColor(systemBarColor);

                                GradientDrawable gradient = new GradientDrawable(
                                        GradientDrawable.Orientation.TOP_BOTTOM,
                                        new int[] {
                                                ColorUtils.setAlphaComponent(colorInt, 255),       // 顶部
                                                ColorUtils.setAlphaComponent(colorInt, 255),       // 顶部延续颜色以覆盖ActionBar
                                                ColorUtils.setAlphaComponent(colorInt, 200),       // 中部
                                                ColorUtils.setAlphaComponent(colorInt, 100),       // 中下部
                                                ColorUtils.setAlphaComponent(colorInt, 40),        // 下部
                                                ColorUtils.setAlphaComponent(colorInt, 0)          // 底部完全透明
                                        }
                                );
                                binding.coverGradient.setBackground(gradient);
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            binding.cover.setImageDrawable(placeholder);
                        }
                    });
            //showProgress();
            songDataModel.getPlayBtnStatus().postValue(true);
        });
    }

    /**
     * 递归遍历ViewGroup并设置所有TextView的颜色
     */
    private void setTextViewsColor(ViewGroup viewGroup, int color) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            } else if (child instanceof ViewGroup) {
                setTextViewsColor((ViewGroup) child, color);
            }
        }
    }

    private void buildNowPlayingDialog() {
        DialogNowPlayingBinding nowPlayingBinding = DialogNowPlayingBinding.inflate(
                LayoutInflater.from(this), null, false);
        nowPlayingBinding.infoTitle.setText(songDataModel.getNowPlayingTitle().getValue());
        nowPlayingBinding.infoArtist.setText(songDataModel.getNowPlayingArtist().getValue());
        nowPlayingBinding.infoAlbum.setText(songDataModel.getNowPlayingAlbum().getValue());
        nowPlayingBinding.infoYears.setText(songDataModel.getNowPlayingYears().getValue());
        nowPlayingBinding.infoCircle.setText(songDataModel.getNowPlayingCircle().getValue());
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
            SettingsSheetDialog dialog = new SettingsSheetDialog(getApplication(), this);
            dialog.show(getSupportFragmentManager(), "settings");
            fragmentArrayList.add(dialog);
        } else if (item.getItemId() == R.id.menu_history) {
            SongHistorySheetDialog dialog = new SongHistorySheetDialog(this);
            dialog.show(getSupportFragmentManager(), "history");
            fragmentArrayList.add(dialog);
        } else if (item.getItemId() == R.id.menu_login) {
            if (!SPStaticUtils.contains(Constants.PREF_APPSESSIONID_KEY) ||
                        !SPStaticUtils.contains(Constants.PREF_API_KEY)) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                UserSheetDialog dialog = new UserSheetDialog();
                dialog.show(getSupportFragmentManager(), "user");
                fragmentArrayList.add(dialog);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (screenOrientation != newConfig.orientation) {
            for (BottomSheetDialogFragment fragment : fragmentArrayList) {
                if (fragment.isVisible()) {
                    fragment.dismiss();
                }
            }
            fragmentArrayList.clear();
            screenOrientation = newConfig.orientation;
        }

        recreate();
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
        if (Boolean.TRUE.equals(songDataModel.getVisualizerUsable().getValue()) && visualizerView != null) {
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
        if (Boolean.TRUE.equals(songDataModel.getVisualizerUsable().getValue()) && visualizerView != null) {
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
        if (playerServiceFuture.isDone() && !playerServiceFuture.isCancelled()) {
            try {
                playerServiceFuture.get().release();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            playerServiceFuture.cancel(true);
        }
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
                        songDataModel.getNowPlayingTitle().postValue(dataBean.getSongInfo().getTitle());
                        songDataModel.getNowPlayingArtist().postValue(dataBean.getSongInfo().getArtist());
                        songDataModel.getNowPlayingAlbum().postValue(dataBean.getSongInfo().getAlbum());
                        songDataModel.getNowPlayingYears().postValue(dataBean.getSongInfo().getYear());
                        songDataModel.getNowPlayingCircle().postValue(dataBean.getSongInfo().getCircle());
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