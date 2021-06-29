package com.bhavin.onlinecityshop;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class OrderDetails extends AppCompatActivity {

    RelativeLayout layout;
    public static final int CONSUMER_VIEW = 0;
    public static final int SELLER_VIEW = 1;
    AlertDialog dialog, ratingDialog;
    int choice;
    Seller seller;
    Order order;
    ArrayList<Product> items = new ArrayList<>();
    Address address;
    int orderId;
    float sellerRatingValue = 5f;
    float []productRatings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        /**
         * Get order id from intent
         */
        orderId = getIntent().getIntExtra("orderId",-1);

        /**
         * Loading Dialog
         */
        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view)
                .setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        layout = findViewById(R.id.container);

        choice = getIntent().getIntExtra("choice",0);

        new Task().execute();
    }

    public void consumerView(){
        View view = getLayoutInflater().inflate(R.layout.consumer_order_view, null);
        ImageView shopBanner = view.findViewById(R.id.shop_image);
        shopBanner.setClipToOutline(true);
        TextView shopName = view.findViewById(R.id.shop_name_order);
        TextView shopAddress = view.findViewById(R.id.shop_address);

        TextView consumerName = view.findViewById(R.id.consumer_name);
        TextView deliveryAddress = view.findViewById(R.id.delivery_address);

        TextView date = view.findViewById(R.id.order_date);

        TextView orderStatus = view.findViewById(R.id.order_status);
        TextView paymentStatus = view.findViewById(R.id.payment_status);

        LinearLayout orderContainer = view.findViewById(R.id.order_items_container);

        addItems(orderContainer);

        TextView totalPrice = view.findViewById(R.id.item_total);
        TextView deliveryCharge = view.findViewById(R.id.delivery_charge);
        TextView totalAmount = view.findViewById(R.id.total_amount);
        TextView itemsCount = view.findViewById(R.id.item_count);

        shopName.setText(seller.getShopName());
        shopAddress.setText(seller.getAddress());
        shopBanner.setImageBitmap(seller.getBanner());

        consumerName.setText(order.getConsumerName());
        deliveryAddress.setText("Mo: "+address.getPhoneNo()+"\n"+address.getStreet()+", "+address.getCity());
        date.setText(order.getDateAndtime());

        orderStatus.setText(order.getDecodedDeliveryStatus());
        paymentStatus.setText(order.getDecodedPaymentStatus());

        float itemTotal = getTotalPrice();
        int itemCounted = getTotalItemsCount();

        totalPrice.setText(itemTotal+"");
        itemsCount.setText(itemCounted+"");
        deliveryCharge.setText("20");
        totalAmount.setText(itemTotal+20+"");

        Button cancelOrder = view.findViewById(R.id.cancel_order);
        if(order.getDeliveryStatus() != Order.ORDER_BOOKED){
            cancelOrder.setVisibility(View.GONE);
        }

        cancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CancelOrderTask().execute(order);
            }
        });

        Button rate = view.findViewById(R.id.rate);
        rate.setVisibility(View.GONE);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        if(order.getDeliveryStatus() == Order.DELIVERED){
            rate.setVisibility(View.VISIBLE);
        }

        layout.addView(view);
    }

    private void showRatingDialog() {
        productRatings = new float[items.size()];
        Arrays.fill(productRatings, 5f);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        /**
         * Inflate the view
         */
        View view = getLayoutInflater().inflate(R.layout.rating_dialog, null);
        final RatingBar sellerRate = view.findViewById(R.id.rating_bar);
        sellerRate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                sellerRatingValue = rating;
            }
        });

        LinearLayout layout = view.findViewById(R.id.container);
        TextView sellerName = view.findViewById(R.id.seller_name);
        sellerName.setText(seller.getShopName());
        ImageView sellerPhoto = view.findViewById(R.id.sellerPhoto);
        sellerPhoto.setImageBitmap(seller.getBanner());

        addProductsRating(layout);

        view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RatingTask().execute();
                ratingDialog.dismiss();
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingDialog.dismiss();
            }
        });

        builder.setView(view);
        ratingDialog = builder.create();
        ratingDialog.show();
    }

    private void addProductsRating(LinearLayout layout) {
        for(int i=0;i<items.size();i++){
            View view = getLayoutInflater().inflate(R.layout.product_rating, null);

            RatingBar ratingBar = view.findViewById(R.id.rating_bar);
            TextView productName = view.findViewById(R.id.product_name);
            ImageView photo = view.findViewById(R.id.img);
            photo.setClipToOutline(true);

            final int index = i;

            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    productRatings[index] = rating;
                }
            });

            productName.setText(items.get(i).getName());
            photo.setImageBitmap(items.get(i).getPhoto());

            layout.addView(view);
        }
    }

    public void sellerView(){
        View view = getLayoutInflater().inflate(R.layout.seller_order_view, null);

        TextView consumerName = view.findViewById(R.id.consumer_name);
        TextView deliveryAddress = view.findViewById(R.id.delivery_address);

        TextView date = view.findViewById(R.id.order_date);

        TextView orderStatus = view.findViewById(R.id.order_status);
        TextView paymentStatus = view.findViewById(R.id.payment_status);

        LinearLayout orderContainer = view.findViewById(R.id.order_items_container);

        addItems(orderContainer);

        TextView totalPrice = view.findViewById(R.id.item_total);
        TextView deliveryCharge = view.findViewById(R.id.delivery_charge);
        TextView totalAmount = view.findViewById(R.id.total_amount);
        TextView itemsCount = view.findViewById(R.id.item_count);

        consumerName.setText(order.getConsumerName());
        deliveryAddress.setText("Mo: "+address.getPhoneNo()+"\n"+address.getStreet()+", "+address.getCity());
        date.setText(order.getDateAndtime());

        orderStatus.setText(order.getDecodedDeliveryStatus());
        paymentStatus.setText(order.getDecodedPaymentStatus());

        float itemTotal = getTotalPrice();
        int itemCounted = getTotalItemsCount();

        totalPrice.setText(itemTotal+"");
        itemsCount.setText(itemCounted+"");
        deliveryCharge.setText("20");
        totalAmount.setText(itemTotal+20+"");

        layout.addView(view);
    }

    private void addItems(LinearLayout container) {
        for(int i=0;i<items.size();i++){
            View view = getLayoutInflater().inflate(R.layout.bill_item, null);

            Product p = items.get(i);

            ImageView photo = view.findViewById(R.id.item_photo);
            TextView itemName, itemProceCount, totalItemPrice;
            itemName = view.findViewById(R.id.item_name);
            itemProceCount = view.findViewById(R.id.item_price_count);
            totalItemPrice = view.findViewById(R.id.total_item_price);

            itemName.setText(p.getName());
            itemProceCount.setText(p.getQuantity()+" X Rs "+p.getPrice());
            totalItemPrice.setText(p.getQuantity()*p.getPrice()+"");
            photo.setImageBitmap(p.getPhoto());
            photo.setClipToOutline(true);

            container.addView(view);
        }
    }

    private int getTotalItemsCount(){
        int sum = 0;
        for(int i=0;i<items.size();i++){
            sum = sum + items.get(i).getQuantity();
        }
        return sum;
    }

    private float getTotalPrice(){
        float sum = 0;
        for(int i=0;i<items.size();i++){
            sum = sum + items.get(i).getQuantity()*items.get(i).getPrice();
        }
        return sum;
    }

    class Task extends AsyncTask<String, String, String>{

        boolean success = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            if(success){
                if(choice == CONSUMER_VIEW){
                    consumerView();
                }else{
                    sellerView();
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(), "Something went wrong",Toast.LENGTH_LONG).show();
            onBackPressed();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{

                Class.forName("com.mysql.jdbc.Driver");

                try(
                        Connection con = DriverManager.getConnection(UserData.URL, UserData.USER, UserData.PASSWORD)
                ){

                    order = Order.fetchOrderDetails(orderId, con);
                    address = Order.fetchDeliveryAddress(orderId, con);
                    if(choice == CONSUMER_VIEW){
                        seller = Order.fetchSellerOfOrder(orderId, con);
                    }
                    items = Order.fetchItemsInOrder(orderId, con);
                    success = true;

                } catch (SQLException throwables) {
                    publishProgress();
                    throwables.printStackTrace();
                }

            }catch (ClassNotFoundException e){
                publishProgress();
            }
            return null;
        }
    }

    class CancelOrderTask extends AsyncTask<Order, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            onBackPressed();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Order... orders) {

            try{

                Class.forName("com.mysql.jdbc.Driver");

                try(
                        Connection con = DriverManager.getConnection(UserData.URL, UserData.USER, UserData.PASSWORD)
                ){

                    Order.changeDeliveryStatus(orders[0].getId(), Order.CANCELLED, con);
                    UserData.getOrder(orders[0].getId()).setDeliveryStatus(Order.CANCELLED);

                } catch (SQLException throwables) {
                    publishProgress();
                    throwables.printStackTrace();
                }

            }catch (ClassNotFoundException e){
                publishProgress();
            }

            return null;
        }
    }

    class RatingTask extends AsyncTask<Void, Void, Void>{

        boolean success = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if(success){
                Toast.makeText(getApplicationContext(), "Thanks For your Feedback", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(), "Something Went Wrong", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try{
                Class.forName("com.mysql.jdbc.Driver");

                try(
                        Connection connection = DriverManager.getConnection(UserData.URL, UserData.USER, UserData.PASSWORD)
                        ){
                    Seller.addRating(seller, sellerRatingValue, connection);

                    for (int i = 0; i < items.size(); i++) {
                        items.get(i).addRating(productRatings[i], connection);
                    }
                    success = true;
                }

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                publishProgress();
            }

            return null;
        }
    }
}