package com.bhavin.onlinecityshop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Order {

    private int id;
    private int paymentStatus;
    private int deliveryStatus;
    private String dateAndtime;
    private float totalAmount;
    private int itemCount;
    private int deliveryAddressId;
    private String SellerId;
    private String SellerName;
    private String ConsumerName;

    public static final int NOT_PAID = 0;
    public static final int PAID = 1;
    public static final int ORDER_BOOKED = 0;
    public static final int ON_THE_WAY = 1;
    public static final int DELIVERED = 2;
    public static final int CANCELLED = 3;

    public String getDecodedDeliveryStatus(){
        switch (deliveryStatus){
            case ORDER_BOOKED:
                return "Order Booked";
            case ON_THE_WAY:
                return "On The Way";
            case DELIVERED:
                return "Delivered";
            case CANCELLED:
                return "Cancelled";
            default:
                return "Invalid Data";
        }
    }

    public String getDecodedPaymentStatus(){
        switch (paymentStatus){
            case PAID:
                return "Paid";
            case NOT_PAID:
                return "Not Paid";
            default:
                return "Invalid Data";
        }
    }

    public String getConsumerName() {
        return ConsumerName;
    }

    public void setConsumerName(String consumerName) {
        ConsumerName = consumerName;
    }

    public String getSellerName() {
        return SellerName;
    }

    public void setSellerName(String sellerName) {
        SellerName = sellerName;
    }

    public String getSellerId() {
        return SellerId;
    }

    public void setSellerId(String sellerId) {
        SellerId = sellerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(int paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public int getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(int deliveryStatus) {
        if(deliveryStatus>=0 && deliveryStatus<=3)
            this.deliveryStatus = deliveryStatus;
        else
            System.out.println("Invalid Address Status");
    }

    public String getDateAndtime() {
        return dateAndtime;
    }

    public void setDateAndtime(String dateAndtime) {
        this.dateAndtime = dateAndtime;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getDeliveryAddressId() {
        return deliveryAddressId;
    }

    public void setDeliveryAddressId(int deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
    }

    public static ArrayList<Order> fetchOrdersForUserID(String userId, Connection con) throws SQLException {

        String query="select O.date_time, O.delivery_status, S.shop_name, R.total_price, O.id " +
                "from orders as O, order_total as R, seller as S " +
                "where R.user_id=? and O.user_id=? and S.seller_id=O.seller_id and R.order_id=O.id";
        ArrayList<Order> getdata = new ArrayList<>();

        try(
                PreparedStatement preparedStatement = con.prepareStatement(query)
        ) {

            preparedStatement.setString(1,userId);
            preparedStatement.setString(2,userId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Order a= new Order();
                a.setDateAndtime(rs.getString(1));
                a.setSellerName(rs.getString(3));
                a.setTotalAmount(rs.getInt(4));
                a.setDeliveryStatus(rs.getInt(2));
                a.setId(rs.getInt(5));
                getdata.add(a);
            }
        }
        catch (SQLException throwables) {
            throw throwables;
        }
        return getdata;
    }

    public static ArrayList<Order> fetchNewOrderReceivedToSeller(String sellerId,
                                                                 Connection con) throws SQLException {

        String query = "select O.date_time, O.delivery_status, U.name, R.total_price, O.id " +
                "from orders as O, order_total as R, user as U " +
                "where R.user_id=O.user_id and U.contact_no=O.user_id and R.order_id=O.id and O.delivery_status=? " +
                "and O.seller_id=?";

        ArrayList<Order> getdata = new ArrayList<>();
        try(
                PreparedStatement preparedStatement = con.prepareStatement(query))
        {
            preparedStatement.setInt(1, ORDER_BOOKED);
            preparedStatement.setString(2, sellerId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                Order a= new Order();
                a.setDateAndtime(rs.getString(1));
                a.setConsumerName(rs.getString(3));
                a.setTotalAmount(rs.getInt(4));
                a.setDeliveryStatus(rs.getInt(2));
                a.setId(rs.getInt(5));
                getdata.add(a);
            }
        }
        catch (SQLException throwables) {
            throw throwables;
        }
        return getdata;
    }

    public static ArrayList<Order> fetchOldOrderOfSeller(String sellerId, Connection con) throws SQLException {

        String query = "select O.date_time, O.delivery_status, U.name, R.total_price, O.id " +
                        "from orders as O, order_total as R, user as U " +
                        "where R.user_id=O.user_id and U.contact_no=O.user_id " +
                                "and R.order_id=O.id and O.delivery_status!=? " +
                                "and O.seller_id=?";

        ArrayList<Order> getdata = new ArrayList<>();
        try(
                PreparedStatement preparedStatement = con.prepareStatement(query))
        {
            preparedStatement.setInt(1, ORDER_BOOKED);
            preparedStatement.setString(2,sellerId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Order a= new Order();
                a.setDateAndtime(rs.getString(1));
                a.setConsumerName(rs.getString(3));
                a.setTotalAmount(rs.getInt(4));
                a.setDeliveryStatus(rs.getInt(2));
                a.setId(rs.getInt(5));
                getdata.add(a);
            }
            con.close();
        }
        catch (SQLException throwables) {
            throw throwables;
        }
        return getdata;
    }

    public static void addNewOrder(String user_id, Order order,
                                   ArrayList<Product> products, Connection con) throws SQLException {

        int orderId=-1;
        String oid= "select data_value from last_order_id where id=1";
        String query1 = "insert into orders(id, user_id, payment_status, address_id, seller_id) " +
                "values(?, ?, ?, ?, ?)";
        String query2 = "insert into order_item(price, quantity, product_id,order_id) " +
                "values(?,?,?,?)";
        String updateOid ="UPDATE last_order_id SET data_value = data_value + 1 where id=1";
        try(
                PreparedStatement preparedStatement = con.prepareStatement(query1);
                PreparedStatement preparedStatement1 = con.prepareStatement(query2);
                PreparedStatement preparedStatement2 = con.prepareStatement(oid);
                Statement statement = con.createStatement()
        )
        {
            /**
             * Fetching previos orderId generated and generate new order id
             */
            ResultSet rs = preparedStatement2.executeQuery();
            rs.next();
            orderId= rs.getInt(1)+1;
            rs.close();
            /**
             * Insert order to data base
             */
            preparedStatement.setInt(1, orderId);
            preparedStatement.setString(2,user_id);
            preparedStatement.setInt(3,order.getPaymentStatus());
            preparedStatement.setInt(4,order.getDeliveryAddressId());
            preparedStatement.setString(5,products.get(0).getSellerId());
            preparedStatement.executeUpdate();

            /**
             * Update previous data to +1 in data_val
             */
            statement.executeUpdate(updateOid);

            for(int i=0;i<products.size();i++)
            {
                preparedStatement1.setFloat(1,products.get(i).getPrice());
                preparedStatement1.setInt(2,products.get(i).getQuantity());
                preparedStatement1.setInt(3,products.get(i).getId());
                preparedStatement1.setInt(4,orderId);
                preparedStatement1.executeUpdate();
            }
        }
        catch (SQLException throwables)
        {
            throw throwables;
        }
    }

    public static Order fetchOrderDetails(int orderId, Connection con) throws SQLException {

        String Query = "SELECT u.name, o.payment_status, o.date_time, o.delivery_status, o.seller_id " +
                       "FROM orders o,user u " +
                       "where o.user_id=u.contact_no and o.id=?";
        Order a = null;
        try(
                PreparedStatement preparedStatement = con.prepareStatement(Query)
        ){
            preparedStatement.setInt(1,orderId);
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                a = new Order();
                a.setConsumerName(rs.getString(1));
                a.setId(orderId);
                a.setPaymentStatus(rs.getInt(2));
                a.setDateAndtime(rs.getString(3));
                a.setDeliveryStatus(rs.getInt(4));
                a.setSellerId(rs.getString(5));
            }
        }
        catch (SQLException throwables) {
            throw throwables;
        }
        return a;
    }

    public static Seller fetchSellerOfOrder(int orderId, Connection con) throws SQLException {

        String query = "select S.shop_name, S.address, S.banner, S.seller_id, S.rating, S.rating_count " +
                    "from seller as S, orders as O " +
                    "where S.seller_id=O.seller_id and O.id=?";
        Seller a = null;

        try (
                PreparedStatement preparedStatement = con.prepareStatement(query)
        ){
            preparedStatement.setInt(1,orderId);
            System.out.println(orderId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                a= new Seller();

                a.setShopName(rs.getString(1));
                a.setAddress(rs.getString(2));
                a.setBanner(PhotoHelper.convertBlobToBitmap(rs.getBlob(3)));
                a.setSellerId(rs.getString(4));
                a.setRating(rs.getFloat(5));
                a.setRatingCount(rs.getInt(6));
            }
        }
        catch (SQLException throwables) {
            throw throwables;
        }
        return a;
    }

    public static Address fetchDeliveryAddress(int OrderId,Connection con) throws SQLException {

        String query="select a.street,a.city,a.phone_number " +
                     "from address a,orders o " +
                     "where o.address_id=a.id and o.id=?";
        Address a= null;
        try(PreparedStatement preparedStatement = con.prepareStatement(query))
        {
            preparedStatement.setInt(1,OrderId);
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                a = new Address();
                a.setStreet(rs.getString(1));
                a.setCity(rs.getString(2));
                a.setPhoneNo(rs.getString(3));
            }
        }
        catch (SQLException throwables) {
            throw throwables;
        }
        return a;
    }

    public static ArrayList<Product> fetchItemsInOrder(int orderId, Connection con) throws SQLException {

        String query="select p.name,oi.price,oi.quantity,oi.product_id,p.photo, p.rating, p.rating_count " +
                "from product p, order_item oi " +
                "where p.id=oi.product_id and oi.order_id=?";
        ArrayList<Product> getdata = new ArrayList<>();

        try(
                PreparedStatement preparedStatement = con.prepareStatement(query)
        ){
            preparedStatement.setInt(1,orderId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Product a= new Product();

                a.setName(rs.getString(1));
                a.setPrice(rs.getFloat(2));
                a.setQuantity(rs.getInt(3));
                a.setId(rs.getInt(4));
                a.setPhoto(PhotoHelper.convertBlobToBitmap(rs.getBlob(5)));
                a.setRating(rs.getFloat(6));
                a.setRatingCount(rs.getInt(7));

                getdata.add(a);
            }
        }
        catch (SQLException throwables) {
            throw throwables;
        }
        return getdata;
    }

    public static void changeDeliveryStatus(int orderId,int deliveryStatus, Connection con) throws SQLException {

        String query = "UPDATE orders SET delivery_status = ? WHERE orders.id = ?";

        try (
                PreparedStatement preparedStatement = con.prepareStatement(query)
        ){
            preparedStatement.setInt(2,orderId);
            preparedStatement.setInt(1, deliveryStatus);

            preparedStatement.executeUpdate();
        }
        catch (SQLException throwables) {
            throw throwables;
        }
    }

    public static void changePaymentStatus(int orderId,int paymentStatus, Connection con) throws SQLException {

        String query = "UPDATE orders SET payment_status = ? WHERE orders.id = ?";

        try (
                PreparedStatement preparedStatement = con.prepareStatement(query)
        ){
            preparedStatement.setInt(2,orderId);
            preparedStatement.setInt(1, paymentStatus);

            preparedStatement.executeUpdate();
        }
        catch (SQLException throwables) {
            throw throwables;
        }
    }
}