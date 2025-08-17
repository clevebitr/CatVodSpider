package com.github.catvod.spider;


import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.utils.HttpFetcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.catvod.spider.Quark.patternQuark;

/**
 * 电影云集
 *
 * @author lushunming
 * @createdate 2024-12-03
 */
public class DianYingYunJi extends Cloud {

    private final String siteUrl = "https://dyyjpro.com";


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

    private HashMap<String, String> getHeaderWithCookie() {
        HashMap<String, String> header = new HashMap<>();
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36 Edg/138.0.0.0");
        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        header.put("Accept-Encoding", "gzip, deflate, br, zstd");
        header.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        header.put("Cache-Control", "max-age=0");
        header.put("Referer", "https://dm84.net/p/5088-1-92.html");
        header.put("Priority", "u=0, i");
        header.put("Sec-Ch-Ua", "\"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"138\", \"Microsoft Edge\";v=\"138\"");
        header.put("Sec-Ch-Ua-Mobile", "?0");
        header.put("Sec-Ch-Ua-Platform", "\"Windows\"");
        header.put("Sec-Fetch-Dest", "document");
        header.put("Sec-Fetch-Mode", "navigate");
        header.put("Sec-Fetch-Site", "none");
        header.put("Sec-Fetch-User", "?1");
        header.put("Sec-Gpc", "1");
        header.put("Upgrade-Insecure-Requests", "1");
        header.put("cookie", "esc_search_captcha=1; result=43");
        return header;
    }

    @Override
    public void init(String extend) throws Exception {

        super.init(extend);
    }

    @Override
    public String homeContent(boolean filter) throws IOException {
        List<Class> classes = new ArrayList<>();
        Document doc = Jsoup.parse(HttpFetcher.fetchAndDecompress(siteUrl, getHeaders()));
//        System.out.println(doc);
        Elements elements = doc.select(" #header-navbar > li.menu-item > a");
        for (Element e : elements) {
            String url = e.attr("href");
            String name = e.text();
            if (url.contains(siteUrl)) {
                classes.add(new Class(url, name));
            }

        }

        return Result.string(classes, parseVodListFromDoc(doc));
    }

    private List<Vod> parseVodListFromDoc(Document doc) {
        List<Vod> list = new ArrayList<>();
        Elements elements = doc.select(" article.post-item");
        for (Element e : elements) {
            String vodId = e.selectFirst("h2.entry-title > a").attr("href");
            String vodPic = e.selectFirst(" div.entry-media > a").attr("data-bg");
            if (!vodPic.startsWith("http")) {
                vodPic = siteUrl + vodPic;
            }
            String vodName = e.selectFirst("h2.entry-title > a").text();
            String vodRemarks = "";
            list.add(new Vod(vodId, vodName, vodPic, vodRemarks));
        }
        return list;
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws IOException {

        Document doc = Jsoup.parse(HttpFetcher.fetchAndDecompress(String.format("%s/page/%s", tid, pg), getHeaders()));
        List<Vod> list = parseVodListFromDoc(doc);
        int total = (Integer.parseInt(pg) + 1) * 19;
        return Result.get().vod(list).page(Integer.parseInt(pg), Integer.parseInt(pg) + 1, 19, total).string();
    }


    @Override
    public String detailContent(List<String> ids) throws Exception {
        String vodId = ids.get(0);
        Document doc = Jsoup.parse(HttpFetcher.fetchAndDecompress(vodId, getHeaders()));

        Vod item = new Vod();
        item.setVodId(vodId);
        item.setVodName(doc.selectFirst(" h1.post-title").text());
        item.setVodPic(doc.selectFirst("article.post-content  img").attr("src"));
        String html = doc.select("article.post-content > p").text();
        item.setVodDirector(getStrByRegex(Pattern.compile("导演:(.*?)编剧:"), html));
        item.setVodArea(getStrByRegex(Pattern.compile("地区:(.*?)语言:"), html));
        item.setVodActor(getStrByRegex(Pattern.compile("主演:(.*?)类型:"), html));
        item.setVodYear(getStrByRegex(Pattern.compile("上映日期:(.*?)片长:"), html));
        item.setVodRemarks("");
        item.setVodContent(getStrByRegex(Pattern.compile("剧情简介(.*?)获奖情况"), html));

        List<String> shareLinks = new ArrayList<>();

        for (Element element : doc.select("article.post-content p a")) {
            if (element.attr("href").matches(patternQuark)) {
                shareLinks.add(element.attr("href").trim());
            }
        }

        item.setTypeName(doc.selectFirst(" span.meta-cat-dot").text());

        item.setVodPlayUrl(super.detailContentVodPlayUrl(shareLinks));
        item.setVodPlayFrom(super.detailContentVodPlayFrom(shareLinks));

        return Result.string(item);
    }

    private String getStrByRegex(Pattern pattern, String str) {
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) return matcher.group(1).trim();
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return searchContent(key, "1");
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        return searchContent(key, pg);
    }

    private String searchContent(String key, String pg) throws IOException {
        String searchURL = siteUrl + String.format("?cat=&s=%s", URLEncoder.encode(key));
        String html = HttpFetcher.fetchAndDecompress(searchURL, getHeaderWithCookie());
        Document doc = Jsoup.parse(html);

        return Result.string(parseVodListFromDoc(doc));
    }
}