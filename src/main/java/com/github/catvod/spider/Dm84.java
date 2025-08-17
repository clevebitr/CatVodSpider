package com.github.catvod.spider;

import cn.hutool.core.codec.Base64;
import com.github.catvod.bean.Class;
import com.github.catvod.bean.Filter;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.Util;

import com.github.luben.zstd.ZstdException;
import com.github.luben.zstd.ZstdInputStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.github.luben.zstd.Zstd;

/**
 * @author FongMi
 */
public class Dm84 extends Spider {

    private static final String siteUrl = Util.base64Decode("aHR0cHM6Ly9kbTg0Lm5ldC8=");
    private static final String playUrl = Util.base64Decode("aHR0cHM6Ly9oaGp4LmhocGxheWVyLmNvbQ==");

    //private static final Map<String, String> decodeMapping = Json.parseSafe(Util.base64Decode("eyAiME9vMG8wTzAiOiAiYSIsICIxTzBiTzAwMSI6ICJiIiwgIjJPb0NjTzIiOiAiYyIsICIzTzBkTzBPMyI6ICJkIiwgIjRPb0VlTzQiOiAiZSIsICI1TzBmTzBPNSI6ICJmIiwgIjZPb0dnTzYiOiAiZyIsICI3TzBoTzBPNyI6ICJoIiwgIjhPb0lpTzgiOiAiaSIsICI5TzBqTzBPOSI6ICJqIiwgIjBPb0trTzAiOiAiayIsICIxTzBsTzBPMSI6ICJsIiwgIjJPb01tTzIiOiAibSIsICIzTzBuTzBPMyI6ICJuIiwgIjRPb09vTzQiOiAibyIsICI1TzBwTzBPNSI6ICJwIiwgIjZPb1FxTzYiOiAicSIsICI3TzByTzBPNyI6ICJyIiwgIjhPb1NzTzgiOiAicyIsICI5TzB0TzBPOSI6ICJ0IiwgIjBPb1V1TzAiOiAidSIsICIxTzB2TzBPMSI6ICJ2IiwgIjJPb1d3TzIiOiAidyIsICIzTzB4TzBPMyI6ICJ4IiwgIjRPb1l5TzQiOiAieSIsICI1TzB6TzBPNSI6ICJ6IiwgIjBPb0FBTzAiOiAiQSIsICIxTzBCQk8xIjogIkIiLCAiMk9vQ0NPMiI6ICJDIiwgIjNPMERETzMiOiAiRCIsICI0T29FRU80IjogIkUiLCAiNU8wRkZPNSI6ICJGIiwgIjZPb0dHTzYiOiAiRyIsICI3TzBISE83IjogIkgiLCAiOE9vSUlPOCI6ICJJIiwgIjlPMEpKTzkiOiAiSiIsICIwT29LS08wIjogIksiLCAiMU8wTExPMSI6ICJMIiwgIjJPb01NTzIiOiAiTSIsICIzTzBOTk8zIjogIk4iLCAiNE9vT09PNCI6ICJPIiwgIjVPMFBQTzUiOiAiUCIsICI2T29RUU82IjogIlEiLCAiN08wUlJPNyI6ICJSIiwgIjhPb1NTTzgiOiAiUyIsICI5TzBUVE85IjogIlQiLCAiME9vVU8wIjogIlUiLCAiMU8wVlZPMSI6ICJWIiwgIjJPb1dXTzIiOiAiVyIsICIzTzBYWE8zIjogIlgiLCAiNE9vWVlPNCI6ICJZIiwgIjVPMFpaTzUiOiAiWiJ9"), Map.class);

