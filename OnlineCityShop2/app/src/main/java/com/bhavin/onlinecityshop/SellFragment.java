package com.bhavin.onlinecityshop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

public class SellFragment extends Fragment {

    private static final int PICK_IMAGE = 100;
    public Context context;
    public ImageView banner;
    public Bitmap bitmap;
    public TaskListener listener;
    public ArrayList<Seller> sellers = new ArrayList<>();
    public LinearLayout container;
    public boolean isPhotoPicking = false;

    public SellFragment(Context context) {
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
        return inflater.inflate(R.layout.fragment_sell, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        container = view.findViewById(R.id.seller_container);

        if(UserData.user.getIsSeller()){
            sellerAccountMgmt(container);
        }else {
            noSellerAccount(container);
        }

    }

    public void sellerAccountMgmt(final LinearLayout container){
        container.removeAllViews();

        View view = getLayoutInflater().inflate(R.layout.seller_account_management,null);
        ImageView imageView = view.findViewById(R.id.shop_banner);
        imageView.setClipToOutline(true);

        TextView shopName, shopDesc, ratingNo;
        RatingBar ratingBar;

        ratingBar = view.findViewById(R.id.rating_bar);
        shopName = view.findViewById(R.id.shop_name);
        shopDesc = view.findViewById(R.id.shop_desc);
        ratingNo = view.findViewById(R.id.rating_no);

        if(UserData.seller != null){
            shopName.setText(UserData.seller.getShopName());
            shopDesc.setText(UserData.seller.getDescription());
            ratingNo.setText(UserData.seller.getRating()+"("+UserData.seller.getRatingCount()+")");
            ratingBar.setRating(UserData.seller.getRating());
            imageView.setImageBitmap(UserData.seller.getBanner());
        }

        view.findViewById(R.id.manage_details).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, SellerDetailsManagement.class);
                startActivity(i);
            }
        });

        view.findViewById(R.id.manage_product).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ProductManagementActivity.class);
                startActivity(i);
            }
        });

        view.findViewById(R.id.new_orders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, OrdersListActivity.class);
                i.putExtra("title","New Received Order");
                i.putExtra("isOld", false);
                startActivity(i);
            }
        });

        view.findViewById(R.id.old_orders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, OrdersListActivity.class);
                i.putExtra("title","Old Order");
                i.putExtra("isOld", true);
                startActivity(i);
            }
        });

        container.addView(view);
    }

    public void noSellerAccount(final LinearLayout container){
        container.removeAllViews();

        View view = getLayoutInflater().inflate(R.layout.no_seller_account, null);
        Button register = view.findViewById(R.id.register_seller);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerSeller(container);
            }
        });

        container.addView(view);
    }

    public void registerSeller(final LinearLayout container) {
        container.removeAllViews();

        View view = getLayoutInflater().inflate(R.layout.register_seller, null);
        Button register = view.findViewById(R.id.f_register);
        Button choose = view.findViewById(R.id.choose);
        Button cancel = view.findViewById(R.id.f_cancel);
        banner = view.findViewById(R.id.image_banner);
        banner.setClipToOutline(true);

        final EditText name, desc, serviceCity, address, contactNo, gstNo;
        name = view.findViewById(R.id.f_name);
        desc = view.findViewById(R.id.f_description);
        serviceCity = view.findViewById(R.id.f_service_city);
        address = view.findViewById(R.id.f_address);
        contactNo = view.findViewById(R.id.f_contact_no);
        gstNo = view.findViewById(R.id.f_gst_no);

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                isPhotoPicking = true;
                startActivityForResult(Intent.createChooser(intent,"Choose Picture"), PICK_IMAGE);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noSellerAccount(container);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmap != null){
                    Seller seller = new Seller();
                    seller.setSellerId(UserData.user.getContact());
                    seller.setShopName(name.getText().toString());
                    seller.setDescription(desc.getText().toString());
                    seller.setBanner(bitmap);
                    seller.setAddress(address.getText().toString());
                    seller.setServiceCity(serviceCity.getText().toString());
                    seller.setContactNumber(contactNo.getText().toString());
                    seller.setGstNo(gstNo.getText().toString());

                    RegisterSellerTask task = new RegisterSellerTask(container);
                    task.execute(seller);

                } else {
                    Toast.makeText(context, "Must select a photo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        container.addView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(UserData.user.getIsSeller()){
            sellerAccountMgmt(container);
        }else {
            if(isPhotoPicking){
                registerSeller(container);
                banner.setImageBitmap(bitmap);
            }else{
                noSellerAccount(container);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK){
            try{
               bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            banner.setImageBitmap(bitmap);
        }
    }

    class RegisterSellerTask extends AsyncTask<Seller, Void, Void>{

        LinearLayout container;

        public RegisterSellerTask(LinearLayout container) {
            this.container = container;
        }

        @Override
        protected Void doInBackground(Seller... sellers) {
            Seller seller = sellers[0];

            try{
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try (Connection con = DriverManager.getConnection(UserData.URL,
                    UserData.USER, UserData.PASSWORD)){

                Seller.addNewSeller(seller, con);
                UserData.user.setIsSeller(true);
                UserData.seller = seller;

            } catch (SQLException | IOException throwables) {
                throwables.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.showLoadingDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listener.dismissLoadingDialog();
            sellerAccountMgmt(container);
        }
    }

}