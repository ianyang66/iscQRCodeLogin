package cn.trunch.auth.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpUtil {
    public static String doGet(String httpurl) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;// 返回結果字符串
        try {
            // 創建遠程url連接對象
            URL url = new URL(httpurl);
            // 通過遠程url連接對像打開一個連接，強轉成httpURLConnection類
            connection = (HttpURLConnection) url.openConnection();
            // 設置連接方式：get
            connection.setRequestMethod("GET");
            // 設置連接主機服務器的超時時間：15000毫秒
            connection.setConnectTimeout(15000);
            // 設置讀取遠程返回的數據時間：60000毫秒
            connection.setReadTimeout(100000);
            // 發送請求
            connection.connect();
            // 通過connection連接，獲取輸入流
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 封裝輸入流is，並指定字符集
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                // 存放數據
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 關閉資源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            connection.disconnect();// 關閉遠程連接
        }

        return result;
    }

    public static String doPost(String httpUrl, String param) {

        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
        try {
            URL url = new URL(httpUrl);
            // 通過遠程url連接對像打開連接
            connection = (HttpURLConnection) url.openConnection();
            // 設置連接請求方式
            connection.setRequestMethod("POST");
            // 設置連接主機服務器超時時間：15000毫秒
            connection.setConnectTimeout(15000);
            // 設置讀取主機服務器返回數據超時時間：60000毫秒
            connection.setReadTimeout(60000);

            // 默認值為：false，當向遠程服務器傳送數據/寫數據時，需要設置為true
            connection.setDoOutput(true);
            // 默認值為：true，當前向遠程服務讀取數據時，設置為true，該參數可有可無
            connection.setDoInput(true);
            // 設置傳入參數的格式:請求參數應該是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 設置鑑權信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
            connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
            // 通過連接對象獲取一個輸出流
            os = connection.getOutputStream();
            // 通過輸出流對象將參數寫出去/傳輸出去,它是通過字節數組寫出的
            os.write(param.getBytes());
            // 通過連接對象獲取一個輸入流，向遠程讀取
            if (connection.getResponseCode() == 200) {

                is = connection.getInputStream();
                // 對輸入流對象進行包裝:charset根據工作項目組的要求來設置
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

                StringBuffer sbf = new StringBuffer();
                String temp = null;
                // 循環遍歷一行一行讀取數據
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 關閉資源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 斷開與遠程地址url的連接
            connection.disconnect();
        }
        return result;
    }

    public static String decodeUnicode(String dataStr) {
        int start = 0;
        int end;
        final StringBuffer buffer = new StringBuffer();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr;
            if (end == -1) {
                charStr = dataStr.substring(start + 2, dataStr.length());
            } else {
                charStr = dataStr.substring(start + 2, end);
            }
            char letter = (char) Integer.parseInt(charStr, 16); // 16進制parse整形字符串
            buffer.append(new Character(letter).toString());
            start = end;
        }
        return buffer.toString();
    }
}