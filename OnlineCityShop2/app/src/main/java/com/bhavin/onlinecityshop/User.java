package com.bhavin.onlinecityshop;

import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {

    public static final int NO_ADDRESS = -1;

    private String name;
    private String contactNo;
    private String emailId;
    private String password;
    private boolean isSeller;
    private int addressId;

    public User() {
        name = "NA";
        contactNo = "NA";
        emailId = "NA";
        password = "";
        isSeller = false;
        addressId = -1;
    }

    public User(String contactNo, String password, String emailId, String name, boolean isSeller, int addressId) {
        this.contactNo = contactNo;
        this.emailId = emailId;
        this.password=password;
        this.name=name;
        this.isSeller=isSeller;
        this.addressId=addressId;
    }

    public boolean hasAnyAddress() {
        if(addressId == NO_ADDRESS){
            return false;
        }else {
            return true;
        }
    }

    public void setContact(String contactNo) {
        this.contactNo=contactNo;
    }

    public String getContact() {
        return contactNo;
    }

    public void setEmailId(String emailId) {
        this.emailId=emailId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setPassword(String password) {
        this.password=password;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setIsSeller(boolean isSeller) {
        this.isSeller=isSeller;
    }

    public boolean getIsSeller() {
        return isSeller;
    }

    public void setAddressId(int addressId) {
        this.addressId=addressId;
    }

    public int getAddressId() {
        return addressId;
    }

    public static User fetchUser(String userId, Connection con) throws SQLException {

        User user = new User();

        String query = "select name, email, is_seller, curr_address from user where contact_no=?";

        try(
                PreparedStatement ps = con.prepareStatement(query)
        ) {

            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();
            rs.next();

            user.setName(rs.getString(1));
            user.setEmailId(rs.getString(2));
            user.setContact(userId);
            user.setIsSeller(rs.getBoolean(3));
            user.setAddressId(rs.getInt(4));
            rs.close();

        } catch (SQLException throwables) {
            throw throwables;
        }

        return user;
    }

    public static void addNewUser(User user, Connection con) throws SQLException {

        String query = "insert into user(name, email, password, contact_no) values(?,?,?,?)";

        try(
                PreparedStatement statement = con.prepareStatement(query))
        {

            String contact = user.getContact();

            if((contact.length() != 10) || (contact.charAt(0) <= '5')){
                throw new SQLException("Incorrect Contact");
            }

            statement.setString(4,user.getContact());
            statement.setString(3, user.getPassword());
            statement.setString(2,user.getEmailId());
            statement.setString(1, user.getName());

            statement.executeUpdate();

        } catch (SQLException throwables) {
            if(throwables.getErrorCode() == 1062){
                /**
                 * This error code is for duplicate entry of contact no
                 */
                SQLException exception = new SQLException("Phone no already exist");
                throw exception;
            }

            else if(throwables.getErrorCode() == 1406){
                /**
                 * This error code is when input parameters are not in the range.
                 */
                SQLException exception = new SQLException("Contact number not valid !!");
                throw exception;
            }
            throw throwables;
        }

    }

    public static void deleteUser(String contactNo, Connection con) throws SQLException {

        String query = "delete from user where contact_no=?";

        try(
                PreparedStatement statement = con.prepareStatement(query))
        {

            statement.setString(1,contactNo);
            statement.executeUpdate();

        } catch (SQLException throwables) {
            throw throwables;
        }

    }

    public static void updateUser(User user, Connection con) throws SQLException {

        String query = "update user set name=?, email=? " +
                "where contact_no=?";

        try(
                PreparedStatement statement = con.prepareStatement(query))
        {

            statement.setString(1,user.getName());
            statement.setString(2,user.getEmailId());
            statement.setString(3, user.getContact());

            statement.executeUpdate();

        } catch (SQLException throwables) {
            throw throwables;
        }
    }

    public static boolean checkUser(User user, Connection con) throws SQLException {

        boolean success = false;
        String query = "select password from user where contact_no=?";

        try(
                PreparedStatement ps = con.prepareStatement(query)
        )
        {
            String contact = user.getContact();

            if((contact.length() != 10) || (contact.charAt(0) <= '5')){
                throw new SQLException("Incorrect Contact");
            }

            ps.setString(1, user.getContact());

            ResultSet rs = ps.executeQuery();

            String password = null;

            while(rs.next()) {
                password = rs.getString(1);
            }

            if(user.getPassword().equals(password)){
                success = true;
            }else{
                if(password == null){
                    throw new SQLException("No Account with this contact.\nPlease register");
                }
                throw new SQLException("Incorrect Password");
            }

        } catch (SQLException throwables) {

            throw throwables;
        }

        return success;
    }

    public static void changeCurrentAddress(String userId, int addressId, Connection con) throws SQLException {

        String query="update user "+
                "set curr_address=? "+
                "where contact_no=?";
        try(
                PreparedStatement statement = con.prepareStatement(query);
        ){

            statement.setInt(1, addressId);
            statement.setString(2, userId);
            statement.executeUpdate();

        } catch (SQLException throwables) {
            throw throwables;
        }
    }

    public static ArrayList<Product> fetchItemsInCart(String contactNo, Connection con) throws SQLException {

        String query =
                "select p.name, p.description, p.price, T.quantity, p.seller_id, p.photo, p.id " +
                "from (select product_id, quantity " +
                "      from items_in_cart " +
                "      where user_id=?) as T, product as p " +
                "where p.id=T.product_id ";

        ArrayList<Product> arrList=new ArrayList<>();

        try(
                PreparedStatement statement = con.prepareStatement(query)
        ){
            statement.setString(1, contactNo);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Product product = new Product();

                product.setName(rs.getString(1));
                product.setDescription(rs.getString(2));
                product.setPrice(rs.getFloat(3));
                product.setQuantity(rs.getInt(4));
                product.setSellerId(rs.getString(5));
                product.setPhoto(PhotoHelper.convertBlobToBitmap(rs.getBlob(6)));
                product.setId(rs.getInt(7));

                arrList.add(product);
            }

        } catch (SQLException throwables) {
            throw throwables;
        }

        return arrList;
    }

    public static void addToShoppingCart(String userId, int productId, Connection con) throws SQLException {

        String query="insert into items_in_cart(quantity, user_id, product_id) " +
                "values(?,?,?)";

        try (
                PreparedStatement statement=con.prepareStatement(query)
        ){

            statement.setInt(1, 1);
            statement.setString(2, userId);
            statement.setInt(3, productId);

            statement.executeUpdate();

        }catch(SQLException throwables) {
            throw throwables;
        }

    }

    public static void removeItemFromCart(String userId, int productId, Connection con) throws SQLException {


        String query="delete from items_in_cart " +
                "where user_id=? and product_id=?";
        try(
                PreparedStatement statement=con.prepareStatement(query)
        ){

            statement.setString(1, userId);
            statement.setInt(2, productId);

            statement.executeUpdate();

        }catch(SQLException throwables) {
            throw throwables;
        }

    }

    public static void updateItemQuantityInCart(String userId, int productId, int quantity, Connection con) throws SQLException {

        String query="update items_in_cart set quantity=? " +
                "where user_id=? and product_id=?";

        try (
                PreparedStatement statement=con.prepareStatement(query)
        ){
            System.out.println("quantity : "+quantity+" user id "+userId+" pID "+productId);
            statement.setInt(1, quantity);
            statement.setString(2, userId);
            statement.setInt(3, productId);

            statement.executeUpdate();

        }catch(SQLException throwables) {
            throw throwables;
        }

    }

}