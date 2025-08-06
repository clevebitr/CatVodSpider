package com.github.catvod.spider;/*
 * @File     : changzhang.js
 * @Author   : jade
 * @Date     : 2024/2/2 16:02
 * @Email    : jadehh@1ive.com
 * @Software : Samples
 * @Desc     :
 */


import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.github.catvod.bean.Class;
import com.github.catvod.bean.Filter;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangZhang extends Spider {

    private String siteUrl = "https://www.czzy77.com/";
    private static final String PUBLISH_URL = "https://www.czzy.site/";

    public ChangZhang() {
        // 初始化时尝试获取最新地址
        try {
            updateSiteUrl();
        } catch (Exception e) {
            SpiderDebug.log("获取最新地址失败，使用默认地址: " + e.getMessage());
        }
    }

    private void updateSiteUrl() throws Exception {
        SpiderDebug.log("正在从发布页获取最新地址...");
        String html = OkHttp.string(PUBLISH_URL);
        Document doc = Jsoup.parse(html);

        // 从页面中提取最新地址
        String latestUrl = extractLatestUrl(doc);
        if (latestUrl != null && !latestUrl.isEmpty()) {
            this.siteUrl = latestUrl;
            SpiderDebug.log("已更新到最新地址: " + siteUrl);
            System.out.println("已更新到最新地址: " + siteUrl);
        } else {
            SpiderDebug.log("未找到最新地址，继续使用: " + siteUrl);
        }
    }

    private String extractLatestUrl(Document doc) {
        // 方法1: 从meta标签和链接中提取
        Elements links = doc.select("a[href]");
        List<String> candidateUrls = new ArrayList<>();

        for (Element link : links) {
            String href = link.attr("href");
            if (href.matches("https?://www\\.(czzy|czzymovie|cz4k|cz0101)\\.[a-z]{2,3}/?")) {
                candidateUrls.add(href);
            }
        }

        // 方法2: 从文本内容中提取URL模式
        String bodyText = doc.body().text();
        Pattern pattern = Pattern.compile("https?://www\\.(czzy|czzymovie|cz4k|cz0101)\\.[a-z]{2,3}/?");
        Matcher matcher = pattern.matcher(bodyText);

        while (matcher.find()) {
            String url = matcher.group();
            if (!candidateUrls.contains(url)) {
                candidateUrls.add(url);
            }
        }

        // 优先级排序：优先选择czzymovie.com，其次是cz4k.com
        for (String url : candidateUrls) {
            if (url.contains("czzymovie.com")) {
                return url.endsWith("/") ? url : url + "/";
            }
        }

        for (String url : candidateUrls) {
            if (url.contains("cz4k.com")) {
                return url.endsWith("/") ? url : url + "/";
            }
        }

        // 返回第一个找到的候选地址
        if (!candidateUrls.isEmpty()) {
            String url = candidateUrls.get(0);
            return url.endsWith("/") ? url : url + "/";
        }

        return null;
    }

    private Map<String, String> getHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        header.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        header.put("Connection", "keep-alive");
        // 正确设置Host和Referer
        String cleanUrl = siteUrl.replace("https://", "").replace("http://", "");
        String host = cleanUrl.replace("/", "");
        String referer = siteUrl.endsWith("/") ? siteUrl.substring(0, siteUrl.length() - 1) : siteUrl;

        header.put("Referer", referer); // 必须设置Referer
        header.put("Host", host); // 确保Host正确
        header.put("Cookie", "myannoun=1; Hm_lvt_0653ba1ead8a9aabff96252e70492497=2718862211; Hm_lvt_06341c948291d8e90aac72f9d64905b3=2718862211; Hm_lvt_07305e6f6305a01dd93218c7fe6bc9c3=2718862211");
        return header;
    }

    @Override
    public String homeContent(boolean filter) throws Exception {

        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        LinkedHashMap<String, List<Filter>> filters = new LinkedHashMap<>();
        Document doc = Jsoup.parse(OkHttp.string(siteUrl));

        for (Element div : doc.select(".navlist > li ")) {
            classes.add(new Class(div.select(" a").attr("href"), div.select(" a").text()));
        }

        getVods(list, doc);
        SpiderDebug.log("++++++++++++厂长-homeContent" + Json.toJson(list));
        SpiderDebug.log("当前使用地址: " + siteUrl);
        return Result.string(classes, list);
    }

    private void getVods(List<Vod> list, Document doc) {
        for (Element div : doc.select(".bt_img.mi_ne_kd > ul >li")) {
            String id = div.select(".dytit > a").attr("href");
            String name = div.select(".dytit > a").text();
            String pic = div.select("img").attr("data-original");
            if (pic.isEmpty()) pic = div.select("img").attr("src");
            String remark = div.select(".hdinfo > span").text();

            list.add(new Vod(id, name, pic, remark));
        }
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl + tid + "/page/" + pg;
        //String filters = extend.get("filters");
        String html = OkHttp.string(target);
        Document doc = Jsoup.parse(html);
        getVods(list, doc);
        String total = "" + Integer.MAX_VALUE;


        SpiderDebug.log("++++++++++++厂长-categoryContent" + Json.toJson(list));
        return Result.get().vod(list).page(Integer.parseInt(pg), Integer.parseInt(total) / 25 + ((Integer.parseInt(total) % 25) > 0 ? 1 : 0), 25, Integer.parseInt(total)).string();
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        Document doc = Jsoup.parse(OkHttp.string(ids.get(0), getHeader()));

        // 1. 基础信息选择器
        String title = doc.select("div.moviedteail_tt h1").text();
        String vodPic = doc.select("div.dyimg img").attr("src");

        // 2. 详细信息选择器
        Elements infoElements = doc.select("ul.moviedteail_list li");
        Map<String, String> infoMap = new HashMap<>();

        for (Element li : infoElements) {
            String text = li.text();
            if (text.contains("：")) {
                String[] parts = text.split("：", 2);
                infoMap.put(parts[0].trim(), parts[1].trim());
            }
        }

        // 3. 播放列表选择器
        Elements playElements = doc.select("div.paly_list_btn a");
        StringBuilder vod_play_url = new StringBuilder();
        StringBuilder vod_play_from = new StringBuilder("厂长").append("$$$");

        for (int i = 0; i < playElements.size(); i++) {
            String epName = playElements.get(i).text();
            String epUrl = playElements.get(i).attr("href");
            vod_play_url.append(epName).append("$").append(epUrl);
            if (i < playElements.size() - 1) {
                vod_play_url.append("#");
            }
        }
        vod_play_url.append("$$$");

//        // 4. 下载地址选择器
//        Elements downElements = doc.select("div.ypbt_down_list a");
//        StringBuilder downList = new StringBuilder();
//        for (Element a : downElements) {
//            downList.append(a.text()).append("$").append(a.attr("href")).append("#");
//        }

        // 5. 剧情简介
        String brief = doc.select("div.yp_context").text();

        // 构建VOD对象
        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodName(title);
        vod.setVodPic(vodPic);
        vod.setVodYear(infoMap.getOrDefault("年份", ""));
        vod.setVodArea(infoMap.getOrDefault("地区", ""));
        vod.setVodRemarks(infoMap.getOrDefault("上映", ""));
        vod.setVodDirector(infoMap.getOrDefault("导演", ""));
        vod.setVodActor(infoMap.getOrDefault("主演", ""));
        vod.setTypeName(infoMap.getOrDefault("类型", ""));
        vod.setVodContent(brief);
        vod.setVodPlayFrom(vod_play_from.toString());
        vod.setVodPlayUrl(vod_play_url.toString());

        SpiderDebug.log("++++++++++++厂长-detailContent" + Json.toJson(vod));

        return Result.string(vod);
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        String searchUrl = siteUrl + "/daoyongjiekoshibushiyoubing?q=";
        String html = OkHttp.string(searchUrl + key);
        if (html.contains("Just a moment")) {
            Util.notify("厂长资源需要人机验证");
        }
        Document document = Jsoup.parse(html);
        List<Vod> list = new ArrayList<>();
        getVods(list, document);

        SpiderDebug.log("++++++++++++厂长-searchContent" + Json.toJson(list));
        return Result.string(list);
    }


    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String content = OkHttp.string(id, getHeader());
        Document document = Jsoup.parse(content);

        Elements iframe = document.select("iframe");
