package com.bhavin.onlinecityshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.sql.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ProfileFragment.Listner,
        TaskListener{

    public static final int ADD_ADDRESS = 0;
    public static final int UPDATE_ADDRESS = 1;
    public static final int UPDATE_CURR_ADDRESS = 2;
    public static final int UPDATE_CART = 3;
    public static final int UPDATE_QUANTITY_IN_CART = 4;
    public static final int UPDATE_USER = 5;
    public static final int SEARCH_SHOP = 6;
    public static final int DELETE_CART_ITEM = 7;

    EditText street, city, state, country, pinCode, phoneNo;

    private BottomNavigationView navigationView;
    int pos = 0, currAdrressId = UserData.user.getAddressId();
    private AlertDialog dialog;
    private AlertDialog.Builder builder1;
    private BottomSheetDialog addressChooser, addAddress;
    private SharedPreferences preferences;
    private String userId;
    private NearMeFragment nearMeFragment;
    private boolean isRefreshing = false;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = getIntent().getStringExtra("user_id");

        preferences = getSharedPreferences("login", MODE_PRIVATE);
        preferences.edit().putBoolean("is_logged_in", true)
                .putString("user_id", userId).apply();

        nearMeFragment = new NearMeFragment(this);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, nearMeFragment);
        transaction.commit();

        navigationView = findViewById(R.id.bottom_navigation);

        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view)
                .setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        buildAddressChooser();
        buildAddNewAddressDialog();
        /**
         * Create a error dialog
         */
        builder1 = new AlertDialog.Builder(this);
        builder1.setIcon(R.drawable.ic_baseline_error_24)
                .setTitle("Error Occurred")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        /**
         * Start fetching data
         */
        DataFetchingTask task = new DataFetchingTask();
        task.execute();
        Log.d("User Id", userId);
        ////////////////////////////////////////////////////////////////////////////////////////
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.shop:
                        if(pos != 0){
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);
                            transaction.replace(R.id.main_container, nearMeFragment);
                            transaction.commit();
                        }
                        pos = 0;
                        break;
                    case R.id.shop1:
                        if(pos != 1){
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);
                            transaction.replace(R.id.main_container, new ShoppingCartFragment(getApplicationContext()));
                            transaction.commit();
                        }
                        pos = 1;
                        break;
                    case R.id.shop2:
                        if(pos != 2){
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);
                            transaction.replace(R.id.main_container, new SellFragment(getApplicationContext()));
                            transaction.commit();
                        }
                        pos = 2;
                        break;
                    case R.id.shop3:
                        if(pos != 3){
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);
                            transaction.replace(R.id.main_container, new ProfileFragment(getApplicationContext()));
                            transaction.commit();
                        }
                        pos = 3;
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public void showBottomSheet(final ProfileFragment fragment) {
        View dialogView = getLayoutInflater().inflate(R.layout.edit_bottom_dialog, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        final EditText name, email;
        name = dialogView.findViewById(R.id.name);
        email = dialogView.findViewById(R.id.email);
        name.setText(UserData.user.getName());
        email.setText(UserData.user.getEmailId());

        dialogView.findViewById(R.id.update).
                setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                User user = new User();
                user.setName(name.getText().toString());
                user.setEmailId(email.getText().toString());
                new UpdateTask(fragment, UPDATE_USER, user).execute();
            }
        });

        dialog.setContentView(dialogView);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.show();
    }

    @Override
    public void logOut() {
        preferences.edit().putBoolean("is_logged_in", false)
                .putString("user_id", "").apply();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void performUpdate(Fragment fragment, int task, Object data) {
        new UpdateTask(fragment, task, data).execute();
    }

    @Override
    public void showLoadingDialog() {
        dialog.show();
    }

    @Override
    public void dismissLoadingDialog() {
        dialog.dismiss();
    }

    @Override
    public void showErrorDialog(String message) {
        builder1.setMessage(message);
        builder1.create().show();
    }

    @Override
    public void showAddressChooserDialog(){
        addressChooser.show();
    }

    @Override
    public void refresh(SwipeRefreshLayout layout) {
        isRefreshing = true;
        refreshLayout = layout;
        DataFetchingTask task = new DataFetchingTask();
        task.execute();
    }

    public void buildAddressChooser(){
        // Address Chooser Dialog
        int index = UserData.user.getAddressId();
        currAdrressId = index;
        View addressChooserView = getLayoutInflater().inflate(R.layout.address_chooser_dialog, null);
        addressChooserView.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //action update database
                Address a = new Address();
                a.setId(currAdrressId);
                new UpdateTask(null, UPDATE_CURR_ADDRESS, a).execute();
                addressChooser.dismiss();
            }
        });
        addressChooserView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressChooser.dismiss();
            }
        });

        addressChooserView.findViewById(R.id.add_new).
                setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressChooser.dismiss();
                addAddress.show();
            }
        });

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
            if(UserData.addresses.get(i).getId() == currAdrressId){
                button.setBackground(getDrawable(R.drawable.purple_boarder));
            }

            Address a = UserData.addresses.get(i);
            String s = a.getStreet()+"\n"+a.getCity()+", "+
                    a.getState()+"\n"+a.getCountry();

            address.setText(s);
            phone.setText("Mo: "+a.getPhoneNo());

            final int curr = i;

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currAdrressId = UserData.addresses.get(curr).getId();
                    System.out.println(currAdrressId);
                    addAddressCard(container, addressId);
                }
            });

            container.addView(view);
        }
    }

    public void buildAddNewAddressDialog(){
        View view = getLayoutInflater().inflate(R.layout.add_new_address, null);

        street = view.findViewById(R.id.street);
        city = view.findViewById(R.id.city);
        state = view.findViewById(R.id.state);
        country = view.findViewById(R.id.country);
        pinCode = view.findViewById(R.id.pin_code);
        phoneNo = view.findViewById(R.id.phone_no);

        final Address address = new Address();

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Start updating data
                 */
                address.setStreet(street.getText().toString());
                address.setCity(city.getText().toString());
                address.setState(state.getText().toString());
                address.setCountry(country.getText().toString());
                address.setPinCode(Integer.parseInt(pinCode.getText().toString()));
                address.setPhoneNo(phoneNo.getText().toString());
                new UpdateTask(null, ADD_ADDRESS, address).execute();
                addAddress.dismiss();
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAddress.dismiss();
            }
        });

        addAddress = new BottomSheetDialog(this);
        addAddress.setContentView(view);
        addAddress.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    /**
     * Async Task for fetching data
     */
    class DataFetchingTask extends AsyncTask<String, String, Void> {

        ArrayList<Seller> sellers = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(isRefreshing){
                refreshLayout.setRefreshing(true);
            }else {
                showLoadingDialog();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(isRefreshing){
                refreshLayout.setRefreshing(false);
            }else {
                dismissLoadingDialog();
            }
            nearMeFragment.addSellers();
            nearMeFragment.setAddressView();
            buildAddressChooser();
            isRefreshing = false;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(values[0].contains("Add address")){
                addAddress.show();
            }else {
                showErrorDialog(values[0]);
            }
        }

        @Override
        protected Void doInBackground(String... strings) {

            try{
                Class.forName("com.mysql.jdbc.Driver");

                try(
                        Connection connection = DriverManager.getConnection(UserData.URL,
                                UserData.USER, UserData.PASSWORD)
                ) {

                    if(!isRefreshing){
                        UserData.user = User.fetchUser(userId, connection);
                        UserData.addresses = Address.fetchAddressesOfUser(UserData.user.getContact(), connection);
                        if(UserData.addresses.isEmpty()){
                            publishProgress("Add address");
                        }
                        else{
                            UserData.orders = Order.fetchOrdersForUserID(UserData.user.getContact(), connection);
                            UserData.inMyCart = User.fetchItemsInCart(UserData.user.getContact(), connection);

                            if(UserData.user.getIsSeller()){
                                UserData.seller = Seller.fetchSellerWithUserId(UserData.user.getContact(), connection);
                            }
                        }
                    }

                    Address curr = UserData.getAddress(UserData.user.getAddressId());
                    if(curr != null){
                        sellers = Seller.fetchSellerInCity(curr.getCity(), connection);
                    }
                    UserData.shops = sellers;

                } catch (SQLException throwables) {
                    publishProgress(throwables.getMessage());
                    throwables.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                publishProgress("Error occurred while fetching data");
            }

            return null;
        }
    }

    class UpdateTask extends AsyncTask<String, String, String> {

        Fragment fragment;
        int task;
        Object object;

        public UpdateTask(Fragment fragment, int task, Object data) {
            this.fragment = fragment;
            this.task = task;
            this.object = data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingDialog();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dismissLoadingDialog();
            if(fragment != null){
                if(fragment instanceof ProfileFragment){
                    ((ProfileFragment)fragment).refreshData();
                }else if(fragment instanceof ShoppingCartFragment){
                    ((ShoppingCartFragment)fragment).addItems();
                }
            }
            nearMeFragment.setAddressView();
            if(pos == 0){
                nearMeFragment.addSellers();
            }
            buildAddressChooser();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            showErrorDialog(values[0]);
        }

        @Override
        protected String doInBackground(String... strings) {

            try{
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try(
                    Connection connection = DriverManager.getConnection(UserData.URL,
                            UserData.USER, UserData.PASSWORD)
            ) {

                switch (task){
                    case UPDATE_USER:
                        User user = (User)object;
                        user.setContact(UserData.user.getContact());

                        User.updateUser(user, connection);
                        UserData.user.setName(user.getName());
                        UserData.user.setEmailId(user.getEmailId());
                        break;
                    case ADD_ADDRESS:
                        Address address = (Address)object;
                        Address.addNewAddress(address, UserData.user.getContact(), connection);
                        UserData.addresses = Address.fetchAddressesOfUser(UserData.user.getContact(), connection);
                        User.changeCurrentAddress(UserData.user.getContact(), UserData.getLastAddress().getId(), connection);
                        UserData.user.setAddressId(UserData.getLastAddress().getId());
                        Address curr = UserData.getAddress(UserData.user.getAddressId());
                        if(curr != null){
                            UserData.shops = Seller.fetchSellerInCity(curr.getCity(), connection);
                        }
                        break;
                    case SEARCH_SHOP:
                        String search = (String)object;
                        UserData.shops = Seller.searchSeller(search, UserData.getAddress(UserData.user.getAddressId()).getCity(), connection);
                        break;
                    case UPDATE_ADDRESS:
                        Address address1 = (Address)object;
                        Address.updateAddressHavingID(address1, connection);
                        UserData.addresses.remove(UserData.getAddress(address1.getId()));
                        UserData.addresses.add(address1);
                        break;
                    case UPDATE_CURR_ADDRESS:
                        Address address2 = (Address)object;
                        User.changeCurrentAddress(UserData.user.getContact(), address2.getId(), connection);
                        UserData.user.setAddressId(address2.getId());
                        Address curr1 = UserData.getAddress(UserData.user.getAddressId());
                        if(curr1 != null){
                            UserData.shops = Seller.fetchSellerInCity(curr1.getCity(), connection);
                        }
                        break;
                    case UPDATE_CART:
                        Product product = (Product)object;
                        if(UserData.checkForSameSeller(product.getSellerId())){
                            User.addToShoppingCart(UserData.user.getContact(), product.getId(), connection);
                            UserData.inMyCart.add(product);
                        }else{
                            publishProgress("You can add items in cart from same seller only");
                        }
                        break;
                    case UPDATE_QUANTITY_IN_CART:
                        Product product1 = (Product)object;
                        User.updateItemQuantityInCart(UserData.user.getContact(), product1.getId(), product1.getQuantity(), connection);
                        UserData.getProduct(product1.getId()).setQuantity(product1.getQuantity());
                        break;
                    case DELETE_CART_ITEM:
                        Product product2 = (Product)object;
                        User.removeItemFromCart(UserData.user.getContact(),product2.getId(), connection);
                        UserData.inMyCart.remove(product2);
                        break;
                }

            } catch (SQLException throwables) {
                publishProgress(throwables.getMessage());
                throwables.printStackTrace();
            }
            return null;
        }

    }

}