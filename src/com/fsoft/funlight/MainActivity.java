package com.fsoft.funlight;

import android.app.Activity;
import android.os.*;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    public LedController ledController = new LedController();
    private ArrayList<LedView> ledViews = new ArrayList<LedView>();
    List<String> LEDs = null;
    private MainActivity context = this;
    private Toast toast = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLedList();
    }
    public void loadLedList(){
        status("Загрузка...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                sleep(1000);
                LEDs = ledController.getLEDs();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status("Готов.");
                        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layoutLEDs);
                        if(linearLayout == null) {
                            status("Ошибка.");
                            return;
                        }
                        if(linearLayout.getChildCount() > 0)
                            return;
                        if(LEDs.size() > 0) {
                            for (String led : LEDs) {
                                LedView ledView = new LedView(context, led);
                                ledViews.add(ledView);
                                linearLayout.addView(ledView);
                            }
                            addMacroses(linearLayout);
                        }
                        else {
                            TextView textView = new TextView(context);
                            textView.setText("Диодов в системе не найдено=(");
                            linearLayout.addView(textView);
                        }
                    }
                });
            }
        }).start();
    }
    public void addMacroses(LinearLayout linearLayout){
        linearLayout.addView(new BreatheMacrosView(context, LEDs));
        linearLayout.addView(new RainbowMacrosView(context, LEDs));

    }
    private void sleep(int ms){
        try{
            Thread.sleep(ms);
        }
        catch (Exception e){}
    }
    public void status(final String text){
        Log.d("FL", "mes: " + text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.textViewStatus)).setText(text);
            }
        });
    }
    public void show(final String text){
        Log.d("FL", "mes: " + text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
}
