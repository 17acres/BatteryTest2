package com.seventeen_acres_development;

/**
 * Created by Dan on 3/16/2019.
 */
public class ArduinoConverters {
    static double openLoopResistance=14.0/50.0;
    static int ISENSEBASE;
    static double convertVoltage(int rawVoltage){
        int VDIVOFFSET=0;
        double VDIVSCALE=11.7/736.8;
        rawVoltage+=VDIVOFFSET;
        return VDIVSCALE*(double)rawVoltage;
    }

    static double convertCurrent(int rawVoltage){
		double fracChange=(double)rawVoltage/(double)ISENSEBASE-1;
        double ISENSESCALE=10.19;
        return fracChange*ISENSESCALE;
    }
}
