package cn.trunch.auth.service;

import cn.trunch.auth.dao.AuthDao;
import cn.trunch.auth.entity.Auth;
import cn.trunch.auth.entity.Message;
import cn.trunch.auth.util.HttpUtil;
import cn.trunch.auth.util.IPUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;


@Service
public class AuthService {

    @Resource
    AuthDao authDao;

    public Message addAuthInfo(HttpServletRequest request) {
        // 通過UUID生成隨機的token
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        // 通過IPUtil獲取客戶端的真實IP地址
        String ip = IPUtil.getIpAddress(request);
        // 本機端測試的時候,上面獲取的IP為127.0.0.1
        // 所以無法通過局域網獲得地理位置，因此手動改為外網IP
        String fakeip = "134.208.97.100";
        // 通過獲取到的客戶端IP地址確定客戶端所在地理位置
        String address;
        String address1;
        // 使用baidu接口獲取IP地址
        String url = "http://api.map.baidu.com/location/ip?ip=" + fakeip + "&ak=nSxiPohfziUaCuONe4ViUP2N&coor=bd09ll";
        //String url = "http://ip-api.com/json/" + fakeip + "?lang=zh-CN";
        // 通過Http工具訪問接口
        String result = HttpUtil.doGet(url);
        // 對返回的數據進行解析
        JSONObject jsonObject = JSONObject.parseObject(result);
        if (jsonObject != null) {
            Integer status = jsonObject.getInteger("status");
            if (status == 0) {
                address1 = jsonObject.getJSONObject("content").getString("address");
                // 將簡體轉繁體
                address = ZhConverterUtil.convertToTraditional(address1);
                address = address.replace("省", "");
                System.out.println(address);
            } else {
                address = "台灣花蓮縣"; // 解析失敗，默認地址
            }
        } else {
            address = "台灣花蓮縣"; // 獲取baidu接口數據失敗
        }
        // 將token相關信息存入數據庫中
        authDao.addAuthInfo(token, ip, address);
        // 將token返回给客戶端
        return new Message(200, "獲取App失敗", token);
    }

    public Message getAuthInfo(String authToken, String userId, boolean isScan) {
        Auth auth = authDao.getAuthInfo(authToken);
        // 為空則獲取資訊失敗
        if (auth == null) {
            return new Message(201, "獲取App資訊失敗", new Auth());
        }
        //手机端访问，如果token等待验证或正在验证，则将token的state和userId更新
        if (isScan && (auth.getAuthState() == 0 || auth.getAuthState() == 2)) {
            authDao.setAuthState(authToken, 2, userId);
        }
        return new Message(200, "獲取App資訊失敗", auth);
    }

    public Message setAuthState(String authToken, String userId) {
        //tokenState：0等待驗證，1驗證成功，2正在驗證，3驗證失敗（過期）
        Integer state = 3; // 默認token為3，不存在
        Auth auth = authDao.getAuthInfo(authToken);
        if (null != auth) {
            state = auth.getAuthState(); // 獲得token的狀態
        }
        Message message = new Message();
        HashMap<String, Integer> hashMap = new HashMap<>();
        if (userId != null && (state == 0 || state == 2)) { // token狀態為0，等待驗證
            // TODO 要判斷token的時間是否已經過期，可以通過時間戳相減獲得
            System.out.println("===" + (System.currentTimeMillis() - auth.getAuthTime().getTime()));
            authDao.setAuthState(authToken, 1, userId);
            message.setCode(200);
            message.setMessage("使用App成功");
            hashMap.put("state", 1);
        } else { // token狀態為1或3，失效
            message.setCode(201);
            message.setMessage("使用App失敗");
            hashMap.put("state", 0);
        }
        message.setData(hashMap);
        return message;
    }
}
