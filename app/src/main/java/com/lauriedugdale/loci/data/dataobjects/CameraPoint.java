package com.lauriedugdale.loci.data.dataobjects;

/**
 * Created by mnt_x on 08/07/2017.
 */

public class CameraPoint {

    private float x;
    private float y;
    private GeoEntry entry;

    private float [] xArray;
    private int xCount;

    public CameraPoint(GeoEntry entry) {
        this.entry = entry;
        xArray = new float [5];
    }
    public CameraPoint(float x, float y, GeoEntry entry) {
        this.x = x;
        this.y = y;
        this.entry = entry;

        xArray = new float [5];
    }

    public void setX(float x) {

//        if (xCount < 5) {
////            System.out.println("mCount for");
//            xArray[xCount] = x;
//            xCount++;
//        } else {
//            xCount = 0;
//        }
//        float sum = 0f;
//        int num = 0;
//        for (float f : xArray){
//            sum += f;
//            num ++;
//        }

        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public GeoEntry getEntry() {
        return entry;
    }

}
