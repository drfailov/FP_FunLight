package com.fsoft.funlight;

import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.*;

import java.util.List;

/**
 * макрос крч
 * Created by Dr. Failov on 21.01.2015.
 */
public class BreatheMacrosView extends LinearLayout {
    MainActivity context = null;
    List<String> LEDs = null;
    Spinner spinner;
    Thread thread = null;
    boolean cont = false;

    public BreatheMacrosView(MainActivity context1, List<String> LEDs1) {
        super(context1);
        this.context = context1;
        this.LEDs = LEDs1;
        setOrientation(VERTICAL);


        TextView textViewName = new TextView(context);
        textViewName.setText("Макрос \"" +    "Дыхание"     + "\"");
        textViewName.setTextSize(15);
        textViewName.setTextColor(Color.WHITE);
        addView(textViewName);


        String[] leds = new String[LEDs.size()];
        for (int i = 0; i < LEDs.size(); i++)
            leds[i] = LEDs.get(i);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, leds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = new Spinner(context); // адаптер
        spinner.setAdapter(adapter);
        spinner.setPrompt("Диод для макроса");
        addView(spinner);


        Button button = new Button(context);
        button.setText("Старт\\Стоп");
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(thread == null)
                    start();
                else
                    stop();
            }
        });
        addView(button);
    }
    void start(){
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                runAsync();
            }
        });
        cont = true;
        thread.start();
        context.status("Макрос запущен");
    }
    void stop(){
        cont = false;
        thread = null;
        context.status("Остановка макроса...");
    }
    void runAsync(){
        String led = spinner.getSelectedItem().toString();
        while (cont){
            context.status("Включение диода " + led + " ...");
            context.ledController.smoothSetBrightnessCoef(led, 1.0f, 3000);
            context.status("Выключение диода " + led + " ...");
            context.ledController.smoothSetBrightnessCoef(led, 0.0f, 3000);
            context.status("Переход к началу ...");
        }
    }
}
