package com.bhavin.onlinecityshop;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SellerDetailsManagement extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private ImageView banner;
    private EditText name, desc, address, service_city, contactNo, gstNo;
    private Button update, choose;
    private AlertDialog dialog;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_details_management);

        ImageButton back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        banner = findViewById(R.id.image_banner);
        name = findViewById(R.id.f_name);
        desc = findViewById(R.id.f_description);
        address = findViewById(R.id.f_address);
        service_city = findViewById(R.id.f_service_city);
        contactNo = findViewById(R.id.f_contact_no);
        gstNo = findViewById(R.id.f_gst_no);

        update = findViewById(R.id.f_update);
        choose = findViewById(R.id.choose);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Seller seller = new Seller();
                seller.setShopName(name.getText().toString());
                seller.setGstNo(gstNo.getText().toString());
                seller.setServiceCity(service_city.getText().toString());
                seller.setAddress(address.getText().toString());
                seller.setContactNumber(contactNo.getText().toString());
                seller.setDescription(desc.getText().toString());
                seller.setSellerId(UserData.user.getContact());
                if(bitmap != null){
                    /**
                     * No photo selected by choose button
                     */
                    seller.setBanner(bitmap);
                }else{
                    seller.setBanner(UserData.seller.getBanner());
                }

                new UpdateTask().execute(seller);
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

        /**
         * Loading Dialog
         */

        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view)
                .setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setData();
    }

    public void setData(){
        Seller seller = UserData.seller;

        banner.setImageBitmap(seller.getBanner());
        name.setText(seller.getShopName());
        desc.setText(seller.getDescription());
        address.setText(seller.getAddress());
        service_city.setText(seller.getServiceCity());
        contactNo.setText(seller.getContactNumber());
        gstNo.setText(seller.getGstNo());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK){
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            banner.setImageBitmap(bitmap);
        }
    }

    class UpdateTask extends AsyncTask<Seller, Seller, Seller>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected void onPostExecute(Seller seller) {
            super.onPostExecute(seller);
            dialog.dismiss();
            setData();
        }

        @Override
        protected void onProgressUpdate(Seller... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Seller doInBackground(Seller... sellers) {
            Seller seller = sellers[0];

            try{
                Class.forName("com.mysql.jdbc.Driver");

                try(
                        Connection connection = DriverManager.getConnection(UserData.URL,
                                UserData.USER, UserData.PASSWORD)
                ) {
                    Seller.updateSeller(seller, connection);
                    UserData.seller = seller;
                }

            } catch (ClassNotFoundException | SQLException | IOException e) {
                publishProgress();
            }

            return null;
        }
    }
}