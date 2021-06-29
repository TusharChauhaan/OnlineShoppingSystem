package com.bhavin.onlinecityshop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private Context context;
    private Listner listner;
    private TextView name, email;
    private LinearLayout layout;

    public ProfileFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listner) {
            listner = (Listner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProfileFragment.Listner");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layout = view.findViewById(R.id.past_orders_container);

        Button edit = view.findViewById(R.id.edit_button);
        Button logOut = view.findViewById(R.id.logout_btn);
        name = view.findViewById(R.id.user_name);
        TextView contact = view.findViewById(R.id.user_contact);
        email = view.findViewById(R.id.user_email);

        name.setText(UserData.user.getName());
        contact.setText(UserData.user.getContact());
        email.setText(UserData.user.getEmailId());

        final ProfileFragment fragment = this;

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.logOut();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.showBottomSheet(fragment);
            }
        });

        addOrders();
    }

    public void refreshData(){
        name.setText(UserData.user.getName());
        email.setText(UserData.user.getEmailId());
    }

    public void addOrders(){
        layout.removeAllViews();
        final ArrayList<Order> orders = UserData.orders;

        for(int i=0;i<orders.size();i++){
            View view = getLayoutInflater().inflate(R.layout.orders_card, null);

            final Order order = orders.get(i);

            view.findViewById(R.id.order_card).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, OrderDetails.class);
                    i.putExtra("choice",OrderDetails.CONSUMER_VIEW);
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
            seller.setText(order.getSellerName());
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

    @Override
    public void onResume() {
        super.onResume();
        addOrders();
    }

    public interface Listner{
        void showBottomSheet(ProfileFragment fragment);
        void logOut();
    }
}