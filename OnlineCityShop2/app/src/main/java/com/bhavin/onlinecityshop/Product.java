package com.bhavin.onlinecityshop;

import android.graphics.Bitmap;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Product {

    private int id;
    private float price;
    private String name;
    private String description;
    private int availableUnits;
    private float rating;
    private int ratingCount;
    private Bitmap photo;

    private int quantity;
    private String sellerId;

    public Product(){
        this.id = 0;
        this.price = 0;
        this.name = "";
        this.description = "";
        this.availableUnits = 0;
        this.rating=0;
        this.ratingCount=0;
    }

    public Product(int id, float price, String name, String description,
                   int availableUnits, float rating, int ratingCount, int quantity) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.description = description;
        this.availableUnits = availableUnits;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.quantity = quantity;
    }

    public Product(Product product){
        this.id = product.getId();
        this.price = product.getPrice();
        this.name = product.getName();
        this.description = product.getDescription();
        this.availableUnits = product.getAvailableUnits();
        this.rating = product.getRating();
        this.quantity = product.getQuantity();
        this.ratingCount = product.getRatingCount();
        this.photo = product.getPhoto();
        this.sellerId = product.getSellerId();
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAvailableUnits() {
        return availableUnits;
    }

    public void setAvailableUnits(int availableUnits) {
        this.availableUnits = availableUnits;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static Product fetchProduct(int productId, Connection con) throws SQLException {

        String query = "select photo, name, description, rating, rating_count, price, available_units " +
                "from product " +
                "where id=?";
        Product product = null;

        try(
                PreparedStatement statement = con.prepareStatement(query))
        {

            statement.setInt(1,productId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                product = new Product();
                //set attributes
                product.setId(productId);
                product.setPhoto(PhotoHelper.convertBlobToBitmap(resultSet.getBlob(1)));
                product.setPrice(resultSet.getFloat(6));
                product.setName(resultSet.getString(2));
                product.setDescription(resultSet.getString(3));
                product.setAvailableUnits(resultSet.getInt(7));
                product.setRating(resultSet.getFloat(4));
                product.setRatingCount(resultSet.getInt(5));
            }

        } catch (SQLException throwables) {
            throw throwables;
        }

        return product;
    }

    public static boolean insertProduct(Product product, String sellerId, Connection con) throws IOException, SQLException {

        boolean success = false;
        String query = "insert into product(seller_id, photo, available_units, name, description, price) " +
                "values(?, ?, ?, ?, ?, ?)";

        try(
                PreparedStatement statement = con.prepareStatement(query)
        )
        {

            statement.setString(1, sellerId);
            statement.setBinaryStream(2, PhotoHelper.compressAndConvertToIS(product.getPhoto()));
            statement.setInt(3, product.getAvailableUnits());
            statement.setString(4, product.getName());
            statement.setString(5, product.getDescription());
            statement.setFloat(6, product.getPrice());

            int rowAffected = statement.executeUpdate();

            if(rowAffected>0){
                success = true;
            }

        } catch (SQLException | IOException throwables) {
            throw throwables;
        }

        return success;
    }

    public static boolean deleteProduct(int productId, Connection con) throws SQLException {

        boolean success = false;
        String query = "delete from product where id=?";

        try(
                PreparedStatement statement = con.prepareStatement(query))
        {

            statement.setInt(1,productId);

            int rowAffected = statement.executeUpdate();

            if(rowAffected>0){
                success = true;
            }

        } catch (SQLException throwables) {
            throw throwables;
        }

        return success;
    }

    public static boolean updateProduct(Product product, Connection con) throws IOException, SQLException {

        boolean success = false;
        String query =  "update product " +
                        "set name=?, description=?, photo=?, price=? " +
                        "where id=?";

        try(
                PreparedStatement statement = con.prepareStatement(query)
        )
        {

            statement.setString(1,product.getName());
            statement.setString(2, product.getDescription());
            statement.setBinaryStream(3, PhotoHelper.compressAndConvertToIS(product.getPhoto()));
            statement.setFloat(4,product.getPrice());

            int rowAffected = statement.executeUpdate();

            if(rowAffected>0){
                success = true;
            }

        } catch (SQLException | IOException throwables) {
            throw throwables;
        }

        return success;
    }

    public static ArrayList<Product> searchItems(String search, Connection con) throws SQLException {

        ArrayList<Product> result = new ArrayList<>();

        String query =  "select id, photo, name, description, rating, rating_count, price, available_units \n" +
                        "from product \n" +
                        "where match(name) against(?) or match(description) against(?)";

        try(
                PreparedStatement statement = con.prepareStatement(query)
        )
        {

            statement.setString(1, search);
            statement.setString(2, search);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                Product product = new Product();
                product.setId(resultSet.getInt(1));
                product.setPhoto(PhotoHelper.convertBlobToBitmap(resultSet.getBlob(2)));
                product.setName(resultSet.getString(3));
                product.setDescription(resultSet.getString(4));
                product.setRating(resultSet.getFloat(5));
                product.setRatingCount(resultSet.getInt(6));
                product.setPrice(resultSet.getFloat(7));
                product.setAvailableUnits(resultSet.getInt(8));
                result.add(product);
            }

        } catch (SQLException throwables) {
            throw throwables;
        }

        return result;
    }

    public boolean addRating(float rating, Connection con) throws SQLException {
        boolean success = false;
        String query =  "update product set rating=?, rating_count=? " +
                        "where id=?";

        try(
                PreparedStatement statement = con.prepareStatement(query)
        )
        {

            float total = (this.rating * ratingCount) + rating;
            ratingCount++;
            this.rating = total/ratingCount;
            statement.setFloat(1, this.rating);
            statement.setInt(2,ratingCount);
            statement.setInt(3,id);

            int rowAffected = statement.executeUpdate();

            if(rowAffected>0){
                success = true;
            }

        } catch (SQLException throwables) {
            throw throwables;
        }

        return success;
    }

}