package cn.trunch.auth.http;


public class ApiUtil {
    public static String USER_ID = ""; // 使用者ID
    public static String USER_TOKEN = ""; // 使用者TOKEN
    public static String USER_AVATAR = ""; // 使用者頭像
    public static String USER_NAME = ""; // 使用者暱稱


    public final static String IP_PORT = "http://192.168.68.112/";
    public final static String LOGIN = IP_PORT + "login";
    public final static String TOKEN_INFO = IP_PORT + "auth/info/";
    public final static String TOKEN_USE = IP_PORT + "auth/use/";
}
