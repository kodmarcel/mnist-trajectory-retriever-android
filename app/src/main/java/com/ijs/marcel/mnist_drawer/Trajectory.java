package com.ijs.marcel.mnist_drawer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by marcel on 11/19/17.
 */

public class Trajectory {
    private List<float[]> points;

    public Trajectory() {
        this.points = new ArrayList<float[]>();
    }

    public void addPoint(float x, float y, float time){
        points.add(new float[]{x,y,time});
    }

    public List<float[]> getTrajectory(){
        return points;
    }

    public String getAsString(){
        String string = "";
        for (float[] point:points){
            string += Arrays.toString(point);
            if (points.indexOf(point) < points.size() - 1) {
                string += ", ";
            }
        }
        return string;
    }

    public void reScale(int scale) {
        for (int i = 0; i < points.size(); i++){
            float[] point = points.get(i);
            float[] scaledPoint = new float[]{point[0] / scale * 28, point[1] / scale * 28, point[2]};
            points.set(i, scaledPoint);
        }
    }
}