    private static final Map<String, String> decodeMapping = new HashMap<String, String>() {{
        // 小写字母映射
        put("0Oo0o0Oo", "a");
        put("1O0bO001", "b");
        put("2OoCcO2", "c");
        put("3O0dO0O3", "d");
        put("4OoEeO4", "e");
        put("5O0fO0O5", "f");
        put("6OoGgO6", "g");
        put("7O0hO0O7", "h");
        put("8OoIiO8", "i");
        put("9O0jO0O9", "j");
        put("0OoKkO0", "k");
        put("1O0lO0O1", "l");
        put("2OoMmO2", "m");
        put("3O0nO0O3", "n");
        put("4OoOoO4", "o");
        put("5O0pO0O5", "p");
        put("6OoQqO6", "q");
        put("7O0rO0O7", "r");
        put("8OoSsO8", "s");
        put("9O0tOoO9", "t");
        put("0OoUuO0", "u");
        put("1O0vO0O1", "v");
        put("2OoWwO2", "w");
        put("3O0xO0O3", "x");
        put("4OoYyO4", "y");
        put("5O0zO0O5", "z");

        // 大写字母映射
        put("0OoAAO0", "A");
        put("1O0BBO1", "B");
        put("2OoCCO2", "C");
        put("3O0DDO3", "D");
        put("4OoEEO4", "E");
        put("5O0FFO5", "F");
        put("6OoGGO6", "G");
        put("7O0HHO7", "H");
        put("8OoIIO8", "I");
        put("9O0JJO9", "J");
        put("0OoKKO0", "K");
        put("1O0LLO1", "L");
        put("2OoMMO2", "M");
        put("3O0NNO3", "N");
        put("4OoOOO4", "O");
        put("5O0PPO5", "P");
        put("6OoQQO6", "Q");
        put("7O0RRO7", "R");
        put("8OoSSO8", "S");
        put("9O0TTO9", "T");
        put("0OoUO0", "U");
        put("1O0VVO1", "V");
        put("2OoWWO2", "W");
        put("3O0XXO3", "X");
        put("4OoYYO4", "Y");
        put("5O0ZZO5", "Z");
    }};

    private static final OkHttpClient client = new OkHttpClient();

    public void TestDM84HTML() {
        try {
            String html = fetchAndDecompress(siteUrl, getHeaders());
            System.out.println(html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String fetchAndDecompress(String url, HashMap<String, String> headers) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            ResponseBody body = response.body();
            if (body == null) throw new IOException("Empty response body");

            String contentEncoding = response.header("Content-Encoding", "").trim().toLowerCase();
            byte[] rawBytes = body.bytes();

//             调试：保存原始数据到文件以便检查
            Files.write(Paths.get("raw_response.bin"), rawBytes);
            SpiderDebug.log("Content-Encoding: " + contentEncoding);
            SpiderDebug.log("Raw data size: " + rawBytes.length + " bytes");

            byte[] decompressed;
            if ("zstd".equals(contentEncoding)) {
                try {
                    // 添加更详细的调试信息
                    long decompressedSize = Zstd.decompressedSize(rawBytes);
                    SpiderDebug.log("Estimated decompressed size: " + decompressedSize);

                    decompressed = decompressZstd(rawBytes);
                    SpiderDebug.log("Successfully decompressed Zstd data");
                } catch (Exception e) {
                    throw new IOException("Failed to decompress Zstd data", e);
                }
            } else if ("gzip".equals(contentEncoding)) {
                decompressed = Util.decompressGzip(rawBytes);
            } else {
                decompressed = rawBytes;
            }

            return new String(decompressed, StandardCharsets.UTF_8);
        }
    }

    private byte[] decompressZstd(byte[] compressedData) throws IOException {
        try {
            // 方法1：尝试标准解压（适用于完整帧数据）
            try {
                return Zstd.decompress(compressedData);
            } catch (ZstdException e) {
                // 方法2：手动缓冲解压（处理未知内容大小）
                return decompressWithDynamicBuffer(compressedData);
            }
        } catch (Exception e) {
            throw new IOException("Zstd decompression failed", e);
        }
    }

    private byte[] decompressWithDynamicBuffer(byte[] compressedData) throws IOException {
        // 初始缓冲区大小（根据压缩数据大小估算）
        int bufferSize = Math.max(compressedData.length * 3, 1024 * 1024); // 至少1MB
        byte[] buffer = new byte[bufferSize];

        // 使用流式API解压
        try (ZstdInputStream zis = new ZstdInputStream(new ByteArrayInputStream(compressedData))) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] tempBuffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = zis.read(tempBuffer)) != -1) {
                output.write(tempBuffer, 0, bytesRead);
            }

            return output.toByteArray();
        } catch (Exception e) {
            // 方法3：回退到带大小提示的解压
            return decompressWithSizeHint(compressedData, bufferSize * 2);
        }
    }

    private byte[] decompressWithSizeHint(byte[] compressedData, int suggestedSize) {
        byte[] buffer = new byte[suggestedSize];
        long resultSize = Zstd.decompressByteArray(
                buffer, 0, buffer.length,
                compressedData, 0, compressedData.length
        );

        if (Zstd.isError(resultSize)) {
            throw new RuntimeException("Zstd error: " + Zstd.getErrorName(resultSize));
        }

        return Arrays.copyOf(buffer, (int) resultSize);
    }


    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();

        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36 Edg/138.0.0.0");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.put("Accept-Encoding", "gzip, deflate, br, zstd");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        headers.put("Cache-Control", "max-age=0");
        headers.put("Referer", "https://dm84.net/p/5088-1-92.html");
        headers.put("Priority", "u=0, i");
        headers.put("Sec-Ch-Ua", "\"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"138\", \"Microsoft Edge\";v=\"138\"");
        headers.put("Sec-Ch-Ua-Mobile", "?0");
        headers.put("Sec-Ch-Ua-Platform", "\"Windows\"");
        headers.put("Sec-Fetch-Dest", "document");
        headers.put("Sec-Fetch-Mode", "navigate");
        headers.put("Sec-Fetch-Site", "none");
        headers.put("Sec-Fetch-User", "?1");
        headers.put("Sec-Gpc", "1");
        headers.put("Upgrade-Insecure-Requests", "1");

        // 如果你有 Cookie，也可以加上
