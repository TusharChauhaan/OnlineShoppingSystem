package com.bhavin.onlinecityshop;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AddProductActivity extends AppCompatActivity {

    EditText name, description, price, availableUnits;
    Button add, choose, cancel;
    ImageView photo;
    Bitmap photoBitmap = null;
    AlertDialog loading;
    Toast error, success;

    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        name = findViewById(R.id.p_name);
        description = findViewById(R.id.p_description);
        price = findViewById(R.id.p_price);
        availableUnits = findViewById(R.id.p_available_units);

        add = findViewById(R.id.f_add);
        choose = findViewById(R.id.choose);
        cancel = findViewById(R.id.f_cancel);

        photo = findViewById(R.id.product_photo);

        /**
         * Alert Dialog Building
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.loading_dialog, null));
        loading = builder.create();
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        /**
         * Error Toast
         */
        error = Toast.makeText(this, "Error while adding data", Toast.LENGTH_LONG);

        /**
         * Success Toast
         */
        success = Toast.makeText(this, "Product Added", Toast.LENGTH_LONG);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(photoBitmap == null){
                    Toast.makeText(getApplicationContext(),
                            "Please pick a photo", Toast.LENGTH_LONG).show();
                }else{
                    Product product = new Product();
                    product.setPhoto(photoBitmap);
                    product.setName(name.getText().toString());
                    product.setDescription(description.getText().toString());
                    product.setPrice(Float.parseFloat(price.getText().toString()));
                    product.setAvailableUnits(Integer.parseInt(availableUnits.getText().toString()));

                    new Task().execute(product);
                }
            }
        });

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent,"Choose Picture"), PICK_IMAGE);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK){
            try{
                photoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            photo.setImageBitmap(photoBitmap);
        }
    }

    public void popActivity(){
        onBackPressed();
    }

    /**
     *****************************  Task    *******************************************
     */

    class Task extends AsyncTask<Product, Void, Void>{

        boolean isSuccess = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!isSuccess){
                error.show();
            }else{
                success.show();
                popActivity();
            }
            loading.dismiss();
        }

        @Override
        protected Void doInBackground(Product... products) {

            try{
                Class.forName("com.mysql.jdbc.Driver");

                try(
                        Connection con = DriverManager.getConnection(UserData.URL,
                                UserData.USER, UserData.PASSWORD)
                ){

                    isSuccess = Product.insertProduct(products[0],
                            UserData.user.getContact(), con);
                    UserData.productUpdated = true;

                } catch (SQLException | IOException throwables) {
                    throwables.printStackTrace();
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}