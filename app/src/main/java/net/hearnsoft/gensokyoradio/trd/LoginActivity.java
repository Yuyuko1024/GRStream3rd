package net.hearnsoft.gensokyoradio.trd;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.hearnsoft.gensokyoradio.trd.beans.LoginDataBean;
import net.hearnsoft.gensokyoradio.trd.databinding.ActivityLoginBinding;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;
import net.hearnsoft.gensokyoradio.trd.widgets.LoginLoadingDialog;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AlertDialog dialog;
    private OkHttpClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化OkHttpClient
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // 减少超时时间
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        setupActionBar();
        setupLoginButton();
        initSubActionButtons();
    }

    private void setupActionBar() {
        setSupportActionBar(binding.topAppbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar, (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBar.top, 0, 0);
            return insets;
        });
    }

    private void setupLoginButton() {
        binding.loginButton.setOnClickListener(v -> {
            String username = binding.username.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (validateInput(username, password)) {
                buildLoginLoadDialog();
                v.setEnabled(false);
                login(username, password);
            }
        });
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            binding.username.setError(getString(R.string.login_error_empty));
            return false;
        }
        if (password.isEmpty()) {
            binding.password.setError(getString(R.string.login_error_empty));
            return false;
        }
        return true;
    }

    private void login(String username, String password) {
        new Thread(() -> {
            try {
                FormBody body = new FormBody.Builder()
                        .add("user", username)
                        .add("pass", password)
                        .build();

                Request request = new Request.Builder()
                        .url(Constants.GR_LOGIN_API_URL)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    handleLoginResponse(response);
                }
            } catch (IOException e) {
                runOnUiThread(() -> {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    showError(getString(R.string.login_error_network));
                });
                Log.e("LoginActivity", "Error logging in", e);
            }
        }).start();
    }

    private void handleLoginResponse(Response response) throws IOException {
        if (response.isSuccessful() && response.body() != null) {
            String responseBody = response.body().string();
            Gson gson = new GsonBuilder().setLenient().create();
            List<LoginDataBean> loginData = gson.fromJson(responseBody, new TypeToken<List<LoginDataBean>>(){}.getType());
            LoginDataBean login = loginData.get(0);

            runOnUiThread(() -> {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

                if (login.getRESULT().equals("SUCCESS")) {
                    saveLoginData(login);
                    finish();
                } else {
                    showError(getString(R.string.login_error_invalid));
                }
            });
        } else {
            runOnUiThread(() -> {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                showError(getString(R.string.login_error_server));
            });
        }
    }

    private void saveLoginData(LoginDataBean login) {
        getSharedPreferences(Constants.PREF_GLOBAL_NAME, MODE_PRIVATE)
                .edit()
                .putString("username", login.getUSERNAME())
                .putString("userid", login.getUSERID())
                .putString("appsessionid", login.getAPPSESSIONID())
                .putString("api", login.getAPI())
                .apply();
    }

    private void showError(String message) {
        binding.username.setError(message);
        binding.password.setError(message);
        binding.loginButton.setEnabled(true);
    }

    private void initSubActionButtons() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        binding.createAccount.setOnClickListener(v -> {
            // Create account
            intent.setData(Uri.parse(Constants.GR_REGISTER_URL));
            startActivity(intent);
        });
        binding.forgotPassword.setOnClickListener(v -> {
            // Forgot password
            intent.setData(Uri.parse(Constants.GR_FORGOT_PASSWORD_URL));
            startActivity(intent);
        });
    }

    private void buildLoginLoadDialog() {
        // Build login loading dialog
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new LoginLoadingDialog(this).create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
