package com.bhavin.onlinecityshop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProductsInShop extends AppCompatActivity {

    public GridLayout gridLayout;
    public ArrayList<Product> products = new ArrayList<>();
    public Product productAdding;
    public AlertDialog dialog;
    public boolean addingToCart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_in_shop);

        gridLayout = findViewById(R.id.grid_container);

        ImageView banner = findViewById(R.id.shop_banner);
        RatingBar ratingBar =  findViewById(R.id.rating_bar);
        TextView rating = findViewById(R.id.rating_no);
        TextView name = findViewById(R.id.shop_name);
        TextView desc = findViewById(R.id.shop_desc);

        banner.setImageBitmap(Data.seller.getBanner());
        ratingBar.setRating(Data.seller.getRating());
        rating.setText(Data.seller.getRating()+"");
        name.setText(Data.seller.getShopName());
        desc.setText(Data.seller.getDescription());

        /**
         * Loading Dialog
         */

        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view)
                .setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        fetchData();
    }

    public void addProducts(){
        gridLayout.removeAllViews();

        for(int i=0;i<products.size();i++){
            View view = getLayoutInflater().inflate(R.layout.product_card, null);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f);

            params.width = 0;
            view.setLayoutParams(params);

            final Product product = products.get(i);

            CardView cardView = view.findViewById(R.id.product_card_button);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Data.product = product;
                    Intent i = new Intent(getApplicationContext(), DetailedViewOfProductActivity.class);
                    startActivity(i);
                }
            });

            ImageView photo = view.findViewById(R.id.item_photo);
            RatingBar ratingBar = view.findViewById(R.id.rating_bar);
            TextView ratingNo, productName, price;
            final Button addToCart = view.findViewById(R.id.add_to_cart);
            ratingNo = view.findViewById(R.id.rating_no);
            productName = view.findViewById(R.id.product_name);
            price = view.findViewById(R.id.price);

            addToCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addingToCart = true;
                    productAdding = product;
                    new Task().execute();
                }
            });
            
            if(UserData.getProduct(product.getId()) != null){
                /**
                 * This product is in cart
                 */
                addToCart.setEnabled(false);
            }

            ratingBar.setRating(product.getRating());
            ratingNo.setText(product.getRating()+" ("+product.getRatingCount()+")");
            productName.setText(product.getName());
            price.setText(product.getPrice()+"");
            photo.setImageBitmap(product.getPhoto());

            gridLayout.addView(view);
        }
    }

    public void fetchData(){
        new Task().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addProducts();
    }

    class Task extends AsyncTask<String, String, String> {

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
            Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... strings) {

            try{
                Class.forName("com.mysql.jdbc.Driver");

                try(
                        Connection connection = DriverManager.getConnection(UserData.URL,
                                UserData.USER, UserData.PASSWORD)
                ) {
                    if(addingToCart){
                        addingToCart = false;
                        if(UserData.checkForSameSeller(productAdding.getSellerId())){
                            User.addToShoppingCart(UserData.user.getContact(), productAdding.getId(), connection);
                            productAdding.setQuantity(1);
                            UserData.inMyCart.add(productAdding);
                        }else{
                            publishProgress("You can add items in cart from same seller only");
                        }
                    }else{
                        products = Seller.fetchProductsOfSeller(Data.seller.getSellerId(), connection);
                    }
                }

            } catch (ClassNotFoundException | SQLException e) {
                publishProgress("Something went wrong");
                e.printStackTrace();
            }
            return null;
        }
    }

}