//        System.out.println("iframe--" + iframe);
        if (!iframe.isEmpty()) {
            String videoContent = OkHttp.string(iframe.get(0).attr("src"), Util.webHeaders(siteUrl));
            String url = Util.findByRegex("const\\s+mysvg\\s+=\\s+'(.*)'", videoContent, 1);
            if (url.endsWith(".png")) {
                return Result.error("无法识别的格式 png format");
            }
            return Result.get().m3u8().url(url).string();
        } else {
            // 新增：检测 video 标签
            Elements video = document.select("video");
//            System.out.println("video--" + video);
            if (!video.isEmpty()) {
                String url = video.get(0).attr("src");
                if (url.endsWith(".png")) {
                    return Result.error("无法识别的格式 png format");
                }
                return Result.get().m3u8().url(url).string();
            } else {
//                System.out.println("使用解密模式");
                SpiderDebug.log("使用解密模式");
                String content_B = OkHttp.string(id, getHeader());

                // 动态匹配加密变量：匹配var 变量名 = "超长字符串" 模式
                // \w+ 匹配任意变量名，{200,} 确保字符串足够长（过滤短字符串变量）
                Pattern pattern = Pattern.compile("var\\s+(\\w+)\\s*=\\s*\"([^\"]{200,})\"");
                Matcher matcher = pattern.matcher(content_B);
                String encryptedJs = null;

                // 遍历所有匹配结果，找到能成功解密的变量
                while (matcher.find()) {
                    String varName = matcher.group(1);
                    String candidateData = matcher.group(2);
//                    System.out.println("尝试解密变量: " + varName);
                    SpiderDebug.log("尝试解密变量: " + varName);
                    // 使用已知密钥尝试解密
                    String key = "e883aa859cb94c81";
                    String iv = "1234567890983456";
                    String decrypted = decryptAes(candidateData, key, iv);

                    // 验证解密结果是否包含视频URL相关特征
                    if (decrypted.contains("url") || decrypted.contains("m3u8") || decrypted.contains("dncry")) {
                        encryptedJs = candidateData;
//                        System.out.println("找到有效加密变量: " + varName);
                        SpiderDebug.log("找到有效加密变量: " + varName);
                        break;
                    }
                }

                if (encryptedJs != null) {
                    String key = "e883aa859cb94c81";
                    String iv = "1234567890983456";
                    String decryptedJs = decryptAes(encryptedJs, key, iv);

                    // 修复：增强正则兼容性，支持单/双引号、空格，并扩大函数名匹配范围
                    String base64Url = Util.findByRegex("(dncry|decode|decrypt)\\s*\\(\\s*['\"]([^'\"]+)['\"]\\s*\\)", decryptedJs, 2);

                    // 备选方案：若未找到加密函数，直接搜索URL特征（如http/https开头的m3u8链接）
                    if (base64Url == null || base64Url.isEmpty()) {
//                        System.out.println("未找到dncry函数，尝试直接提取URL...");
                        SpiderDebug.log("未找到dncry函数，尝试直接提取URL...");
                        base64Url = Util.findByRegex("(https?://[^'\"]+\\.m3u8)", decryptedJs, 1);
                    }

                    if (base64Url != null && !base64Url.isEmpty()) {
                        // 若提取到的是直接URL（非base64），无需解码
                        String videoUrl;
                        if (base64Url.startsWith("http")) {
                            videoUrl = base64Url; // 直接使用URL
                        } else {
                            videoUrl = new String(Base64.getDecoder().decode(base64Url), "UTF-8"); // 解码base64
                        }
//                        System.out.println("videoUrl--" + videoUrl);
                        SpiderDebug.log("videoUrl--" + videoUrl);
                        return Result.get().m3u8().url(videoUrl).string();
                    } else {
                        return Result.error("base64Url为空");
                    }
                } else {
                    return Result.error("未找到有效加密变量");
                }
            }
        }
    }


    // AES解密方法
    private String decryptAes(String data, String key, String iv) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] ivBytes = iv.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = Base64.getDecoder().decode(data);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(ivBytes));

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    String cryptJs(String text, String key, String iv) {
        byte[] key_value = key.getBytes(StandardCharsets.UTF_8);
        byte[] iv_value = iv.getBytes(StandardCharsets.UTF_8);


        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key_value, iv_value);

        String content = new String(aes.decrypt(text), StandardCharsets.UTF_8);

        return content;
    }

}
