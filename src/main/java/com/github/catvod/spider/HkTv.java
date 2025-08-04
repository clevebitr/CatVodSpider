package com.github.catvod.spider;

import cn.hutool.core.codec.Base64Decoder;
import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Util;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HkTv extends Spider {

    private static String siteUrl = "http://www.tvyb07.com";
    private static String cateUrl = siteUrl + "/vod/type/id/";
    private static String detailUrl = siteUrl + "/vod/detail/id/";
    private static String playUrl = siteUrl + "/vod/play/id/";
    private static String searchUrl = siteUrl + "/vod/search.html?wd=";

    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Util.CHROME);
        return headers;
    }

    @Override
    public void init(String extend) throws Exception {
        super.init(extend);
        if(extend.isBlank()) return;
        Document doc = Jsoup.parse(OkHttp.string(extend));


        if (StringUtils.isNoneBlank(doc.html())) {
            String data = doc.select("ul > li > a").first().attr("href");
            siteUrl = data;
        }
        cateUrl = siteUrl + "/vod/type/id/";
        detailUrl = siteUrl + "/vod/detail/id/";
        playUrl = siteUrl + "/vod/play/id/";
        searchUrl = siteUrl + "/vod/search.html?wd=";
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        String[] typeIdList = {"1", "2", "3", "4", "19"};
        String[] typeNameList = {"电影", "电视剧", "综艺", "动漫", "短片"};
        for (int i = 0; i < typeNameList.length; i++) {
            classes.add(new Class(typeIdList[i], typeNameList[i]));
        }
        Document doc = Jsoup.parse(OkHttp.string(siteUrl, getHeaders()));
        for (Element element : doc.select("a.myui-vodlist__thumb")) {
            try {
                String pic = element.attr("data-original");
                String url = element.attr("href");
                String name = element.attr("title");
                if (!pic.startsWith("http")) {
                    pic = siteUrl + pic;
                }
                String id = url.split("/")[4];
                list.add(new Vod(id, name, pic));
            } catch (Exception e) {
            }
        }
        return Result.string(classes, list);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        List<Vod> list = new ArrayList<>();
        String target = cateUrl + tid + ".html";
        if (!"1".equals(pg)) {
            target = cateUrl + pg + "/page/" + tid + ".html";
        }
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select("ul.myui-vodlist li a.myui-vodlist__thumb")) {
            try {
                String pic = element.attr("data-original");
                String url = element.attr("href");
                String name = element.attr("title");
                if (!pic.startsWith("http")) {
                    pic = siteUrl + pic;
                }
                String id = url.split("/")[4];
                list.add(new Vod(id, name, pic));
            } catch (Exception e) {
            }
        }

        Integer total = (Integer.parseInt(pg) + 1) * 20;
        return Result.string(Integer.parseInt(pg), Integer.parseInt(pg) + 1, 20, total, list);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        Document doc = Jsoup.parse(OkHttp.string(detailUrl.concat(ids.get(0)), getHeaders()));
        String name = doc.select("h1.title").text();
        String pic = doc.select("a.myui-vodlist__thumb.picture img").attr("data-original");
        // 播放源
        Elements tabs = doc.select("div.myui-panel__head.bottom-line.active.clearfix h3");
        Elements list = doc.select("ul.myui-content__list");
        String PlayFrom = "";
        String PlayUrl = "";
        for (int i = 1; i < tabs.size() - 1; i++) {
            String tabName = tabs.get(i).text();
            if (!"".equals(PlayFrom)) {
                PlayFrom = PlayFrom + "$$$" + tabName;
            } else {
                PlayFrom = PlayFrom + tabName;
            }
            Elements li = list.get(i - 1).select("a");
            String liUrl = "";
            for (int i1 = 0; i1 < li.size(); i1++) {
                if (!"".equals(liUrl)) {
                    liUrl = liUrl + "#" + li.get(i1).text() + "$" + li.get(i1).attr("href").replace("/vod/play/id/", "");
                } else {
                    liUrl = liUrl + li.get(i1).text() + "$" + li.get(i1).attr("href").replace("/vod/play/id/", "");
                }
            }
            if (!"".equals(PlayUrl)) {
                PlayUrl = PlayUrl + "$$$" + liUrl;
            } else {
                PlayUrl = PlayUrl + liUrl;
            }
        }

        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(siteUrl + pic);
        vod.setVodName(name);
        vod.setVodPlayFrom(PlayFrom);
        vod.setVodPlayUrl(PlayUrl);
        return Result.string(vod);
    }

    @Override
    // 需要验证码
    public String searchContent(String key, boolean quick) throws Exception {
        List<Vod> list = new ArrayList<>();
        Document doc = Jsoup.parse(OkHttp.string(searchUrl.concat(URLEncoder.encode(key)), getHeaders()));
        for (Element element : doc.select("div.searchlist_img")) {
            try {
                String pic = element.select("a").attr("data-original");
                String url = element.select("a").attr("href");
                String name = element.select("a").attr("title");
                if (!pic.startsWith("http")) {
                    pic = siteUrl + pic;
                }
                String id = url.replace("/video/", "").replace(".html", "-1-1.html");
                list.add(new Vod(id, name, pic));
            } catch (Exception e) {
            }
        }
        return Result.string(list);
    }

        @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String target = playUrl.concat(id);
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));

        // 尝试多种可能的正则表达式
        String[] regexPatterns = {
            "\"url\\\":\\\"(.*?)\\\",\\\"url_next\\\":",
            "\"play_url\\\":\\\"(.*?)\\\"",
            "\"video_url\\\":\\\"(.*?)\\\"",
            "source:\\s*\\\"(.*?)\\\"",
            "src:\\s*\\\"(.*?)\\\""
        };

        String url = null;
        for (String regex : regexPatterns) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(doc.html());
            if (matcher.find()) {
                url = matcher.group(1);
                break;
            }
        }

        if (url == null) {
            // 如果正则匹配失败，尝试从JavaScript变量中提取
            Elements scripts = doc.select("script");
            for (Element script : scripts) {
                String html = script.html();
                if (html.contains("play_url") || html.contains("video_url")) {
                    Pattern pattern = Pattern.compile("['\"](https?://[^'\"]+\\.m3u8[^'\"]*)['\"]");
                    Matcher matcher = pattern.matcher(html);
                    if (matcher.find()) {
                        url = matcher.group(1);
                        break;
                    }
                }
            }
        }

        if (url == null) {
            // 如果还是找不到，返回错误信息而不是HTML
            return Result.get().url("").string();
        }

        // 处理URL编码和解码
        if (url.startsWith("http") || url.startsWith("https")) {
            url = decodeURL(url);
        } else if (url.matches("^[a-zA-Z0-9+/=]+$")) {
            String decodedString = new String(Base64Decoder.decode(url));
            url = decodeURL(decodedString);
        }

            // 新增：检测并编码中文字符
        url = encodeChineseCharacters(url);

        return Result.get().url(url).header(getHeaders()).string();
    }

    // 新增方法：编码URL中的中文字符
    private static String encodeChineseCharacters(String url) {
        try {
            // 检查URL中是否包含中文字符
            if (url.matches(".*[\\u4e00-\\u9fa5]+.*")) {
                // 分割URL，只编码路径和查询参数部分
                java.net.URL urlObj = new java.net.URL(url);
                String protocol = urlObj.getProtocol();
                String host = urlObj.getHost();
                int port = urlObj.getPort();
                String path = urlObj.getPath();
                String query = urlObj.getQuery();

                // 编码路径部分
                String encodedPath = encodePath(path);

                // 编码查询参数部分
                String encodedQuery = encodeQuery(query);

                // 重构URL
                StringBuilder encodedUrl = new StringBuilder();
                encodedUrl.append(protocol).append("://").append(host);
                if (port != -1 && port != 80 && port != 443) {
                    encodedUrl.append(":").append(port);
                }
                encodedUrl.append(encodedPath);
                if (encodedQuery != null && !encodedQuery.isEmpty()) {
                    encodedUrl.append("?").append(encodedQuery);
                }

                return encodedUrl.toString();
            }
            return url;
        } catch (Exception e) {
            return url;
        }
    }

    // 新增方法：编码路径部分
    private static String encodePath(String path) {
        try {
            String[] segments = path.split("/");
            StringBuilder encodedPath = new StringBuilder();
            for (String segment : segments) {
                if (!segment.isEmpty()) {
                    encodedPath.append("/").append(URLEncoder.encode(segment, "UTF-8"));
                }
            }
            return encodedPath.toString();
        } catch (Exception e) {
            return path;
        }
    }


    // 新增方法：编码查询参数部分
    private static String encodeQuery(String query) {
        try {
            if (query == null || query.isEmpty()) {
                return query;
            }

            String[] params = query.split("&");
            StringBuilder encodedQuery = new StringBuilder();

            for (int i = 0; i < params.length; i++) {
                String[] keyValue = params[i].split("=", 2);
                if (keyValue.length == 2) {
                    String encodedKey = URLEncoder.encode(keyValue[0], "UTF-8");
                    String encodedValue = URLEncoder.encode(keyValue[1], "UTF-8");
                    encodedQuery.append(encodedKey).append("=").append(encodedValue);
                } else {
                    encodedQuery.append(URLEncoder.encode(params[i], "UTF-8"));
                }

                if (i < params.length - 1) {
                    encodedQuery.append("&");
                }
            }

            return encodedQuery.toString();
        } catch (Exception e) {
            return query;
        }
    }


    public static String decodeURL(String encodedURL) {
        // 处理多余的反斜杠，将连续的斜杠替换为单个斜杠，同时处理转义的反斜杠
        String processedUrl = encodedURL.replaceAll("\\\\", "");
        processedUrl = processedUrl.replaceAll("\\/", "/");

        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < processedUrl.length()) {
            if (processedUrl.charAt(index) == '\\' && index + 1 < processedUrl.length() && processedUrl.charAt(index + 1) == 'u') {
                if (index + 6 <= processedUrl.length()) {
                    String unicodeStr = processedUrl.substring(index + 2, index + 6);
                    char unicodeChar = (char) Integer.parseInt(unicodeStr, 16);
                    sb.append(unicodeChar);
                    index += 6;
                } else {
                    sb.append(processedUrl.charAt(index));
                    index++;
                }
                // 新增：处理 uXXXX 格式（不带反斜杠）
            } else if (processedUrl.charAt(index) == 'u' && index + 4 < processedUrl.length()) {
                String possibleUnicode = processedUrl.substring(index + 1, index + 5);
                if (possibleUnicode.matches("[0-9a-fA-F]{4}")) {
                    char unicodeChar = (char) Integer.parseInt(possibleUnicode, 16);
                    sb.append(unicodeChar);
                    index += 5; // 跳过 u + 4个字符
                    continue;
                }
                // 如果不是有效的Unicode编码，正常添加'u'
                sb.append(processedUrl.charAt(index));
                index++;
            } else if (processedUrl.charAt(index) == '%') {
                // 原有处理百分号编码的逻辑
                if (index + 2 < processedUrl.length()) {
                    if (processedUrl.charAt(index + 1) == 'u') {
                        String unicodeStr = processedUrl.substring(index + 2, index + 6);
                        char unicodeChar = (char) Integer.parseInt(unicodeStr, 16);
                        sb.append(unicodeChar);
                        index += 6;
                    } else {
                        String hexStr = processedUrl.substring(index + 1, index + 3);
                        char hexChar = (char) Integer.parseInt(hexStr, 16);
                        sb.append(hexChar);
                        index += 3;
                    }
                } else {
                    sb.append(processedUrl.charAt(index));
                    index++;
                }
            } else {
                sb.append(processedUrl.charAt(index));
                index++;
            }
        }

        try {
            String result = sb.toString();
            if (result.contains("%")) {
                result = java.net.URLDecoder.decode(result, "UTF-8");
            }
            // 新增规范编码处理
            result = result
                    .replace(" ", "%20")
                    .replace("{", "%7B")
                    .replace("}", "%7D")
                    .replace("|", "%7C");
            return result;
        } catch (UnsupportedEncodingException e) {
            return sb.toString();
        }
    }
}