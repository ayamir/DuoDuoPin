package com.example.duoduopin.bean;

public class OrderContent {
    private String billId;
    private String userId;
    private String nickname;
    private String title;
    private String type;
    private String description;
    private String address;
    private String time;
    private String curPeople;
    private String maxPeople;
    private String price;
    private String latitude;
    private String longitude;
    private String distance;
    private String geohash;

    public OrderContent(String billId, String userId, String nickname, String orderType, String type, String description, String address, String time, String curPeople, String maxPeople, String price) {
        this.billId = billId;
        this.userId = userId;
        this.nickname = nickname;
        this.title = title;
        this.type = orderType;
        this.description = description;
        this.address = address;
        this.time = time;
        this.curPeople = curPeople;
        this.maxPeople = maxPeople;
        this.price = price;
    }

    public OrderContent(String billId, String userId, String nickname, String title, String type, String description, String address, String time, String curPeople, String maxPeople, String price, String latitude, String longitude, String geohash, String distance) {
        this.billId = billId;
        this.userId = userId;
        this.nickname = nickname;
        this.title = title;
        this.type = type;
        this.description = description;
        this.address = address;
        this.time = time;
        this.curPeople = curPeople;
        this.maxPeople = maxPeople;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geohash = geohash;
        this.distance = distance;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCurPeople() {
        return curPeople;
    }

    public void setCurPeople(String curPeople) {
        this.curPeople = curPeople;
    }

    public String getMaxPeople() {
        return maxPeople;
    }

    public void setMaxPeople(String maxPeople) {
        this.maxPeople = maxPeople;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getGeohash() {
        return geohash;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
