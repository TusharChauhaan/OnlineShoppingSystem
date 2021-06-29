package com.bhavin.onlinecityshop;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProductManagementActivity extends AppCompatActivity {

    public LinearLayout layout;
    public Button addNewProduct;
    public ImageButton back;
    public ArrayList<Product> products = new ArrayList<>();
    public AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        layout = findViewById(R.id.product_container);
        addNewProduct = findViewById(R.id.add_new_product);
        back = findViewById(R.id.back);

        /**
         * Loading Dialog
         */

        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view)
                .setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        addNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), AddProductActivity.class);
                startActivity(i);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fetchData();
    }

    public void addProducts(){
        layout.removeAllViews();
        for(int i=0;i<products.size();i++){
            View view = getLayoutInflater().inflate(R.layout.product_mgmt_card, null);

            view.findViewById(R.id.product_card_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            Product product = products.get(i);

            ImageView photo = view.findViewById(R.id.product_photo);
//        photo.setImageBitmap();
            TextView name = view.findViewById(R.id.product_name);
            TextView id = view.findViewById(R.id.product_id);

            photo.setImageBitmap(product.getPhoto());
            name.setText(product.getName());
            id.setText("Product Id "+product.getId());

            layout.addView(view);
        }
    }

    public void fetchData(){
        new Task().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(UserData.productUpdated){
            fetchData();
            UserData.productUpdated = false;
        }
    }

    class Task extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            addProducts();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... strings) {

            try{
                Class.forName("com.mysql.jdbc.Driver");

                try(
                        Connection connection = DriverManager.getConnection(UserData.URL,
                                UserData.USER, UserData.PASSWORD)
                ) {
                    products = Seller.fetchProductsOfSeller(UserData.seller.getSellerId(), connection);
                }

            } catch (ClassNotFoundException | SQLException e) {
                publishProgress();
                e.printStackTrace();
            }
            return null;
        }
    }
}