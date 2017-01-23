package com.fsoft.funlight;

import android.util.Log;

import java.io.*;
import java.lang.Process;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Класс содержит полный интсрументарий для работы со светодами
 * !!! Все команды выполнять НЕ в потоке интерфейса !!!
 * Created by Dr. Failov on 21.01.2015.
 */
public class LedController {
    public interface OnChangeBrightnessListener{
        void onChange(float newValue);
    }

    private Process process = null;
    private DataOutputStream STDIN = null;
    private HashMap<String, Boolean> first = new HashMap<String, Boolean>();
    private HashMap<String, Integer> maxes = new HashMap<String, Integer>();
    private HashMap<String, Integer> level = new HashMap<String, Integer>();
    private HashMap<String, OnChangeBrightnessListener> listeners = new HashMap<String, OnChangeBrightnessListener>();

    public List<String> getLEDs(){
        File file = new File("/sys/class/leds");
        String[] files = file.list();
        ArrayList<String> result = new ArrayList<String>();
        if(files != null) {
            for (String s : files) {
                File cur = new File(s);
                result.add(cur.getName());
            }
        }
        return result;
    }
    public float getCurBrightnessCoef(String name){
        return (float)getCurBrightness(name)/(float)getMaxBrightness(name);
    }
    public boolean setBrightnessCoef(String name, float brightness){//0...1
        float max = getMaxBrightness(name);
        if(max > 0) {
            return setBrightness(name, (int) (brightness * max));
        }
        else
            return false;
    }
    public boolean smoothSetBrightnessCoef(String name, float brightness, long time){
        float max = getMaxBrightness(name);
        if(max > 0)
            return smoothSetBrightness(name, (int) (brightness * max), time);
        else
            return false;
    }
    public void setOnChangeBrightnessListener(String ledName, OnChangeBrightnessListener listener){
        listeners.put(ledName, listener);
    }

    public int getMaxBrightness(String name){
        if(maxes.containsKey(name))
            return maxes.get(name);
        else {
            int max = getMaxBrightnessInternal(name);
            maxes.put(name, max);
            return max;
        }
    }
    public int getCurBrightness(String name){
        if(level.containsKey(name))
            return level.get(name);
        else {
            int cur = getCurBrightnessInternal(name);
            level.put(name, cur);
            return cur;
        }
    }
    public boolean setBrightness(String name, int brightness) {//0...MAX
        if(process == null)
            init();
        try {
            if(listeners.containsKey(name))
                listeners.get(name).onChange((float)brightness/(float)getMaxBrightness(name));
            if (!first.containsKey(name)) {
                STDIN.write(("mount -o remount,rw /sys/class/leds/" + name + "\n").getBytes("UTF-8"));
                STDIN.flush();
                first.put(name, true);
            }
            STDIN.write(("echo \"" + brightness + "\" > /sys/class/leds/" + name + "/brightness" + "\n").getBytes("UTF-8"));
            STDIN.flush();
            level.put(name, brightness);
            Log.d("LC", "setBrightness " + name + " " + brightness + " OK.");
            return true;
        } catch (Exception e) {
            Log.d("LC", "setBrightness "+name+" "+brightness+" FAIL.");
            e.printStackTrace();
            return false;
        }
    }
    public boolean smoothSetBrightness(String name, int brightness, long time){
        //плавно довести яркость до brightness за time миллисекунд
        Log.d("LC", "smoothSetBrightness " + name + " " + brightness + " " + time);
        float delay = 100;
        float cur = getCurBrightness(name);
        Log.d("LC", "cur " + cur);
        float aim = brightness;
        float difference = aim - cur;
        Log.d("LC", "difference " + difference);
        if(difference == 0)
            return true;
        float steps = time/delay;
        Log.d("LC", "steps " + steps);
        float step = difference / steps;
        Log.d("LC", "step " + step);
        if(step == 0)
            return true;
        for (float i = cur; Math.signum(aim - i) == Math.signum(difference); i+= step) {
            if(!setBrightness(name, (int)i))
                return false;
            sleep((int)delay);
        }
        setBrightness(name, brightness);
        return true;
    }

    private void sleep(int ms){
        try{
            Thread.sleep(ms);
        }
        catch (Exception e){}
    }
    private boolean init(){
        try {
            Log.d("LC", "Init...");
            process = Runtime.getRuntime().exec("su");
            Log.d("LC", "Get DataOutputStream...");
            STDIN = new DataOutputStream(process.getOutputStream());
            Log.d("LC", "Init OK.");
            return true;
        }
        catch (Exception e){
            Log.d("LC", "Init FAIL.");
            e.printStackTrace();
            return false;
        }
    }
    private int getCurBrightnessInternal(String name){
        if(process == null)
            init();
        Log.d("LC", "getCurBrightnessInternal "+name+"...");
        String data = readFileInternal("/sys/class/leds/" + name + "/brightness");
        if(data == null || data.equals(""))
            return 0;
        try {
            Log.d("LC", "Parsing...");
            return Integer.parseInt(data.replace("\n", "").replace(" ", ""));
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
    private int getMaxBrightnessInternal(String name){
        if(process == null)
            init();
        Log.d("LC", "getCurBrightnessInternal "+name+"...");
        String data = readFileInternal("/sys/class/leds/" + name + "/max_brightness");
        if(data == null || data.equals(""))
            return 0;
        try {
            Log.d("LC", "Parsing...");
            return Integer.parseInt(data.replace("\n", "").replace(" ", ""));
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
    private String readFileInternal(String name){
        try {
            Log.d("LC", "Init...");
            final Process process = Runtime.getRuntime().exec("su");
            Log.d("LC", "Get DataOutputStream...");
            DataOutputStream STDIN = new DataOutputStream(process.getOutputStream());
            Log.d("LC", "read file...");
            final StringBuilder result = new StringBuilder();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            Log.d("LC", "Received: " + line);
                            result.append(line);
                        }
                        reader.close();
                        Log.d("LC", "Reader closed.");
                    } catch (IOException e) {
                    }
                }
            }).start();
            String cmd = "cat \"" + name + "\"\n";
            Log.d("LC", "Sending " + cmd);
            STDIN.write((cmd).getBytes("UTF-8"));
            STDIN.flush();
            Log.d("LC", "Sending exit");
            STDIN.write("exit\n".getBytes("UTF-8"));
            STDIN.flush();
            Log.d("LC", "Waiting...");
            process.waitFor();
            return result.toString();
        }
        catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
