package com.seventeen_acres_development;

/**
 * Created by Dan on 8/13/2016.
 */
public class Integrator {
    private static double leftSum;
    private static double rightSum;
    private static double lastVal;
    private static double lastTimestamp;
    public static void addPoint(double timestamp,double value){
        value=Math.abs(value);
        leftSum+=value*(timestamp-lastTimestamp);
        rightSum+=lastVal*(timestamp-lastTimestamp);
        lastVal=value;
        lastTimestamp=timestamp;
    }
    public static double getArea(){
        return (leftSum+rightSum)/2;
    }
}
