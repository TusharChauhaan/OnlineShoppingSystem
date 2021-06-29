package com.bhavin.onlinecityshop;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mysql.jdbc.log.Slf4JLogger;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class NearMeFragment extends Fragment {

    public Context context;
    public LinearLayout linearLayout;
    public TaskListener listener;
    public SwipeRefreshLayout refreshLayout;
    public ScrollView scrollView;
    public TextView address;

    public NearMeFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ProfileFragment.Listner) {
            listener = (TaskListener) context;
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
        return inflater.inflate(R.layout.fragment_near_me, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        linearLayout = view.findViewById(R.id.layout);
        final EditText search = view.findViewById(R.id.search_text);
        refreshLayout = view.findViewById(R.id.refresh);
        scrollView = view.findViewById(R.id.scrollView);
        address = view.findViewById(R.id.address);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listener.refresh(refreshLayout);
            }
        });

        int purple = getResources().getColor(R.color.purple);
        int pink = getResources().getColor(R.color.pink);
        int green = getResources().getColor(R.color.green);

        refreshLayout.setColorSchemeColors(purple, pink, green);

        view.findViewById(R.id.address_butoon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.showAddressChooserDialog();
            }
        });

        final NearMeFragment fragment = this;

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    listener.performUpdate(fragment, MainActivity.SEARCH_SHOP, search.getText().toString());

                    search.clearFocus();
                    InputMethodManager manager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(search.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        if(UserData.currAddress != null){
            setAddressView();
        }
        if(!UserData.shops.isEmpty()){
            addSellers();
        }

    }

    public void setAddressView(){
        UserData.currAddress = UserData.getAddress(UserData.user.getAddressId());
        Address a = UserData.getAddress(UserData.user.getAddressId());
        if(a != null){
            address.setText(a.getAddress());
        }
    }

    public void addSellers(){
        linearLayout.removeAllViews();
        ArrayList<Seller> sellers = UserData.shops;

        if(sellers.isEmpty()){
            TextView textView = new TextView(getContext());
            String s = "No Shop Matching to your keyword";
            textView.setText(s);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            linearLayout.addView(textView);
        }
        for(int i=0;i<sellers.size();i++){
            View view = getLayoutInflater().inflate(R.layout.item_card, null);
            TextView name = view.findViewById(R.id.title);
            TextView desc = view.findViewById(R.id.description);
            TextView rating = view.findViewById(R.id.rating_no);
            RatingBar ratingBar = view.findViewById(R.id.rating_bar);
            ImageView photo = view.findViewById(R.id.img);

            final Seller seller = sellers.get(i);

            name.setText(seller.getShopName());
            desc.setText(seller.getDescription());
            rating.setText(seller.getRating()+" ("+seller.getRatingCount()+")");
            ratingBar.setRating(seller.getRating());
            photo.setImageBitmap(seller.getBanner());
            photo.setClipToOutline(true);

            view.findViewById(R.id.shop_card).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProductsInShop.class);
                    Data.seller = seller;
                    startActivity(intent);
                }
            });

            linearLayout.addView(view);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        addSellers();
    }
}

interface TaskListener{
    void performUpdate(Fragment fragment, int task, Object data);
    void showLoadingDialog();
    void dismissLoadingDialog();
    void showErrorDialog(String message);
    void showAddressChooserDialog();
    void refresh(SwipeRefreshLayout layout);
}