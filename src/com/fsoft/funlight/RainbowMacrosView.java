package com.fsoft.funlight;

import android.graphics.Color;
import android.view.View;
import android.widget.*;

import java.util.List;

/**
 * макрос крч
 * Created by Dr. Failov on 21.01.2015.
 */
public class RainbowMacrosView extends LinearLayout {
    MainActivity context = null;
    List<String> LEDs = null;
    Spinner spinnerRed;
    Spinner spinnerGreen;
    Spinner spinnerBlue;
    Thread thread = null;
    boolean cont = false;

    public RainbowMacrosView(MainActivity context1, List<String> LEDs1) {
        super(context1);
        this.context = context1;
        this.LEDs = LEDs1;
        setOrientation(VERTICAL);


        TextView textViewName = new TextView(context);
        textViewName.setText("Макрос \"" +    "Радуга"     + "\"");
        textViewName.setTextSize(15);
        textViewName.setTextColor(Color.WHITE);
        addView(textViewName);

        String[] leds = new String[LEDs.size()];
        for (int i = 0; i < LEDs.size(); i++)
            leds[i] = LEDs.get(i);


        {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, leds);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRed = new Spinner(context); // адаптер
            spinnerRed.setAdapter(adapter);
            spinnerRed.setPrompt("Красный диод");
            for (int i = 0; i < leds.length; i++) {
                if(leds[i].toLowerCase().contains("red") ||
                        leds[i].toLowerCase().contains("-r") ||
                        leds[i].toLowerCase().contains("r-") ||
                        leds[i].toLowerCase().contains("r_") ||
                        leds[i].toLowerCase().contains("_r"))
                    spinnerRed.setSelection(i);
            }
            addView(spinnerRed);
        }


        {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, leds);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerGreen = new Spinner(context); // адаптер
            spinnerGreen.setAdapter(adapter);
            spinnerGreen.setPrompt("Зеленый диод");
            for (int i = 0; i < leds.length; i++) {
                if(leds[i].toLowerCase().contains("green") ||
                        leds[i].toLowerCase().contains("-g") ||
                        leds[i].toLowerCase().contains("g-") ||
                        leds[i].toLowerCase().contains("g_") ||
                        leds[i].toLowerCase().contains("_g"))
                    spinnerGreen.setSelection(i);
            }
            addView(spinnerGreen);
        }


        {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, leds);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBlue = new Spinner(context); // адаптер
            spinnerBlue.setAdapter(adapter);
            spinnerBlue.setPrompt("Синий диод");
            for (int i = 0; i < leds.length; i++) {
                if(leds[i].toLowerCase().contains("blue") ||
                        leds[i].toLowerCase().contains("-b") ||
                        leds[i].toLowerCase().contains("b-") ||
                        leds[i].toLowerCase().contains("b_") ||
                        leds[i].toLowerCase().contains("_b"))
                    spinnerBlue.setSelection(i);
            }
            addView(spinnerBlue);
        }

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
        String red = spinnerRed.getSelectedItem().toString();
        String green = spinnerGreen.getSelectedItem().toString();
        String blue = spinnerBlue.getSelectedItem().toString();
        context.status("Включение диода " + red + " ...");
        context.ledController.smoothSetBrightnessCoef(red, 1.0f, 1000);
        while (cont){
            context.status("Включение диода " + green + " ...");
            context.ledController.smoothSetBrightnessCoef(green, 1.0f, 1000);

            context.status("Выключение диода " + red + " ...");
            context.ledController.smoothSetBrightnessCoef(red, 0.0f, 1000);

            context.status("Включение диода " + blue + " ...");
            context.ledController.smoothSetBrightnessCoef(blue, 1.0f, 1000);

            context.status("Выключение диода " + green + " ...");
            context.ledController.smoothSetBrightnessCoef(green, 0.0f, 1000);

            context.status("Включение диода " + red + " ...");
            context.ledController.smoothSetBrightnessCoef(red, 1.0f, 1000);

            context.status("Выключение диода " + blue + " ...");
            context.ledController.smoothSetBrightnessCoef(blue, 0.0f, 1000);


            context.status("Переход к началу ...");
        }
        context.status("Выключение диода " + red + " ...");
        context.ledController.smoothSetBrightnessCoef(red, 0.0f, 1000);
    }
}
