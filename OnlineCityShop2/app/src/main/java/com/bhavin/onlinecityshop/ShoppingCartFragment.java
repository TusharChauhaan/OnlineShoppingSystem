package com.bhavin.onlinecityshop;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class ShoppingCartFragment extends Fragment {

    public Context context;
    private LinearLayout container;
    private LinearLayout parentContainer;
    private TaskListener listener;
    private TextView totalCount, totalPrice;

    public ShoppingCartFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ProfileFragment.Listner) {
            listener = (TaskListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TaskListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parentContainer = view.findViewById(R.id.parent_container);
        if(UserData.inMyCart.size() == 0){
            emptyCart();
        }else{
            totalCount = view.findViewById(R.id.total_items);
            totalPrice = view.findViewById(R.id.total_price);
            Button placeOrder = view.findViewById(R.id.place_order);
            container = view.findViewById(R.id.items_list_container);

            addItems();

            placeOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, PlaceOrderActivity.class);
                    startActivity(intent);

                }
            });
        }
    }

    public void emptyCart(){
        parentContainer.removeAllViews();

        View view = getLayoutInflater().inflate(R.layout.empty_cart, null);

        parentContainer.addView(view);
    }

    public void addItems(){
        container.removeAllViews();
        final ShoppingCartFragment fragment = this;
        totalCount.setText(UserData.itemCountInCart()+"");
        totalPrice.setText(UserData.getSumTotalOfCart()+"");
        for(int i=0;i<UserData.inMyCart.size();i++){
            final View item = getLayoutInflater().inflate(R.layout.cart_item_card, null);
            ImageButton add, minus;
            add = item.findViewById(R.id.inc_count);
            minus = item.findViewById(R.id.dec_count);
            final TextView countView = item.findViewById(R.id.item_count);
            item.findViewById(R.id.item_image).setClipToOutline(true);

            final Product product = UserData.inMyCart.get(i);
            countView.setText(product.getQuantity()+"");
            TextView name, description, price;
            ImageView photo;
            name = item.findViewById(R.id.product_name);
            photo = item.findViewById(R.id.item_image);
            description = item.findViewById(R.id.description);
            price = item.findViewById(R.id.price);

            name.setText(product.getName());
            photo.setImageBitmap(product.getPhoto());
            description.setText(product.getDescription());
            price.setText("RS "+product.getPrice());

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int count = Integer.parseInt(countView.getText().toString()) + 1;
                    Product p = new Product();
                    p.setId(product.getId());
                    p.setQuantity(count);
                    System.out.println(p.getQuantity());
                    listener.performUpdate(fragment, MainActivity.UPDATE_QUANTITY_IN_CART, p);
                }
            });

            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int count = Integer.parseInt(countView.getText().toString());
                    if(count==1){
                        listener.performUpdate(fragment, MainActivity.DELETE_CART_ITEM, product);
                    }else{
                        count--;
                        Product p = new Product();
                        p.setId(product.getId());
                        p.setQuantity(count);
                        listener.performUpdate(fragment, MainActivity.UPDATE_QUANTITY_IN_CART, p);
                    }
                }
            });

            item.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.performUpdate(fragment, MainActivity.DELETE_CART_ITEM, product);
                }
            });

            container.addView(item);
        }
    }
}