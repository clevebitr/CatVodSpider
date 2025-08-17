package com.github.catvod.utils;

import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.util.Map;

public class VideoUrlParser {

    public static String getVideoUrl(String inputUrl, Map<String, String> header) {
        try {
            // 1. 提取 url 参数
            String urlParam = extractUrlParam(inputUrl);
            SpiderDebug.log(" url 参数 is "+urlParam);
            if (urlParam.isEmpty()) {
                SpiderDebug.log("没有找到 url 参数");
                return "";
            } else if (urlParam.toLowerCase().endsWith(".m3u8")) {
                SpiderDebug.log("请求返回的urlParam: " + urlParam);
                return urlParam;
            }

            // 2. 第一次请求：获取 signed_url
            String apiUrl = "https://mtyy1.com/static/player/pdzy.php";
            String firstUrl = apiUrl + "?get_signed_url=1&url=" + URLEncoder.encode(urlParam, "UTF-8");

            String response1 = OkHttp.string(firstUrl, header);
            JSONObject json1 = new JSONObject(response1);
            String signedUrl = json1.getString("signed_url");
            SpiderDebug.log("signedUrl is "+signedUrl);

            // 判断 signedUrl 是否为 M3u8 链接
            if (signedUrl.toLowerCase().endsWith(".m3u8")) {
                SpiderDebug.log("第一次请求返回的 signedUrl: " + signedUrl);
                SpiderDebug.log("signedUrl is "+signedUrl);
                return signedUrl;
            }

            // 3. 第二次请求：使用 signed_url 获取 jmurl
            String apiUrl2 = "https://mtyy2.com/static/player/art.php";
            String secondUrl = apiUrl2 + signedUrl.replace("?url=", "?url="); // 确保URL格式正确

            SpiderDebug.log("第二次请求的 secondUrl: " + secondUrl);
            String response2 = OkHttp.string(secondUrl, header);
            JSONObject json2 = new JSONObject(response2);

            SpiderDebug.log("第二次请求返回的 json2: " + json2);

            String jmurl = json2.getString("jmurl");
            SpiderDebug.log("第二次请求返回的 jmurl: " + jmurl);

            // 判断 jmurl 是否为 HTML 链接
            if (jmurl.toLowerCase().endsWith(".html")) {
                // 4. 第三次请求：获取最终视频地址
                String finalUrl = OkHttp.string(jmurl, header);
                SpiderDebug.log("第三次请求返回的 finalUrl: " + finalUrl);
                SpiderDebug.log("finalUrl is "+finalUrl);
                return finalUrl;
            }

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