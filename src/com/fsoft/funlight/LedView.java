package com.fsoft.funlight;

import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * класс обрабатывающий зону одного диода
 * Created by Dr. Failov on 21.01.2015.
 */
public class LedView extends LinearLayout {
    private String ledName = null;
    private MainActivity context = null;
    private int seekMax = 255;
    private int DPI = 200;
    private TextView textViewName;
    private SeekBar seekBarBrightness;

    public LedView(final MainActivity in_context, final String in_ledName) {
        super(in_context);
        this.context = in_context;
        this.ledName = in_ledName;
        setOrientation(VERTICAL);
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        DPI = dm.densityDpi;
        textViewName = new TextView(context);
        textViewName.setText(ledName);
        textViewName.setTextSize(15);
        textViewName.setTextColor(Color.WHITE);
        addView(textViewName);

        seekBarBrightness = new SeekBar(context);
        seekBarBrightness.setMax(seekMax);
        seekBarBrightness.setPadding(DPI / 5, 0, DPI / 5, DPI / 6);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int pos = (int)(context.ledController.getCurBrightnessCoef(ledName) * seekMax);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBarBrightness.setProgress(pos);
                    }
                });
            }
        }).start();
        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float actualData = (float)i/(float)seekMax;//0...1
                if(b)
                    context.ledController.setBrightnessCoef(ledName, actualData);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        addView(seekBarBrightness);

        context.ledController.setOnChangeBrightnessListener(ledName, new LedController.OnChangeBrightnessListener() {
            @Override
            public void onChange(float newValue) {
                //Log.d("LIS", "Received " + newValue);
                seekBarBrightness.setProgress((int)(newValue * seekMax));
            }
        });
    }
}