//         headers.put("Cookie","_ga=GA1.1.1429907492.1753201288; notice_show=1; history=%5B%7B%22name%22%3A%22%u6C5F%u601D%u5148%u751F%22%2C%22pic%22%3A%22%22%2C%22link%22%3A%22/p/5088-1-92.html%22%2C%22part%22%3A%22%u7B2C92%u96C6%22%7D%5D; _ga_2JQYJX8CK4=GS2.1.s1753240824$o2$g0$t1753240824$j60$l0$h0");

        return headers;
    }

    private Filter getFilter(String name, String key, List<String> texts) {
        List<Filter.Value> values = new ArrayList<>();
        for (String text : texts) {
            if (text.isEmpty()) continue;
            String n = text.replace("按", "");
            String v = key.equals("by") ? replaceBy(text) : text;
            values.add(new Filter.Value(n, v));
        }
        return new Filter(key, name, values);
    }

    private String replaceBy(String text) {
        return text.replace("按时间", "time").replace("按人气", "hits").replace("按评分", "score");
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        LinkedHashMap<String, List<Filter>> filters = new LinkedHashMap<>();

        // 使用 fetchAndDecompress 获取首页内容
        String html = fetchAndDecompress(siteUrl, getHeaders());
        Document doc = Jsoup.parse(html);
        //System.out.println(doc);
        // 解析分类（兼容多位数ID和完整分类名）
        for (Element element : doc.select(".nav_row > li > a[href^=/list]")) {
            String href = element.attr("href");
            // 提取ID（兼容/list-1.html和/list-10.html等格式）
            String id = href.replaceAll(".*list-(\\d+).html", "$1");

            // 使用完整分类名（避免截断）
            String name = element.text().trim();

            // 跳过空数据
            if (!id.isEmpty() && !name.isEmpty()) {
                classes.add(new Class(id, name));
            }
        }

        // 解析每个分类的筛选条件
        for (Class item : classes) {
            String listUrl = siteUrl + "/list-" + item.getTypeId() + ".html";
            String listHtml = fetchAndDecompress(listUrl, getHeaders());
            Document listDoc = Jsoup.parse(listHtml);

            Elements filterElements = listDoc.select("ul.list_filter > li > div");
            List<Filter> filterArray = new ArrayList<>();

            if (filterElements.size() >= 3) {
                filterArray.add(getFilter("類型", "type", filterElements.get(0).select("a").eachText()));
                filterArray.add(getFilter("時間", "year", filterElements.get(1).select("a").eachText()));
                filterArray.add(getFilter("排序", "by", filterElements.get(2).select("a").eachText()));
                filters.put(item.getTypeId(), filterArray);
            }
        }

        // 解析视频列表（修正选择器和健壮性处理）
        for (Element element : doc.select("ul.v_list.f6t8 div.item")) {
            // 封面图（优先取data-bg，其次取img的src）
            String img = element.select("a.cover").attr("data-bg");
            if (img.isEmpty()) {
                img = element.select("img.cover").attr("src");
            }

            // 视频链接和ID
            String url = element.select("a.title").attr("href");
            String id = url.replaceAll(".*/(\\d+)\\.html", "$1"); // 更安全的ID提取

            // 标题和更新状态
            String name = element.select("a.title").text();
            String remark = element.select("span.desc").text();

            // 空值检查
            if (!id.isEmpty() && !name.isEmpty()) {
                list.add(new Vod(id, name, img.isEmpty() ? "" : img, remark));
            }
        }

        return Result.string(classes, list, filters);
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        List<Vod> list = new ArrayList<>();

        // 1. 处理筛选参数
        extend.putIfAbsent("type", "");
        extend.putIfAbsent("year", "");
        extend.putIfAbsent("by", "time");

        String type = URLEncoder.encode(extend.get("type"));
        String target = siteUrl + String.format("/show-%s--%s-%s--%s-%s.html",
                tid,
                extend.get("by"),
                type,
                extend.get("year"),
                pg);

        try {
            // 2. 使用安全的fetchAndDecompress获取数据
            String html = fetchAndDecompress(target, getHeaders());
            Document doc = Jsoup.parse(html);

            // 3. 优化后的视频列表解析
            for (Element element : doc.select("div.item")) {
                try {
                    String img = element.select("a.cover").attr("data-bg");
                    if (img.isEmpty()) {
                        img = element.select("img.cover").attr("src"); // 备用封面图获取
                    }

                    String url = element.select("a.title").attr("href");
                    String id = url.replaceAll(".*/(\\d+)\\.html", "$1"); // 更安全的ID提取
                    String name = element.select("a.title").text();
                    String remark = element.select("span.desc").text();

                    if (!id.isEmpty() && !name.isEmpty()) {
                        list.add(new Vod(id, name, img, remark));
                    }
                } catch (Exception e) {
                    SpiderDebug.log("解析视频项失败: " + element);
                }
            }
        } catch (Exception e) {
            SpiderDebug.log("获取分类内容失败: " + target);
            e.printStackTrace();
            return Result.error("获取数据失败，请检查网络或参数");
        }

        return Result.string(list);
    }

    @Override
    public String detailContent(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("视频ID不能为空");
        }

        Vod vod = new Vod();
        vod.setVodId(ids.get(0));

        try {
            // 1. 使用安全的fetchAndDecompress获取详情页
            String url = siteUrl.concat("/v/").concat(ids.get(0));
            String html = fetchAndDecompress(url, getHeaders());
            Document doc = Jsoup.parse(html);

            // 2. 基础信息提取（带默认值处理）
            vod.setVodName(doc.selectFirst("h1.v_title").text());
            vod.setVodRemarks(doc.selectFirst("p.v_desc > span.desc").text());
            vod.setVodPic(getMetaContent(doc, "og:image"));
            vod.setVodArea(getMetaContent(doc, "og:video:area"));
            vod.setTypeName(getMetaContent(doc, "og:video:class"));
            vod.setVodActor(getMetaContent(doc, "og:video:actor"));
            vod.setVodContent(getMetaContent(doc, "og:description"));
            vod.setVodYear(getMetaContent(doc, "og:video:release_date"));
            vod.setVodDirector(getMetaContent(doc, "og:video:director"));

            // 3. 播放源解析（优化版）
            parsePlaySources(doc, vod);

        } catch (IOException e) {
            SpiderDebug.log("获取详情页失败: " + e.getMessage());
            return Result.error("视频详情获取失败");
        } catch (Exception e) {
            SpiderDebug.log("解析详情页异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("视频解析异常");
        }

        return Result.string(vod);
    }

    // 辅助方法：安全获取meta标签内容
    private String getMetaContent(Document doc, String property) {
        Element meta = doc.selectFirst("meta[property=" + property + "], meta[name=" + property + "]");
        return meta != null ? meta.attr("content") : "";
    }

    // 辅助方法：解析播放源
    private void parsePlaySources(Document doc, Vod vod) {
        Map<String, String> sites = new LinkedHashMap<>();

        Elements tabControls = doc.select("ul.tab_control > li");
        Elements playLists = doc.select("ul.play_list");

        if (tabControls.size() != playLists.size()) {
            SpiderDebug.log("播放源标签与列表数量不匹配");
            return;
        }

        for (int i = 0; i < tabControls.size(); i++) {
            String sourceName = tabControls.get(i).text();
            List<String> episodes = playLists.get(i).select("a").stream()
                    .map(e -> e.text() + "$" + e.attr("href"))
                    .collect(Collectors.toList());

            if (!episodes.isEmpty()) {
                sites.put(sourceName, String.join("#", episodes));
            }
        }

        if (!sites.isEmpty()) {
            vod.setVodPlayFrom(String.join("$$$", sites.keySet()));
            vod.setVodPlayUrl(String.join("$$$", sites.values()));
        }
    }

    @Override
    public String searchContent(String key, boolean quick) throws IOException {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl.concat("/s----------.html?wd=").concat(key);
        Document doc = Jsoup.parse(fetchAndDecompress(target, getHeaders()));
        for (Element element : doc.select("div.item")) {
            String img = element.select("a.cover").attr("data-bg");
            String url = element.select("a.title").attr("href");
            String name = element.select("a.title").text();
            String remark = element.select("span.desc").text();
            String id = url.split("/")[2];
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            // 1. 使用 fetchAndDecompress 获取初始页面
            Document doc = Jsoup.parse(fetchAndDecompress(siteUrl.concat(id), getHeaders()));
            String urlLink = doc.select("iframe").attr("src");
            // 2. 使用 fetchAndDecompress 获取播放器配置
            String string = fetchAndDecompress(urlLink, getHeaders());
            // 3. 保持原有参数提取逻辑
            String url = Util.findByRegex(getvarRegx("url"), string, 1);
            String t = Util.findByRegex(getvarRegx("t"), string, 1);
            String act = Util.findByRegex(getvarRegx("act"), string, 1);
            String play = Util.findByRegex(getvarRegx("play"), string, 1);
            String key = Util.findByRegex("var\\s+key\\s*=\\s*hhh\\(\"([^\"]+)\"\\)", string, 1);
            String decodeString = decodeString(Base64.decodeStr(key));
            // 4. 保持原有参数组装
            HashMap<String, String> map = new HashMap<>();
            map.put("url", url);
            map.put("t", t);
            map.put("act", act);
            map.put("play", play);
            map.put("key", decodeString);
            // 1. 构建完整的请求头（直接从浏览器复制）
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.put("Accept-Encoding", "gzip, deflate, br, zstd");
            headers.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            headers.put("DNT", "1");
            headers.put("Origin", "https://hhjx.hhplayer.com");
            headers.put("Priority", "u=1, i");
            headers.put("Referer", urlLink); // 使用动态获取的iframe地址
            headers.put("Sec-Ch-Ua", "\"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"138\", \"Microsoft Edge\";v=\"138\"");
            headers.put("Sec-Ch-Ua-Mobile", "?0");
            headers.put("Sec-Ch-Ua-Platform", "\"Windows\"");
            headers.put("Sec-Fetch-Dest", "empty");
            headers.put("Sec-Fetch-Mode", "cors");
            headers.put("Sec-Fetch-Site", "same-origin");
            headers.put("Sec-Fetch-Storage-Access", "active");
            headers.put("Sec-Gpc", "1");
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36 Edg/138.0.0.0");
            headers.put("X-Requested-With", "XMLHttpRequest");
            // 在发送 POST 请求前添加延迟（单位：毫秒）
            try {
                Thread.sleep(2000); // 等待 2 秒
            } catch (InterruptedException e) {
                SpiderDebug.log("延迟被中断: " + e.getMessage());
            }
            // 5. 发送 API 请求并严格验证响应
            try {
                String post = OkHttp.post(playUrl.concat("/api.php"), map, headers).getBody();
                JsonElement parse = Json.parse(post);
                // 验证 1: 确保是 JsonObject
                if (!parse.isJsonObject()) {
                    SpiderDebug.log("dm84 响应格式异常（非JSON对象）: " + post);
                    return Result.error("服务器返回数据格式无效");
                }
                JsonObject json = parse.getAsJsonObject();
                // 验证 2: 检查必填字段是否存在
                if (!json.has("code") || !json.has("url")) {
                    SpiderDebug.log("dm84 响应缺少必要字段: " + post);
                    return Result.error("服务器数据不完整");
                }
                // 验证 3: 检查状态码
                int code = json.get("code").getAsInt();
                if (code != 200) {
                    String errorMsg = json.has("msg") ? json.get("msg").getAsString() : "未知错误";
                    SpiderDebug.log("dm84 请求失败: " + code + " - " + errorMsg);
                    return Result.error(errorMsg);
                }
                // 验证 4: 智能处理 URL（兼容完整地址和相对路径）
                String rawUrl = json.get("url").getAsString();
                String finalUrl;

                if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                    // 情况1：已经是完整地址，直接使用
                    finalUrl = rawUrl;
                } else if (rawUrl.startsWith("//")) {
                    // 情况2：无协议 URL（如 //example.com/video.m3u8），默认补 https:
                    finalUrl = "https:" + rawUrl;
                } else if (rawUrl.startsWith("/")) {
                    // 情况3：绝对路径（如 /cache/video.m3u8），拼接基准域名
                    // 从请求头或 iframe 的 src 中提取域名
                    String referer = urlLink; // 即之前获取的 iframe.attr("src")
                    String baseUrl = new URL(referer).getProtocol() + "://" + new URL(referer).getHost();
                    finalUrl = baseUrl + json.get("url").getAsString();
                } else {
                    // 情况4：相对路径（如 ../videos/123.m3u8），需结合当前页面的 URL
                    String baseUrl = urlLink.substring(0, urlLink.lastIndexOf('/') + 1);
                    finalUrl = baseUrl + rawUrl;
                }
                // 可选：标准化 URL（移除多余的 / 或 ./）
                finalUrl = finalUrl.replaceAll("(?<!:)/+", "/")  // 替换多个 / 为单个
                        .replaceAll("/\\./", "/");     // 替换 /./ 为 /
                // 示例调试日志
                SpiderDebug.log("原始URL: " + rawUrl);
                SpiderDebug.log("最终URL: " + finalUrl);
                return Result.get().url(finalUrl).parse().string(); // 失败时回退
            } catch (Exception e) {
                SpiderDebug.log("dm84 解析响应异常: " + e.getMessage());
                return Result.error("解析数据失败: " + e.getMessage());
            }

        } catch (IOException e) {
            SpiderDebug.log("播放请求异常: " + e.getMessage());
            return Result.error("请求失败: " + e.getMessage());
        }
    }

    private String getvarRegx(String var) {
        return String.format("var\\s*%s\\s*=\\s*[\\n\"]*(.*)[\"]+", var);
    }

    public String decodeString(String decodedString) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < decodedString.length(); i++) {
            String l = String.valueOf(decodedString.charAt(i));

            for (Map.Entry<String, String> entry : decodeMapping.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (i + key.length() <= decodedString.length() &&
                        decodedString.substring(i, i + key.length()).equals(key)) {
                    l = value;
                    i += key.length() - 1;
                    break;
                }
            }
            result.append(l);
        }
        return result.toString();
    }
}
