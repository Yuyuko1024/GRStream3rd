package net.hearnsoft.gensokyoradio.trd;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.hearnsoft.gensokyoradio.trd.beans.NowPlayingBean;
import net.hearnsoft.gensokyoradio.trd.beans.SongDataBean;
import net.hearnsoft.gensokyoradio.trd.databinding.ActivityMainBinding;
import net.hearnsoft.gensokyoradio.trd.databinding.DialogNoticeBinding;
import net.hearnsoft.gensokyoradio.trd.model.SongDataModel;
import net.hearnsoft.gensokyoradio.trd.service.GRStreamPlayerService;
import net.hearnsoft.gensokyoradio.trd.service.WebSocketService;
import net.hearnsoft.gensokyoradio.trd.service.WsServiceInterface;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;
import net.hearnsoft.gensokyoradio.trd.utils.ViewModelUtils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements WsServiceInterface {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final ExecutorService signalThreadPool = Executors.newSingleThreadExecutor();
    private ActivityMainBinding binding;
    private SongDataModel songDataModel;
    private Intent WsIntent;
    private Intent PlayerIntent;
    private Timer timer;
    private SharedPreferences sharedPreferences;
    private boolean isBound = false;
    private boolean isUiPaused = false;
    private boolean isPlaying = false;
    private boolean isUpdateProgress = false;
    private SongDataBean dataBean;
    private GRStreamPlayerService playerService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBound = true;
            GRStreamPlayerService.ServiceBinder playerBinder = (GRStreamPlayerService.ServiceBinder) service;
            playerService = playerBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle(R.string.song_info_title)
                        .setMessage(msg.obj.toString())
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Debug.startMethodTracing("app_trace");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        if (requestPermissions()) {
            initVisualizer();
        }
        setContentView(binding.getRoot());
        WebSocketService.setCallback(this);
        sharedPreferences = getSharedPreferences("ws", Context.MODE_PRIVATE);
        binding.play.setEnabled(false);
        // 获取全局ViewModel
        songDataModel = ViewModelUtils.getViewModel(getApplication(), SongDataModel.class);
        binding.songInfoBtn.setOnClickListener(v -> {
            CompletableFuture<Boolean> future = getNowPlaying();
            Toast.makeText(this, R.string.fetch_song_data_toast, Toast.LENGTH_SHORT).show();
            future.thenAccept(isOK -> {
                if (isOK) {
                    Log.d(TAG, "onCreate: " + dataBean.getSongInfo().getTitle());
                    String info = getString(R.string.song_info, dataBean.getSongInfo().getTitle(),
                            dataBean.getSongInfo().getArtist(), dataBean.getSongInfo().getAlbum(),
                            dataBean.getSongInfo().getYear(), dataBean.getSongInfo().getCircle());
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = info;
                    handler.sendMessage(message);
                }
            });
        });
        binding.play.setOnClickListener(v -> {
            playerService.playAndPauseStream();
            if (isPlaying) {
                isPlaying = false;
                binding.play.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play, getTheme()));
            } else {
                isPlaying = true;
                binding.play.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause, getTheme()));
            }
        });
        songDataModel.getBufferingState().observe(this, bufferingState -> {
            switch (bufferingState) {
                case 0:
                    binding.bufferState.setText("IDLE");
                    break;
                case 1:
                    binding.bufferState.setText("BUFFERING");
                    break;
                case 2:
                    binding.bufferState.setText("READY");
                    break;
            }
        });
        showNoticeDialog();
        startSocketService();
        //Debug.stopMethodTracing();
    }

    private boolean requestPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS,Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.READ_PHONE_STATE}, 1);
                return false;
            } else {
                return true;
            }
        } else {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE}, 1);
                return false;
            } else {
                return true;
            }
        }
    }

    private void initVisualizer() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO ) == PackageManager.PERMISSION_GRANTED) {
            binding.visualizerView.initialize(this);
            binding.visualizerView.setPlaying(true);
            binding.visualizerView.setVisible(true);
            binding.visualizerView.setColor(ContextCompat.getColor(this, R.color.system_accent));
            binding.visualizerView.setPowerSaveMode(false);
        }
    }

    private void startSocketService(){
        signalThreadPool.submit(() -> {
            if (!isBound && WsIntent == null) {
                WsIntent = new Intent(MainActivity.this, WebSocketService.class);
                startService(WsIntent);
            }
            if (PlayerIntent == null) {
                PlayerIntent = new Intent(MainActivity.this, GRStreamPlayerService.class);
                startService(PlayerIntent);
                bindService(PlayerIntent, connection, BIND_AUTO_CREATE);
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
        Log.d(TAG, "beanReceived: " + bean.getSongId() + " " + bean.getTitle());
        runOnUiThread(() -> {
            binding.title.setText(bean.getTitle());
            binding.artist.setText(bean.getArtist() + " - " + bean.getAlbum());
            Glide.with(this).load(bean.getAlbumArt()).placeholder(R.drawable.ic_album).into(binding.cover);
            showProgress(bean.getPlayed()+1, bean.getDuration(), bean.getRemaining()-1);
            Toast.makeText(MainActivity.this, "WebSocket Client ID: " +
                    sharedPreferences.getInt("clientId",0), Toast.LENGTH_SHORT).show();
            binding.play.setEnabled(true);
        });
    }

    private void showProgress(int played, int duration, int remaining) {
        if (isUpdateProgress && timer != null) {
            timer.cancel();
        }
        isUpdateProgress = true;
        Log.d(TAG, "showProgress: " + played + " " + duration + " " + remaining);
        binding.seekBar.setMax(duration);
        binding.totalTime.post(() -> {
            binding.totalTime.setText(formatTime(duration));
        });

        timer = new Timer();
        timer.schedule(new TimerTask() {
            int playedSec = played;
            @Override
            public void run() {
                if (playedSec == duration) {
                    timer.cancel();
                    isUpdateProgress = false;
                } else {
                    playedSec++;
                    binding.seekBar.setProgress(playedSec);
                    binding.playedTime.post(() -> {
                        binding.playedTime.setText(formatTime(playedSec));
                    });
                }
            }
        }, 0, 1000);
    }

    public String formatTime(int seconds) {
        int minutes = seconds / 60;       // 分钟数
        int remainingSeconds = seconds % 60;    // 余下秒数

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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume: ");
        if (isUiPaused && Boolean.TRUE.equals(songDataModel.getIsUpdatedInfo().getValue())) {
            binding.title.setText(songDataModel.getTitle().getValue());
            binding.artist.setText(songDataModel.getArtist().getValue() + " - " + songDataModel.getAlbum().getValue());
            Glide.with(this).load(songDataModel.getCoverUrl().getValue()).placeholder(R.drawable.ic_album).into(binding.cover);
            isUiPaused = false;
            songDataModel.getIsUpdatedInfo().postValue(false);
        }
        binding.visualizerView.setPlaying(true);
        binding.visualizerView.setVisible(true);
        binding.visualizerView.setPowerSaveMode(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause: ");
        isUiPaused = true;
        binding.visualizerView.setPlaying(false);
        binding.visualizerView.setVisible(false);
        binding.visualizerView.setPowerSaveMode(true);
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
        stopService(PlayerIntent);
        unbindService(connection);
        timer.cancel();
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
                    String body = response.body().string();
                    Gson gson = new GsonBuilder()
                            .disableHtmlEscaping()
                            .setLenient()
                            .serializeNulls()
                            .setPrettyPrinting()
                            .enableComplexMapKeySerialization()
                            .create();
                    dataBean = gson.fromJson(body, SongDataBean.class);
                    future.complete(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    future.complete(false);
                }
            }
        });
        return future;
    }

}