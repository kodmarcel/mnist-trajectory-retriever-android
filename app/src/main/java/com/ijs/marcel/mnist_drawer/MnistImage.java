package com.ijs.marcel.mnist_drawer;


import android.util.Log;

/**
 * Created by marcel on 10/25/17.
 */

public class MnistImage {
    private int label;
    private int[] imageData;
    private int rows;
    private int columns;

    public MnistImage(int label, byte[] imageData, int rows, int columns) {
        this.label = label;
        this.imageData = new int[rows * columns];
        this.rows = rows;
        this.columns = columns;

        for (int i = 0; i < rows * columns; i++){
            this.imageData[i] = imageData[i];
        }
    }

    public int[] getImageData() {
        return imageData;
    }

    public int getLabel() {
        return label;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }
}
