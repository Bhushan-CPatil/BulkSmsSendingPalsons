package com.palsons.bulksmssendingpalsons.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.palsons.bulksmssendingpalsons.Api.RetrofitClient;
import com.palsons.bulksmssendingpalsons.Other.Global;
import com.palsons.bulksmssendingpalsons.Other.ViewDialog;
import com.palsons.bulksmssendingpalsons.R;
import com.palsons.bulksmssendingpalsons.model.DefaultResponse;
import com.palsons.bulksmssendingpalsons.model.TimeDelayResponse;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText uid, pass;
    Button login;
    ViewDialog progress;
    boolean allgranted = false;

    @Override
    protected void onStart() {
        super.onStart();
        requestStoragePermission();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Global.clearGlobal();
        progress = new ViewDialog(this);
        uid = findViewById(R.id.uid);
        pass = findViewById(R.id.pass);
        login = findViewById(R.id.login);
        getData();
    }

    public void DoLogin(View view) {
        Vibrator vibrator = (Vibrator) getSystemService(LoginActivity.this.VIBRATOR_SERVICE);
        vibrator.vibrate(200);

        if (allgranted) {

            final String lid = uid.getText().toString().trim();
            String password = pass.getText().toString().trim();

            if (lid.isEmpty()) {
                uid.setError("Username is required");
                uid.requestFocus();
                return;
            }

            if (lid.length() < 5) {
                uid.setError("Enter a valid Username");
                uid.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                pass.setError("Password is required");
                pass.requestFocus();
                return;
            }

            if(lid.equalsIgnoreCase(Global.username)){
                if(password.equalsIgnoreCase(Global.password)){

                    checkLogin();
                }else{
                    Toast.makeText(this, "Wrong Password !", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Wrong Username !", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestStoragePermission();
        }
    }

    private void getData(){
        progress.show();
        Call<TimeDelayResponse> call = RetrofitClient.getInstance().getApi().DateTimeAndDelay();
        call.enqueue(new Callback<TimeDelayResponse>() {

            @Override
            public void onResponse(Call<TimeDelayResponse> call, Response<TimeDelayResponse> response) {
                TimeDelayResponse res = response.body();
                progress.dismiss();
                Global.dateTime = res.getDatetime();
                Global.delay = Integer.parseInt(res.getRepeatAfter());
                Global.username = res.getId();
                Global.password = res.getPassword();
            }

            @Override
            public void onFailure(Call<TimeDelayResponse> call, Throwable t) {
                progress.dismiss();
                if (t instanceof IOException) {
                    Toast.makeText(LoginActivity.this, "Internet Issue ! Failed to process your request !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Data Conversion Issue ! Contact to admin", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkLogin() {
        Call<DefaultResponse> call = RetrofitClient.getInstance().getApi().checkLogin(Global.DBPrefix);
        call.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                if (!response.body().isError()) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, response.body().getErrormsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Failed to login !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestStoragePermission() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.SEND_SMS
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            //Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                            allgranted = true;
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
}
