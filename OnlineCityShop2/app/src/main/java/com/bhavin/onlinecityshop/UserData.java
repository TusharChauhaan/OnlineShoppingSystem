package com.bhavin.onlinecityshop;

import java.util.ArrayList;

public abstract class UserData {

    public static User user = new User();
    public static ArrayList<Address> addresses = new ArrayList<>();
    public static Seller seller = new Seller();
    public static ArrayList<Order> orders = new ArrayList<>();
    public static ArrayList<Product> inMyCart = new ArrayList<>();
    public static Address currAddress = null;
    public static boolean productUpdated = false;
    public static Seller cartItemsSeller = null;

    public static ArrayList<String> address = new ArrayList<>();

    static {
        address.add("Address 1");
        address.add("Address 2");
        address.add("Address 3");
        address.add("Address 4");
        address.add("Address 5");
    }
    /**
     * Data fetched for shops
     */
    public static ArrayList<Seller> shops = new ArrayList<>();

    public static final String URL="jdbc:mysql://65.19.141.67:3306/bhavin70_OnlineCityShop";
    public static final String USER = "bhavin70_bhavin";
    public static final String PASSWORD = "bhavin123";

    public static Address getAddress(int addressId){
        for(int i=0;i<addresses.size();i++){
            if(addresses.get(i).getId() == addressId){
                return addresses.get(i);
            }
        }
        return null;
    }

    public static Address getLastAddress(){
        int n = addresses.size() - 1;
        return addresses.get(n);
    }

    public static Seller getSeller(String sellerId){
        for(int i=0;i<shops.size();i++){
            if(shops.get(i).getSellerId().equals(sellerId)){
                return shops.get(i);
            }
        }
        return null;
    }

    public static Order getOrder(int orderId){
        for(int i=0;i<orders.size();i++){
            if(orders.get(i).getId() == orderId){
                return orders.get(i);
            }
        }
        return null;
    }

    public static Product getProduct(int productId){
        for(int i=0;i<inMyCart.size();i++){
            if(inMyCart.get(i).getId() == productId){
                return inMyCart.get(i);
            }
        }
        return null;
    }

    public static int itemCountInCart(){
        int sum=0;
        for(int i=0;i<inMyCart.size();i++){
            int qty = inMyCart.get(i).getQuantity();
            sum = sum + qty;
        }
        return sum;
    }

    public static float getSumTotalOfCart(){
        float sum = 0;
        for(int i=0;i<inMyCart.size();i++){
            int qty = inMyCart.get(i).getQuantity();
            float price = inMyCart.get(i).getPrice();
            sum = sum + (qty*price);
        }
        return sum;
    }

    public static boolean checkForSameSeller(String sellerId){
        for(int i=0;i<inMyCart.size();i++){
            if(!inMyCart.get(i).getSellerId().equals(sellerId)){
                /**
                 * If any item mismatch then return false
                 */
                return false;
            }
        }
        return true;
    }

}
