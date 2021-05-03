package com.seventeen_acres_development;

import com.fazecast.jSerialComm.SerialPort;
import com.sun.deploy.net.DownloadEngine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.seventeen_acres_development.BattTest.startTime;

/**
 * Created by Dan on 3/16/2019.
 */
public class ArduinoInterface implements Runnable{
    boolean isEnabled;
    private SerialPort serialPort;
    private StringBuilder inputStringBuilder;
    private InputStream inputStream;
    private ScheduledExecutorService executor;
    private double voltage,current;

    public boolean isLoadEnable() {
        return loadEnable;
    }

    private boolean loadEnable;

    @Override
    public void run() {
        if(isEnabled){
            serialPort.writeBytes("OPENLOOP\n".getBytes(StandardCharsets.US_ASCII),9);
        }
        try {
            while (inputStream.available()>0){
                char input=(char)inputStream.read();
                if(input!='\n') {
                    inputStringBuilder.append(input);
                }else{
                    if(inputStringBuilder.length()==13)
                        parseString(inputStringBuilder.toString());
                    inputStringBuilder.setLength(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static ArduinoInterface ourInstance = new ArduinoInterface();



    public static ArduinoInterface getInstance() {
        return ourInstance;
    }

    private ArduinoInterface() {
        inputStringBuilder=new StringBuilder();
    }

    void setComPort(int port) {
        serialPort=SerialPort.getCommPort("COM"+port);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING,0,0);
        serialPort.setBaudRate(250000);
        serialPort.openPort();
        inputStream=serialPort.getInputStream();

    }

    void startTest(){
        startTime=System.currentTimeMillis();
        isEnabled=true;

    }
    void startReading(){
        executor=Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this, 100 - (System.currentTimeMillis() % 100), 100, TimeUnit.MILLISECONDS);
    }

    void endTest(){
        isEnabled=false;
    }

    double getVoltage(){
        return voltage;
    }

    double getCurrent(){
        return current;
    }
    double getPower(){
        return voltage*current;
    }

    /*
    Status String Format:
    Voltage Current RCEnable LoadEnable PIDsp
     VVVV IIII R L SET
     */
    void parseString(String s){
        try {
            Scanner scanner = new Scanner(s);
            voltage = ArduinoConverters.convertVoltage(scanner.nextInt());
            int rawCurrent=scanner.nextInt();
			if (ArduinoConverters.ISENSEBASE==0) {
                ArduinoConverters.ISENSEBASE = rawCurrent;//Calibrate based on first value
                System.out.println("ISENSE Base value set to: " + ArduinoConverters.ISENSEBASE);
            }
            if(loadEnable|current>.2)//hide my mistakes but prevent fires
                current = ArduinoConverters.convertCurrent(rawCurrent);
            else
                current=0;
            boolean rcEnable = scanner.nextInt() == 1;
            loadEnable = scanner.nextInt() == 1;
            //System.out.println("RC Enable: " + rcEnable + " Load Enable: " + loadEnable);


        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
