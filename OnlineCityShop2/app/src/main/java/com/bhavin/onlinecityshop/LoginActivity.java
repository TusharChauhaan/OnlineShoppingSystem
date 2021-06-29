package com.bhavin.onlinecityshop;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener,
        SignUpFragment.OnFragmentInteractionListener
{

    AlertDialog dialog;
    boolean signUp;
    SharedPreferences preferences;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = getSharedPreferences("login", MODE_PRIVATE);

        if(preferences.getBoolean("is_logged_in", false)){
            userId = preferences.getString("user_id","");
            sendToMainActivity();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new LoginFragment(this));
        transaction.commit();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.loading_dialog, null);
        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void changeToSignUpFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in,R.anim.slide_out);
        transaction.replace(R.id.container, new SignUpFragment(this));
        transaction.commit();
    }

    public void changeToLoginFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in,R.anim.slide_out);
        transaction.replace(R.id.container, new LoginFragment(this));
        transaction.commit();
    }

    public void login(String contact, String password){
        User user = new User();
        user.setContact(contact);
        user.setPassword(password);
        userId = contact;

        signUp = false;
        Task task = new Task();
        task.execute(user);
    }

    public void signUp(User user){
        signUp = true;
        userId = user.getContact();
        Task task = new Task();
        task.execute(user);
    }

    public void showDialog(){
        dialog.show();
    }

    public void dismissDialog(){
        dialog.dismiss();
    }

    public void sendToMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("user_id",userId);
        startActivity(i);
        finish();
    }

    class Task extends AsyncTask<User, Void, Void>{

        public static final int LOGIN_SUCCESS = 0;
        public static final int LOGIN_FAILED = 1;
        public static final int SIGN_UP_SUCCESS = 2;
        public static final int SIGN_UP_FAILED = 3;
        int result = 0;
        User user;
        private String errMessage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dismissDialog();

            switch (result){
                case LOGIN_SUCCESS:
                    sendToMainActivity();
                    break;
                case SIGN_UP_SUCCESS:
                    Toast.makeText(getApplicationContext(), "Registration Success",
                            Toast.LENGTH_LONG).show();
                    sendToMainActivity();
                    break;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(User... users) {
            user = users[0];
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try(Connection con = DriverManager.getConnection(
                    UserData.URL, UserData.USER, UserData.PASSWORD)){
                if(signUp){
                    User.addNewUser(user, con);
                    result = SIGN_UP_SUCCESS;
                }else {
                    User.checkUser(user, con);
                    if(User.checkUser(user, con)){
                        //Login Successful
                        result = LOGIN_SUCCESS;
                    }else {
                        result = LOGIN_FAILED;
                    }
                }
            } catch (SQLException throwables) {
                if(signUp){
                    result = SIGN_UP_FAILED;
                }else{
                    result = LOGIN_FAILED;
                }
                errMessage = throwables.getMessage();
                publishProgress();
                throwables.printStackTrace();
            }

            return null;
        }
    }
}
