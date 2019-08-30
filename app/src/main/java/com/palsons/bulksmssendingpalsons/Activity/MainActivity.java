package com.palsons.bulksmssendingpalsons.Activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.palsons.bulksmssendingpalsons.Api.RetrofitClient;
import com.palsons.bulksmssendingpalsons.Other.Global;
import com.palsons.bulksmssendingpalsons.R;
import com.palsons.bulksmssendingpalsons.model.DefaultResponse;
import com.palsons.bulksmssendingpalsons.model.MainSmsResponse;
import com.palsons.bulksmssendingpalsons.model.SMSListItem;
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

        mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = mTTS.setLanguage(Locale.US);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Date date = null;
        try {
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = dateFormatter.parse(Global.dateTime);
        } catch (Exception e) {

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
                            Toast.makeText(MainActivity.this, "Something went wrong!!!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, date, 1000 * 60 * Global.delay);
        //timer.schedule(doAsynchronousTask, 0, 3600000);

    }

    private void speak(String text) {

        int speechStatus = mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);

        if (speechStatus == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!");
        }
    }

    private void fetchSmsFromServer() {
        retrofit2.Call<MainSmsResponse> call1 = RetrofitClient.getInstance().getApi().fetchSms(Global.DBPrefix);
        call1.enqueue(new Callback<MainSmsResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onResponse(retrofit2.Call<MainSmsResponse> call1, Response<MainSmsResponse> response) {
                smslist.clear();
                MainSmsResponse res = response.body();
                sim1count = Integer.parseInt(res.getSim1count());
                sim2count = Integer.parseInt(res.getSim2count());
                curdate = res.getCurdate();
                if (res.getSMSList().size() > 0) {
                    smslist = res.getSMSList();
                    sendSMS();
                    saveSmsDateOnServer();
                } else {
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendSMS() {
        for (int i = 0; i < smslist.size(); i++) {
            SMSListItem model = smslist.get(i);
            if (sim1count > 0) {
                sendSmsViaSim1(model.getMobileno(), model.getTextMsg());
                model.setSmsFlag("Y");
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateToStr = format.format(today);
                model.setSmsSendDate(dateToStr);
                model.setSmsSentFromSim("1");
            } else if (sim2count > 0) {
                sendSmsViaSim2(model.getMobileno(), model.getTextMsg());
                model.setSmsFlag("Y");
                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateToStr = format.format(today);
                model.setSmsSendDate(dateToStr);
                model.setSmsSentFromSim("2");
            } else {
                break;
            }
        }
    }

    private void saveSmsDateOnServer() {
        Gson gson = new GsonBuilder().create();
        JsonArray smsjson = gson.toJsonTree(smslist).getAsJsonArray();
        Call<DefaultResponse> call = RetrofitClient.getInstance().getApi().save_send_sms(curdate, smsjson.toString(), Global.DBPrefix);
        call.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                if (!response.body().isError()) {

                    String message = response.body().getErrormsg();// + " and " + emptyInbox() + " SMS deleted.";
                    speak(message);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
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

    /*private int emptyInbox() {
        Uri inboxUri = Uri.parse("content://sms/inbox");
        int count = 0;
        Cursor c = this.getContentResolver().query(inboxUri , null, null, null, null);
        while (c.moveToNext()) {
            try {
                // Delete the SMS
                String pid = c.getString(0); // Get id;
                String uri = "content://sms/" + pid;
                count = this.getContentResolver().delete(Uri.parse(uri),
                        null, null);
            } catch (Exception e) {
            }
        }
        return count;
        *//*
        try {
            mContext.getContentResolver().delete(Uri.parse("content://sms/"), null, null);
        } catch (Exception ex) {
        }*//*
    }*/

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendSmsViaSim1(String sendto, String textsms) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager localSubscriptionManager = SubscriptionManager.from(this);
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();

                SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(0);
                //SubscriptionInfo simInfo2 = (SubscriptionInfo) localList.get(1);

                //SendSMS From SIM One
                SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(sendto, null, textsms, null, null);
                sim1count--;
                //SendSMS From SIM Two
                //SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendTextMessage(sendto, null, textsms, null, null);
            }
        }*/
        /*else {
            SmsManager.getDefault().sendTextMessage(sendto, null, textsms, null, null);

        }*/
        //SmsManager.getSmsManagerForSubscriptionId(0).sendTextMessage(sendto, null, textsms,null, null);
        //if(SimUtil.sendSMS(this,0,sendto,null,textsms,null,null)) {

        //}


        PendingIntent piSent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
        PendingIntent piDelivered = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
        TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        final ArrayList<Integer> simCardList = new ArrayList<>();
        SubscriptionManager subscriptionManager;
        subscriptionManager = SubscriptionManager.from(getApplicationContext());
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        final List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
            int subscriptionId = subscriptionInfo.getSubscriptionId();
            simCardList.add(subscriptionId);
        }

        int smsToSendFrom = simCardList.get(0); //assign your desired sim to send sms, or user selected choice
        SmsManager.getSmsManagerForSubscriptionId(smsToSendFrom).sendTextMessage(sendto, null, textsms, piSent, piDelivered);
        sim1count--;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendSmsViaSim2(String sendto, String textsms) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager localSubscriptionManager = SubscriptionManager.from(this);
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();

                //SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(0);
                SubscriptionInfo simInfo2 = (SubscriptionInfo) localList.get(1);

                //SendSMS From SIM One
                //SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(sendto, null, textsms, null, null);

                //SendSMS From SIM Two
                SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendTextMessage(sendto, null, textsms, null, null);
                sim2count--;
            }
        }*/
        /*else {
            SmsManager.getDefault().sendTextMessage(sendto, null, textsms, null, null);

        }*/
        //if(SimUtil.sendSMS(this,1,sendto,null,textsms,null,null)) {

        //}

        PendingIntent piSent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
        PendingIntent piDelivered = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
        TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        final ArrayList<Integer> simCardList = new ArrayList<>();
        SubscriptionManager subscriptionManager;
        subscriptionManager = SubscriptionManager.from(getApplicationContext());
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        final List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
            int subscriptionId = subscriptionInfo.getSubscriptionId();
            simCardList.add(subscriptionId);
        }

        int smsToSendFrom = simCardList.get(1); //assign your desired sim to send sms, or user selected choice
        SmsManager.getSmsManagerForSubscriptionId(smsToSendFrom).sendTextMessage(sendto, null, textsms, piSent, piDelivered);
        sim2count--;
    }
}
