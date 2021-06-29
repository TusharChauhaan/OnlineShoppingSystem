package com.bhavin.onlinecityshop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Address {

    private int id;
    private String phoneNo;
    private String street;
    private String city;
    private String state;
    private String country;
    private int pinCode;

    public Address() {

    }

    public Address(int id, String phoneNo, String street, String city, String state, String country, int pinCode) {
        this.id = id;
        this.phoneNo = phoneNo;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pinCode = pinCode;
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return getStreet()+", "+getCity()+", "+getState()+", "+getCountry();
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getPinCode() {
        return pinCode;
    }

    public void setPinCode(int pinCode) {
        this.pinCode = pinCode;
    }

    public static Address fetchAddressHavingID(int id,Connection con) throws SQLException {

        String query="select id, pin_code, country, state, city, street, phone_number " +
                     "from address where id=?";
        Address a= new Address();

        try(
                PreparedStatement preparedStatement = con.prepareStatement(query)
        ){
            preparedStatement.setInt(1,id);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()){
                a.setId(rs.getInt(1));
                a.setPinCode(rs.getInt(2));
                a.setCountry(rs.getString(3));
                a.setState(rs.getString(4));
                a.setCity(rs.getString(5));
                a.setStreet(rs.getString(6));
                a.setPhoneNo(rs.getString(7));
            }

        }
        catch (SQLException throwables) {
            throw throwables;
        }

        return a;
    }

    public static boolean deleteAddressHavingID(int id,Connection con) throws SQLException {

        String query="delete from address where id=?";
        int i=0;

        try(
                PreparedStatement preparedStatement = con.prepareStatement(query)
        ){
            preparedStatement.setInt(1,id);
            i=preparedStatement.executeUpdate();
        }
        catch (SQLException throwables) {
            throw throwables;
        }

        if(i>0){
            return true;
        }
        else{
            return false;
        }
    }

    public static boolean updateAddressHavingID(Address a,Connection con) throws SQLException {

        String query="update address set  street=?, phone_number=?, city=?, state=?, country=?, pin_code=? where id=?";
        int i=0;
        try(
                PreparedStatement preparedStatement = con.prepareStatement(query)
        ){

            preparedStatement.setString(1,a.getStreet());
            preparedStatement.setString(2,a.getPhoneNo());
            preparedStatement.setString(3,a.getCity());
            preparedStatement.setString(4,a.getState());
            preparedStatement.setString(5,a.getCountry());
            preparedStatement.setInt(6,a.getPinCode());
            preparedStatement.setInt(7, a.getId());

            i=preparedStatement.executeUpdate();
        }
        catch (SQLException throwables) {
            throw throwables;
        }

        if(i>0){
            return true;
        }
        else{
            return false;
        }
    }

    public static boolean addNewAddress(Address f, String userId, Connection con) throws SQLException{

        String query="insert into address(pin_code, country, state, city, street, phone_number, user_id) " +
                "values(?, ?, ?, ?, ?, ?, ?)";
        int i=0;

        try(
                PreparedStatement preparedStatement = con.prepareStatement(query)
        ){
            preparedStatement.setString(6, f.getPhoneNo());
            preparedStatement.setString(5,f.getStreet());
            preparedStatement.setString(4,f.getCity());
            preparedStatement.setString(3,f.getState());
            preparedStatement.setString(2,f.getCountry());
            preparedStatement.setInt(1,f.getPinCode());
            preparedStatement.setString(7,userId);
            i= preparedStatement.executeUpdate();
        }
        catch (SQLException throwables) {
            throw throwables;
        }

        if(i>0){
            return true;
        }
        else{
            return false;
        }

    }

    public static ArrayList<Address> fetchAddressesOfUser(String userId, Connection con) throws SQLException {

        String query="select id, pin_code, country, state, city, street, phone_number " +
                "from address where user_id=?";
        ArrayList<Address> getdata = new ArrayList<>();

        try(
                PreparedStatement preparedStatement = con.prepareStatement(query)
        )
        {

            preparedStatement.setString(1,userId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                Address a= new Address();
                a.setId(rs.getInt(1));
                a.setPinCode(rs.getInt(2));
                a.setCountry(rs.getString(3));
                a.setState(rs.getString(4));
                a.setCity(rs.getString(5));
                a.setStreet(rs.getString(6));
                a.setPhoneNo(rs.getString(7));
                getdata.add(a);
                System.out.println(a.getId());
            }

        }
        catch (SQLException throwables) {
            throw throwables;
        }

        return getdata;
    }

}

