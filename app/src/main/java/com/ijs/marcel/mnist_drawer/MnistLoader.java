package com.ijs.marcel.mnist_drawer;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: vivin
 * Date: 11/11/11
 * Time: 10:07 AM
 */
public class MnistLoader {

    /**
     * the following constants are defined as per the values described at http://yann.lecun.com/exdb/mnist/
     **/

    private static final int MAGIC_OFFSET = 0;
    private static final int OFFSET_SIZE = 4; //in bytes

    private static final int LABEL_MAGIC = 2049;
    private static final int IMAGE_MAGIC = 2051;

    private static final int ITEMS_SIZE = 4;

    private static final int ROWS_SIZE = 4;
    public static final int ROWS = 28;

    private static final int COLUMNS_SIZE = 4;
    public static final int COLUMNS = 28;

    private static final int IMAGE_OFFSET = 16;
    private static final int IMAGE_SIZE = ROWS * COLUMNS;
    private final byte[] labelMagic;
    private final byte[] imageMagic;
    private final int numberOfImages;
    private BufferedInputStream labelInputStream = null;
    private BufferedInputStream imageInputStream = null;
    private byte[] buffer = new byte[2*IMAGE_SIZE];
    private List<MnistImage> images;
    private int MAX_BUFFERED_IMAGES = 100;
    private int currentImage = -1;


    public MnistLoader(InputStream labelFileStream, InputStream imageFileStream, Context context) throws IOException {
        List<MnistImage> images = new ArrayList<MnistImage>();

        labelInputStream = new BufferedInputStream(labelFileStream, 10 * ITEMS_SIZE);
        imageInputStream = new BufferedInputStream(imageFileStream, 10 * IMAGE_SIZE);

        labelInputStream.read(buffer, 0, OFFSET_SIZE);
        labelMagic = Arrays.copyOfRange(buffer, 0, OFFSET_SIZE);
        if (ByteBuffer.wrap(labelMagic).getInt() != LABEL_MAGIC) {
            throw new IOException("Bad adb shell pm uninstall com.ijs.marcel.mnist_drawermagic number in label file!");
        }

        imageInputStream.read(buffer, 0, OFFSET_SIZE);
        imageMagic = Arrays.copyOfRange(buffer, 0, OFFSET_SIZE);
        if (ByteBuffer.wrap(imageMagic).getInt() != IMAGE_MAGIC) {
            throw new IOException("Bad magic number in image file!");
        }


        labelInputStream.read(buffer, 0, OFFSET_SIZE);
        int numberOfLabels = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 0, ITEMS_SIZE)).getInt();

        imageInputStream.read(buffer, 0, OFFSET_SIZE);
        numberOfImages = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 0, ITEMS_SIZE)).getInt();

        if (numberOfImages != numberOfLabels) {
            throw new IOException("The number of labels and images do not match!");
        }

        imageInputStream.read(buffer, 0, ROWS_SIZE + COLUMNS_SIZE);
        int numRows = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 0, ROWS_SIZE)).getInt();
        int numCols = ByteBuffer.wrap(Arrays.copyOfRange(buffer, ROWS_SIZE, ROWS_SIZE + COLUMNS_SIZE)).getInt();
        if (numRows != ROWS && numRows != COLUMNS) {
            throw new IOException("Bad image. Rows and columns do not equal " + ROWS + "x" + COLUMNS);
        }

        this.images = new ArrayList<>();
    }

    public MnistImage getImage(int i) throws IOException {
        Log.w("CurrentImage", String.valueOf(currentImage));
        Log.w("Stored Images", String.valueOf(images.size()));
        Log.w("DesiredImage", String.valueOf(i));
        if (i >= numberOfImages) {
            throw new IndexOutOfBoundsException("Index " + i + "out of bounds. There are only " + numberOfImages + " images.");
        }
        if (currentImage >= i){
            if (currentImage - images.size() <= i){
                return images.get((images.size() - 1)  - (currentImage - i));
            }else{
                labelInputStream.reset();
                imageInputStream.reset();
                currentImage = -1;
                images.clear();
                labelInputStream.read(buffer, 0, 2 * OFFSET_SIZE);
                imageInputStream.read(buffer, 0, IMAGE_OFFSET);
                return getImage(i);
            }
        }
        while (currentImage < i){
            labelInputStream.read(buffer, 0, 1);
            int label = buffer[0];
            imageInputStream.read(buffer, 0, IMAGE_SIZE);
            byte[] imageData = Arrays.copyOfRange(buffer, 0, IMAGE_SIZE);
            images.add(new MnistImage(label, imageData, ROWS, COLUMNS));
            if (images.size() > MAX_BUFFERED_IMAGES){
                images.remove(0);
            }
            currentImage += 1;
        }

        return images.get(images.size() - 1);
    }

    public int getNumberOfImages() {
        return numberOfImages;
    }
}