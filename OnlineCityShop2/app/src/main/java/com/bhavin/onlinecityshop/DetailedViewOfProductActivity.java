package com.bhavin.onlinecityshop;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class DetailedViewOfProductActivity extends AppCompatActivity {

    private static final int UPDATE_CART = 0;
    private static final int ORDER = 3;
    private static final int UPDATE_QUANTITY_IN_CART = 1;
    private static final int DELETE_CART_ITEM = 2;
    private ImageView photo;
    private Bitmap bitmap;
    private TextView rating, price, name, description, quantity;
    private RatingBar ratingBar;
    private ImageButton plus, minus;
    private Button buyNow, addToCart;
    private AlertDialog dialog, orderNowDialog;
    private Product product, product1;
    private Order order = new Order();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view_of_product);

        photo = findViewById(R.id.product_image);
        ratingBar = findViewById(R.id.rating_bar);
        rating = findViewById(R.id.rating_no);
        price = findViewById(R.id.price);
        name = findViewById(R.id.product_name);
        description = findViewById(R.id.product_desc);
        quantity = findViewById(R.id.item_count);

        buyNow = findViewById(R.id.order_now);
        addToCart = findViewById(R.id.add_to_cart);

        plus = findViewById(R.id.inc_count);
        minus = findViewById(R.id.dec_count);

        product1 = Data.product;
        quantity.setText(0+"");

        product = new Product(product1);
        product.setQuantity(1);

        Product p = UserData.getProduct(product.getId());
        if(p != null){
            product1.setQuantity(p.getQuantity());
            quantity.setText(product1.getQuantity()+"");
            addToCart.setEnabled(false);
        }

        order.setDeliveryAddressId(UserData.user.getAddressId());

        /**
         * Create a loading Dialog
         */
        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view)
                .setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        /**
         * Set product data to view items
         */

        photo.setImageBitmap(product.getPhoto());
        ratingBar.setRating(product.getRating());
        rating.setText(product.getRating()+" ("+product.getRatingCount()+")");
        price.setText(product.getPrice()+"");
        name.setText(product.getName());
        description.setText(product.getDescription());

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(quantity.getText().toString()) + 1;
                product1.setQuantity(count);
                if(count == 1){
                    new Task(UPDATE_CART).execute();
                }else{
                    new Task(UPDATE_QUANTITY_IN_CART).execute();
                }
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(quantity.getText().toString());
                if(count==1){
                    product1.setQuantity(count-1);
                    new Task(DELETE_CART_ITEM).execute();
                }else{
                    count--;
                    product1.setQuantity(count);
                    new Task(UPDATE_QUANTITY_IN_CART).execute();
                }
            }
        });

        findViewById(R.id.order_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOrderNowDialog();
                //create and also shows the dialog
            }
        });

        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product1.setQuantity(1);
                new Task(UPDATE_CART).execute();
            }
        });
    }

    public void createOrderNowDialog(){
        View view = getLayoutInflater().inflate(R.layout.order_now_dialog, null);
        ImageView photo = view.findViewById(R.id.item_photo);
        TextView name = view.findViewById(R.id.item_name);
        TextView itemTotalPrice = view.findViewById(R.id.total_item_price);
        final TextView itemTotal = view.findViewById(R.id.item_total);
        final TextView toPay = view.findViewById(R.id.to_pay);
        final TextView quantity = view.findViewById(R.id.item_count);

        photo.setImageBitmap(product.getPhoto());
        name.setText(product.getName());
        itemTotalPrice.setText(""+product.getPrice());
        itemTotal.setText(""+ (product.getPrice()*product.getQuantity()));
        toPay.setText(""+(product.getPrice()*product.getQuantity() + 20));
        quantity.setText(""+product.getQuantity());

        view.findViewById(R.id.inc_count).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = Integer.parseInt(quantity.getText().toString()) + 1;
                quantity.setText(n+"");
                product.setQuantity(n);
                itemTotal.setText(""+ (product.getPrice()*product.getQuantity()));
                toPay.setText(""+(product.getPrice()*product.getQuantity() + 20));
            }
        });

        view.findViewById(R.id.dec_count).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = Integer.parseInt(quantity.getText().toString()) - 1;
                if(n == 0){
                    orderNowDialog.dismiss();
                    return;
                }
                quantity.setText(n+"");
                product.setQuantity(n);
                itemTotal.setText(""+ (product.getPrice()*product.getQuantity()));
                toPay.setText(""+(product.getPrice()*product.getQuantity() + 20));
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderNowDialog.dismiss();
            }
        });

        view.findViewById(R.id.cod).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order.setPaymentStatus(Order.NOT_PAID);
                orderNowDialog.dismiss();
                bookOrder();
            }
        });

        view.findViewById(R.id.pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order.setPaymentStatus(Order.PAID);
                orderNowDialog.dismiss();
                bookOrder();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        orderNowDialog = builder.create();
        orderNowDialog.show();
    }

    private void bookOrder() {
        new Task(ORDER).execute();
    }

    private void changeData(){
        quantity.setText(product1.getQuantity()+"");
        if(product1.getQuantity() > 0){
            addToCart.setEnabled(false);
        }else{
            addToCart.setEnabled(true);
        }
    }

    class Task extends AsyncTask<Void, String, Void>{

        private int choice = 0;
        private boolean success = false;

        public Task(int choice) {
            this.choice = choice;
        }

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
                changeData();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(choice == UPDATE_CART){
                Toast.makeText(getApplicationContext(),values[0], Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try{
                Class.forName("com.mysql.jdbc.Driver");

                try(
                        Connection connection = DriverManager.getConnection(UserData.URL, UserData.USER, UserData.PASSWORD);
                        ){

                    switch (choice){
                        case ORDER:
                            ArrayList<Product> products = new ArrayList<>();
                            products.add(product);
                            Order.addNewOrder(UserData.user.getContact(),order, products, connection);
                            UserData.orders = Order.fetchOrdersForUserID(UserData.user.getContact(), connection);
                            success = true;
                            break;
                        case UPDATE_CART:
                            if(UserData.checkForSameSeller(product1.getSellerId())){
                                User.addToShoppingCart(UserData.user.getContact(), product1.getId(), connection);
                                UserData.inMyCart.add(product1);
                                success = true;
                            }else{
                                publishProgress("You can add items in cart from same seller only");
                            }
                            break;
                        case UPDATE_QUANTITY_IN_CART:
                            User.updateItemQuantityInCart(UserData.user.getContact(), product1.getId(), product1.getQuantity(), connection);
                            UserData.getProduct(product1.getId()).setQuantity(product1.getQuantity());
                            success = true;
                            break;
                        case DELETE_CART_ITEM:
                            User.removeItemFromCart(UserData.user.getContact(),product1.getId(), connection);
                            UserData.inMyCart.remove(UserData.getProduct(product1.getId()));
                            success = true;
                            break;
                    }

                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                publishProgress();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                publishProgress();
            }

            return null;
        }
    }

}