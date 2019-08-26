package com.example.bulksmssendingpalsons.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.example.bulksmssendingpalsons.Api.RetrofitClient;
import com.example.bulksmssendingpalsons.Other.Global;
import com.example.bulksmssendingpalsons.Other.SimUtil;
import com.example.bulksmssendingpalsons.R;
import com.example.bulksmssendingpalsons.model.DefaultResponse;
import com.example.bulksmssendingpalsons.model.MainSmsResponse;
import com.example.bulksmssendingpalsons.model.SMSListItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    int sim1count, sim2count;
    public List<SMSListItem> smslist = new ArrayList<>();
    public String curdate = "";
    public Context mContext;
    private TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.GERMAN);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        //speak();
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });


        Date date = null;
        try {
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = dateFormatter.parse(Global.dateTime);
        }catch (Exception e){

        }

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            fetchSmsFromServer();
                        } catch (Exception e) {
                            speak("Something went wrong!!!");
                            Toast.makeText(MainActivity.this, "Something went wrong!!!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, date, 1000 * 60 * 60 * Global.delay);
        //timer.schedule(doAsynchronousTask, 0, 3600000);

    }

    private void speak(String text) {

        /*mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);*/

        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void fetchSmsFromServer() {
        retrofit2.Call<MainSmsResponse> call1 = RetrofitClient.getInstance().getApi().fetchSms(Global.DBPrefix);
        call1.enqueue(new Callback<MainSmsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<MainSmsResponse> call1, Response<MainSmsResponse> response) {
                smslist.clear();
                MainSmsResponse res =  response.body();
                sim1count = Integer.parseInt(res.getSim1count());
                sim2count = Integer.parseInt(res.getSim2count());
                curdate = res.getCurdate();
                if(res.getSMSList().size()>0){
                    smslist = res.getSMSList();
                    sendSMS();
                    saveSmsDateOnServer();
                }else{
                    speak("SMS List is empty !");
                    Toast.makeText(MainActivity.this, "SMS List is empty !", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MainSmsResponse> call1, Throwable t) {
                speak("Failed to fetch SMS from server !");
                Toast.makeText(MainActivity.this, "Failed to fetch SMS from server !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSMS() {
        for(int i=0; i<smslist.size(); i++){
            SMSListItem model = smslist.get(i);
            if(sim1count>0){
                sendSmsViaSim1(model.getMobileno(),model.getTextMsg());
                model.setSmsFlag("Y");
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateToStr = format.format(today);
                model.setSmsSendDate(dateToStr);
                model.setSmsSentFromSim("1");
            }else if(sim2count>0){
                sendSmsViaSim2(model.getMobileno(),model.getTextMsg());
                model.setSmsFlag("Y");
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateToStr = format.format(today);
                model.setSmsSendDate(dateToStr);
                model.setSmsSentFromSim("2");
            }else{
                break;
            }
        }
    }

    private void saveSmsDateOnServer() {
        Gson gson = new GsonBuilder().create();
        JsonArray smsjson = gson.toJsonTree(smslist).getAsJsonArray();
        Call<DefaultResponse> call = RetrofitClient.getInstance().getApi().save_send_sms(curdate,smsjson.toString(),Global.DBPrefix);
        call.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                if(response.body().isError()) {
                    emptyInbox();
                    speak("Process Complete Successfully");
                    Toast.makeText(MainActivity.this, "Process Complete Successfully", Toast.LENGTH_SHORT).show();
                }else{
                    speak(response.body().getErrormsg());
                    Toast.makeText(MainActivity.this, response.body().getErrormsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {
                speak("Failed to Update sent SMS data !");
                Toast.makeText(MainActivity.this, "Failed to Update sent SMS data !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void emptyInbox() {
        try {
            mContext.getContentResolver().delete(Uri.parse("content://sms/"), null, null);
        } catch (Exception ex) {   }
    }

    private void sendSmsViaSim1(String sendto, String textsms) {
        SimUtil.sendSMS(this,0,sendto,null,textsms,null,null);
        sim1count--;
    }

    private void sendSmsViaSim2(String sendto, String textsms) {
        SimUtil.sendSMS(this,1,sendto,null,textsms,null,null);
        sim2count--;
    }
}
