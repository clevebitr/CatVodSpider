package com.github.catvod.utils;

import com.github.catvod.net.OkHttp;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class VideoUrlParser {

    public static String getVideoUrl(String inputUrl, Map<String, String> header) {
        try {
            // 1. 提取 url 参数
            String urlParam = extractUrlParam(inputUrl);
            System.out.println(" url 参数 is "+urlParam);
            if (urlParam.isEmpty()) {
                System.out.println("没有找到 url 参数");
                return "";
            }

            // 2. 第一次请求：获取 signed_url
            String apiUrl = "https://mtyy1.com/static/player/pdzy.php";
            String firstUrl = apiUrl + "?get_signed_url=1&url=" + URLEncoder.encode(urlParam, "UTF-8");

            String response1 = OkHttp.string(firstUrl, header);
            JSONObject json1 = new JSONObject(response1);
            String signedUrl = json1.getString("signed_url");
            System.out.println("signedUrl is "+signedUrl);
            // 3. 第二次请求：使用 signed_url 获取 jmurl
            String secondUrl = apiUrl + signedUrl;
            String response2 = OkHttp.string(secondUrl, header);
            System.out.println("response2 is "+response2);
            JSONObject json2 = new JSONObject(response2);
            String jmurl = json2.getString("jmurl");

             //4. 第三次请求（可选）：获取最终视频地址（如果 jmurl 是跳转地址）
//            String finalUrl = OkHttp.string(jmurl, header);
//            System.out.println("finalUrl is "+finalUrl);
//            return finalUrl;

            return jmurl;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String extractUrlParam(String url) {
        if (url.contains("?url=")) {
            return url.split("\\?url=")[1].split("&")[0];
        }
        return "";
    }
}