package com.bhavin.onlinecityshop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Seller {

    private String sellerId;
    private String registrationDate;
    private String serviceCity;
    private String contactNumber;
    private String address;
    private String shopName;
    private String description;
    private Bitmap banner;
    private float rating;
    private int ratingCount;
    private String gstNo;

    public Seller() {

    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public Bitmap getBanner() {
        return banner;
    }

    public void setBanner(Bitmap banner) {
        this.banner = banner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getServiceCity() {
        return serviceCity;
    }

    public void setServiceCity(String serviceCity) {
        this.serviceCity = serviceCity;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getGstNo() {
        return gstNo;
    }

    public void setGstNo(String gstNo) {
        this.gstNo = gstNo;
    }

    public static boolean addNewSeller(Seller seller, Connection con) throws SQLException, IOException {

        boolean success = false;
        String query = "insert into seller(seller_id, contact_no, banner, shop_name, description, address, service_city, gst_no)\n" +
                " values (?,?, ?, ?, ?, ?, ?, ?)";
        String query1 = "update user set is_seller=? where contact_no=?";

        try (
                PreparedStatement statement = con.prepareStatement(query);
                PreparedStatement statement1 = con.prepareStatement(query1)
        ){

            statement.setString(1,seller.getSellerId());
            statement.setString(2,seller.getContactNumber());
            statement.setBinaryStream(3,
                    PhotoHelper.compressAndConvertToIS(seller.getBanner()));
            statement.setString(4, seller.getShopName());
            statement.setString(5, seller.getDescription());
            statement.setString(6, seller.getAddress());
            statement.setString(7, seller.getServiceCity());
            statement.setString(8,seller.getGstNo());

            int rowAffected = statement.executeUpdate();

            if(rowAffected>0){
                success = true;
            }

            statement1.setBoolean(1, true);
            statement1.setString(2, seller.getSellerId());
            statement1.executeUpdate();

        } catch (SQLException throwables) {
            throw throwables;
        } catch (IOException e) {
            throw e;
        }

        return success;
    }

    public static boolean deleteSeller(Seller seller, Connection con) throws SQLException {

        boolean success = false;
        String query = "delete from seller where contact_no=?";
        String query1 = "update user set is_seller=? where contact_no=?";

        try (
                PreparedStatement statement = con.prepareStatement(query);
                PreparedStatement statement1 = con.prepareStatement(query1)
        ){

            statement.setString(1,seller.getContactNumber());

            int rowAffected = statement.executeUpdate();

            if(rowAffected>0){
                success = true;
            }

            statement1.setBoolean(1, false);
            statement1.setString(2, seller.getSellerId());
            statement1.executeUpdate();

        } catch (SQLException throwables) {
            throw throwables;
        }

        return success;
    }

    public static ArrayList<Product> fetchProductsOfSeller(String sellerId, Connection con) throws SQLException {

        String query = "select p.name, p.photo, p.id, p.rating, p.rating_count, p.seller_id, p.price, p.description " +
                "from product as p " +
                "where p.seller_id=?";
        ArrayList<Product> arrList = new ArrayList<>();

        try (
                PreparedStatement statement = con.prepareStatement(query)
        ){

            statement.setString(1,sellerId);

            ResultSet rs=statement.executeQuery();

            while(rs.next())
            {
                Product product=new Product();

                product.setName(rs.getString(1));
                product.setPhoto(PhotoHelper.convertBlobToBitmap(rs.getBlob(2)));
                product.setId(rs.getInt(3));
                product.setRating(rs.getFloat(4));
                product.setRatingCount(rs.getInt(5));
                product.setSellerId(rs.getString(6));
                product.setPrice(rs.getFloat(7));
                product.setDescription(rs.getString(8));

                arrList.add(product);
            }

        } catch (SQLException throwables) {
            throw throwables;
        }

        return arrList;
    }

    public static boolean updateSeller(Seller seller, Connection con) throws SQLException, IOException {

        boolean success = false;
        String query = "update seller " +
                "set shop_name=?, description=?, address=?, service_city=?, gst_no=?, banner=?, contact_no=? " +
                "where seller_id=?";

        try (
                PreparedStatement statement = con.prepareStatement(query)
        ){

            statement.setString(1,seller.getShopName());
            statement.setString(2, seller.getDescription());
            statement.setString(3, seller.getAddress());
            statement.setString(4, seller.getServiceCity());
            statement.setString(5,seller.getGstNo());
            statement.setBinaryStream(6,PhotoHelper.compressAndConvertToIS(seller.getBanner()));
            statement.setString(7, seller.getContactNumber());
            statement.setString(8, seller.getSellerId());

            int rowAffected = statement.executeUpdate();

            if(rowAffected>0){
                success = true;
            }

        } catch (SQLException throwables) {
            throw throwables;
        } catch (IOException e) {
            throw e;
        }

        return success;
    }

    public static boolean addRating(Seller seller, float rating, Connection con) throws SQLException {

        boolean success = false;
        String query = "update seller set rating=?, rating_count=? " +
                "where seller_id=?";

        try (
                PreparedStatement statement = con.prepareStatement(query)
        ){

            float total = (seller.rating * seller.ratingCount) + rating;
            System.out.println(seller.getSellerId());
            seller.ratingCount++;
            seller.rating = total/seller.ratingCount;
            statement.setFloat(1, seller.getRating());
            statement.setInt(2, seller.getRatingCount());
            statement.setString(3,seller.getSellerId());
            System.out.println(seller.getRating());

            int rowAffected = statement.executeUpdate();

            if(rowAffected>0){
                success = true;
            }

        } catch (SQLException throwables) {
            throw throwables;
        }

        return success;
    }

    public static ArrayList<Seller> searchSeller(String search, String city, Connection con) throws SQLException {

        ArrayList<Seller> result = new ArrayList<>();

        String query = "select seller_id, shop_name, description, rating, rating_count, banner, address " +
                "from seller " +
                "where (match(shop_name) against(?) or match(description) against(?))" +
                "      and service_city=?";

        try(
                PreparedStatement statement = con.prepareStatement(query)
        ){

            statement.setString(1, search);
            statement.setString(2, search);
            statement.setString(3, city);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){

                Seller seller = new Seller();

                seller.setSellerId(resultSet.getString(1));
                seller.setShopName(resultSet.getString(2));
                seller.setDescription(resultSet.getString(3));
                seller.setRating(resultSet.getFloat(4));
                seller.setRatingCount(resultSet.getInt(5));
                seller.setBanner(PhotoHelper.convertBlobToBitmap(resultSet.getBlob(6)));
                seller.setAddress(resultSet.getString(7));

                result.add(seller);
            }

        } catch (SQLException throwables) {
            throw throwables;
        }

        return result;
    }

    public static Seller fetchSellerWithUserId(String userId, Connection con) throws SQLException {
        String query = "select shop_name, description, rating, rating_count, banner, contact_no, address, service_city, gst_no, seller_id " +
                "from seller " +
                "where seller_id=?";
        Seller seller = null;

        try(
                PreparedStatement statement = con.prepareStatement(query)
        ){

            statement.setString(1, userId);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                seller = new Seller();

                seller.setSellerId(userId);
                seller.setShopName(resultSet.getString(1));
                seller.setDescription(resultSet.getString(2));
                seller.setRating(resultSet.getFloat(3));
                seller.setRatingCount(resultSet.getInt(4));
                seller.setBanner(PhotoHelper.convertBlobToBitmap(resultSet.getBlob(5)));
                seller.setContactNumber(resultSet.getString(6));
                seller.setAddress(resultSet.getString(7));
                seller.setServiceCity(resultSet.getString(8));
                seller.setGstNo(resultSet.getString(9));
                seller.setSellerId(resultSet.getString(10));
            }

        } catch (SQLException throwables) {
            throw throwables;
        }

        return seller;

    }

    public static ArrayList<Seller> fetchSellerInCity(String city, Connection con) throws SQLException {

        ArrayList<Seller> arrList = new ArrayList<>();
        String query = "select seller_id, shop_name, description, rating, rating_count, banner, address " +
                       "from seller " +
                       "where service_city=?";

        try (
                PreparedStatement statement=con.prepareStatement(query)
        ){

            statement.setString(1,city);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                Seller seller = new Seller();

                seller.setSellerId(resultSet.getString(1));
                seller.setShopName(resultSet.getString(2));
                seller.setDescription(resultSet.getString(3));
                seller.setRating(resultSet.getFloat(4));
                seller.setRatingCount(resultSet.getInt(5));
                seller.setBanner(PhotoHelper.convertBlobToBitmap(resultSet.getBlob(6)));
                seller.setAddress(resultSet.getString(7));

                arrList.add(seller);
            }

        }catch (SQLException throwables) {
            throw throwables;
        }

        return arrList;
    }

}
