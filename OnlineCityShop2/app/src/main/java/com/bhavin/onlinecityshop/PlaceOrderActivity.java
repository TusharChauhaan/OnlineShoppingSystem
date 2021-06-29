package com.bhavin.onlinecityshop;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.bhavin.onlinecityshop.MainActivity.UPDATE_CURR_ADDRESS;

public class PlaceOrderActivity extends AppCompatActivity {

    ImageView shopBanner;
    Button pay, back;
    LinearLayout orderContainer;
    AlertDialog dialog, paymentDialog;
    BottomSheetDialog addressChooser;
    Order order=new Order();
    int choosedAddress;
    TextView deliveryAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        /**
         * Initially sets delivery address to current address
         */
        order.setDeliveryAddressId(UserData.user.getAddressId());
        /**
         * building address chooser dialog
         */
        buildAddressChooser();
        /**
         * Loading Dialog
         */
        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view)
                .setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        /**
         * Payment Dialog
         */
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("Choose Payment Method")
                .setPositiveButton("PAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        order.setPaymentStatus(Order.PAID);
                        bookOrder();
                    }
                })
                .setNegativeButton("COD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        order.setPaymentStatus(Order.NOT_PAID);
                        bookOrder();
                    }
                });
        paymentDialog = builder1.create();

        Seller seller = UserData.getSeller(UserData.inMyCart.get(0).getSellerId());

        shopBanner = findViewById(R.id.shop_image);
        shopBanner.setClipToOutline(true);
        shopBanner.setImageBitmap(seller.getBanner());
        TextView shopName = findViewById(R.id.shop_name_order);
        shopName.setText(seller.getShopName());
        TextView shopAddress = findViewById(R.id.shop_address);
        shopAddress.setText(seller.getAddress());

        deliveryAddress = findViewById(R.id.delivery_address);
        deliveryAddress.setText(UserData.getAddress(order.getDeliveryAddressId()).getAddress());

        deliveryAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressChooser.show();
            }
        });

        TextView itemTotal = findViewById(R.id.item_total);
        itemTotal.setText(UserData.getSumTotalOfCart()+"");
        TextView toPay = findViewById(R.id.to_pay);
        toPay.setText((UserData.getSumTotalOfCart()+20)+"");

        pay = findViewById(R.id.pay);
        back = findViewById(R.id.back);
        orderContainer = findViewById(R.id.order_items_container);

        addItems();

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentDialog.show();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    public void addItems(){
        ArrayList<Product> products = UserData.inMyCart;
        for(int i=0;i<products.size();i++){
            View view = getLayoutInflater().inflate(R.layout.bill_item, null);
            ImageView photo = view.findViewById(R.id.item_photo);
            photo.setClipToOutline(true);
            TextView itemName, priceCount, price;
            itemName = view.findViewById(R.id.item_name);
            priceCount = view.findViewById(R.id.item_price_count);
            price = view.findViewById(R.id.total_item_price);
            /**
             * Set data
             */
            photo.setImageBitmap(products.get(i).getPhoto());
            itemName.setText(products.get(i).getName());
            priceCount.setText(products.get(i).getQuantity()+" X Rs "+products.get(i).getPrice());
            price.setText(products.get(i).getQuantity()*products.get(i).getPrice()+"");
            /**
             * add to container
             */
            orderContainer.addView(view);
        }
    }

    public void bookOrder(){
        new Task().execute();
    }

    public void buildAddressChooser(){
        // Address Chooser Dialog
        int index = UserData.user.getAddressId();
        choosedAddress = index;
        View addressChooserView = getLayoutInflater().inflate(R.layout.address_chooser_dialog, null);
        addressChooserView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order.setDeliveryAddressId(choosedAddress);
                addressChooser.dismiss();
            }
        });
        addressChooserView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressChooser.dismiss();
            }
        });

        addressChooserView.findViewById(R.id.add_new).setVisibility(View.GONE);

        LinearLayout layout = addressChooserView.findViewById(R.id.container);

        addAddressCard(layout, index);

        addressChooser = new BottomSheetDialog(this);
        addressChooser.setContentView(addressChooserView);

        addressChooser.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

    }

    public void addAddressCard(final LinearLayout container, final int addressId){
        container.removeAllViews();
        for(int i=0;i<UserData.addresses.size();i++){
            View view = getLayoutInflater().inflate(R.layout.address_card, null);
            TextView address = view.findViewById(R.id.address);
            TextView phone = view.findViewById(R.id.phone_no);

            LinearLayout button = view.findViewById(R.id.address_button);
            final Address a = UserData.addresses.get(i);

            if(a.getId() == choosedAddress){
                button.setBackground(getDrawable(R.drawable.purple_boarder));
            }

            String s = a.getStreet()+"\n"+a.getCity()+", "+
                    a.getState()+"\n"+a.getCountry();

            address.setText(s);
            phone.setText("Mo: "+a.getPhoneNo());

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    choosedAddress = a.getId();
                    deliveryAddress.setText(a.getAddress());
                    addAddressCard(container, addressId);
                }
            });

            container.addView(view);
        }
    }

    class Task extends AsyncTask<String, String, String>{

        boolean success=false;

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
                Toast.makeText(getApplicationContext(), "Order Booked Successfully", Toast.LENGTH_LONG).show();
                onBackPressed();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{

                Class.forName("com.mysql.jdbc.Driver");

                try(
                        Connection con = DriverManager.getConnection(UserData.URL, UserData.USER, UserData.PASSWORD)
                ){

                    Order.addNewOrder(UserData.user.getContact(),order, UserData.inMyCart, con);
                    UserData.orders = Order.fetchOrdersForUserID(UserData.user.getContact(), con);
                    success = true;

                } catch (SQLException throwables) {
                    publishProgress("Something went wrong");
                }

            }catch (ClassNotFoundException e){
                publishProgress("Something went wrong");
            }
            return null;
        }
    }

}