package com.bhavin.onlinecityshop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

public abstract class PhotoHelper {

    /**
     * This method helps us to convert a blob object to a bitmap
     * Takes blob as input and returns a bitmap
     */
    public static Bitmap convertBlobToBitmap(Blob blob) throws SQLException {
        byte array[] = blob.getBytes(1, (int)blob.length());
        Bitmap bitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
        return bitmap;
    }

    /**
     * This method will take bitmap as input and compress and
     * convert it to input stream so that we can upload the photo
     * to our data base
     */

    public static ByteArrayInputStream compressAndConvertToIS(Bitmap bitmap) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ByteArrayOutputStream size = new ByteArrayOutputStream();
        /**
         * This compress method compute bitmap to size stream
         */
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, size);
        int quality = 40000000/(size.toByteArray().length);

        if(quality<10){
            quality=10;
        }else if(quality > 100){
            quality = 100;
        }
        size.close();

        /**
         * This time compress method will compress the bitmap
         */
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        byte array[] = stream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(array);

        return inputStream;
    }
}
