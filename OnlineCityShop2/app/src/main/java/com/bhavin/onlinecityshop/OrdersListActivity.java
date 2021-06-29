package com.bhavin.onlinecityshop;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrdersListActivity extends AppCompatActivity {

    public LinearLayout layout;
    public boolean isOld = false;
    public ArrayList<Order> orders = new ArrayList<>();
    public AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);

        TextView title = findViewById(R.id.title);
        String name = getIntent().getStringExtra("title");
        isOld = getIntent().getBooleanExtra("isOld", false);
        title.setText(name);

        ImageButton back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        layout = findViewById(R.id.orders_container);

        /**
         * Loading dialog
         */
        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view)
                .setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        new Task().execute();
    }

    public void addOrders(){
        layout.removeAllViews();

        if(orders.isEmpty()){
            String s;
            if(isOld){
                s = "You do not have any old order";
            }else{
                s = "You do not have received any order";
            }
            TextView textView = new TextView(this);
            textView.setText(s);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(textView);
        }

        for(int i=0;i<orders.size();i++){
            View view = getLayoutInflater().inflate(R.layout.orders_card, null);

            final Order order = orders.get(i);

            view.findViewById(R.id.order_card).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), OrderDetails.class);
                    i.putExtra("choice",OrderDetails.SELLER_VIEW);
                    i.putExtra("orderId", order.getId());
                    startActivity(i);
                }
            });

            TextView date, seller, amount, status;
            date = view.findViewById(R.id.orders_date);
            seller = view.findViewById(R.id.orders_seller);
            amount = view.findViewById(R.id.order_total);
            status = view.findViewById(R.id.order_status);

            date.setText(order.getDateAndtime());
            seller.setText(order.getConsumerName());
            amount.setText(order.getTotalAmount()+20+"");
            status.setText(order.getDecodedDeliveryStatus());

            if(order.getDeliveryStatus() == Order.CANCELLED){

                status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cancel,0,0,0);
                status.setTextColor(getResources().getColor(R.color.red));

            }else if(order.getDeliveryStatus() != Order.DELIVERED){
                status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_atom_circular_symbol_of_circles,0,0,0);
                status.setTextColor(getResources().getColor(R.color.yellow));
            }

            layout.addView(view);
        }
    }

    class Task extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            addOrders();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(),"Something Went Wrong", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                Class.forName("com.mysql.jdbc.Driver");

                try(
                        Connection connection = DriverManager.getConnection(UserData.URL,
                                UserData.USER, UserData.PASSWORD)
                ) {
                    if(isOld){
                        orders = Order.fetchOldOrderOfSeller(UserData.user.getContact(), connection);
                    }else{
                        orders = Order.fetchNewOrderReceivedToSeller(UserData.user.getContact(), connection);
                    }
                }

            } catch (ClassNotFoundException | SQLException e) {
                publishProgress();
                e.printStackTrace();
            }
            return null;
        }
    }
}