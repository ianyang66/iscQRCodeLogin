package cn.trunch.auth.http;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * 處理服務器端回調
 */
public abstract class Api {

    private ApiListener apiListener = null; // 將成功與否通過該接口回調

    private JSONObject jsonObject; // 響應結果

    private final OkHttpCallback okHttpCallback = new OkHttpCallback() {

        @Override
        protected boolean isRunOnUiThread() {
            return isBackToUiThread();
        }

        @Override
        public void onSuccess(Call call, JSONObject jsonObject) { // 成功收到響應結果
            Api.this.jsonObject = jsonObject;
            if (isSuccess()) { // 根據狀態碼判斷調用成功與否
                try {
                    parseData(jsonObject);
                    apiListener.success(Api.this); // 回調成功
                } catch (Exception e) {
                    e.printStackTrace();
                    apiListener.failure(Api.this); // 回調失敗，解析響應結果中的data錯誤
                }
            } else {
                try {
                    parseCode(jsonObject);
                    apiListener.failure(Api.this); // 回調失敗，狀態碼非0
                } catch (Exception e) {
                    e.printStackTrace();
                    apiListener.failure(Api.this); // 回調失敗，解析響應結果中的data錯誤
                }
            }
        }

        @Override
        public void onFailure(Call call) {
            apiListener.failure(Api.this);
        }
    };

    private boolean isSuccess() {
        return "0".equals(jsonObject.optString("code"))
                || "200".equals(jsonObject.optString("code"));
    }

    protected boolean isBackToUiThread() {
        return false;
    }

    private HashMap<String,String> paramsMap = new HashMap<>();
    void setParamsMap(HashMap<String,String> paramsMap) {
        this.paramsMap = paramsMap;
    }

    public void get(ApiListener apiListener) {
        this.apiListener = apiListener;
        OkHttpUtil.get(getUrl(), okHttpCallback, paramsMap);
    }

    public void post(ApiListener apiListener) {
        this.apiListener = apiListener;
        OkHttpUtil.post(getUrl(), okHttpCallback, paramsMap);
    }


    protected abstract void parseCode(JSONObject jsonObject) throws Exception; //解析響應狀態
    protected abstract void parseData(JSONObject jsonObject) throws Exception; //解析響應結果

    protected abstract String getUrl();
}
