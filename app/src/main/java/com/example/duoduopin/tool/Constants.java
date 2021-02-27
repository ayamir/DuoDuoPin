package com.example.duoduopin.tool;

public class Constants {
    private static final String baseUrl = "http://123.57.12.189:8080/";
    private static final String wsUrl = "ws://123.57.12.189:8080/";
    public static final String registerUrl = baseUrl + "User/register/";
    public static final String loginUrl = baseUrl + "User/login/";
    public static final String logoutUrl = baseUrl + "User/logout/";

    public static final String queryUrl = baseUrl + "ShareBill/";
    public static final String queryByInfoUrl = baseUrl + "ShareBill/info/";
    public static final String createOrderUrl = baseUrl + "ShareBill/add/";
    public static final String delOrderUrl = baseUrl + "ShareBill/del/";
    public static final String applyUrl = baseUrl + "ShareBill/apply/";
    public static final String joinUrl = baseUrl + "ShareBill/join/";
    public static final String quitUrl = baseUrl + "ShareBill/quit/";

    public static final String queryUserUrl = baseUrl + "User/";

    public static final String chatUrl = wsUrl + "ws/chat/";
    public static final String queryChatMessageUrl = baseUrl + "chat/";

    public static final String sysMessageUrl = wsUrl + "ws/system/";
    public static final String checkSysMessageUrl = baseUrl + "system/check/";
    public static final String broadMessageUrl = baseUrl + "system/broad/";

    public static String getQueryUserUrl(String userId) {
        return queryUserUrl + userId;
    }

    public static String getQueryUrlByOrderId(String orderId) {
        return queryUrl + orderId;
    }

    public static String getQueryUrlByUserId(String userId) {
        return queryUrl + "user/" + userId;
    }

    public static String getDelOrderUrl(String orderId) {
         return delOrderUrl + orderId;
    }

    public static String getJoinUrl(String orderId) {
        return joinUrl + orderId;
    }

    public static String getAllowUrl(String orderId) {
        return applyUrl + orderId + "/allow/";
    }

    public static String getRejectUrl(String orderId) {
        return applyUrl + orderId + "/reject/";
    }

    public static String getQuitUrl(String orderId, String userId) {
        return quitUrl + orderId + "/" + userId;
    }

    public static String getChatUrl(String orderId) {
        return chatUrl + orderId;
    }

    public static String getQueryChatMessageUrl(String orderId) {
        return queryChatMessageUrl + orderId;
    }

    public static String getQueryUserMessageUrl(String orderId, String userId) {
        return queryChatMessageUrl + orderId + "/" + userId;
    }

    public static String getOfflineMessageUrl(String orderId) {
        return baseUrl + orderId + "/unchecked";
    }

    public static String getSysMessageUrl(String userId) {
        return sysMessageUrl +userId;
    }
}
