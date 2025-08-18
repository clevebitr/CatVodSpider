import com.github.catvod.spider.Wogg;
import com.github.catvod.utils.Json;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import common.AssertUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class WoggTest {
    private static Wogg spider;

    @BeforeAll
    public static void init() throws Exception {
        spider = new Wogg();
        //spider.init("{\"cookie\":\"ctoken=rldVUeNBAbGyhJdbpC4wEUE-;__pus=75e54cf66f9ea5ed1497838782a90a78AATTBUV9c9w7KXUiHDEl6VdV8Wxki4L9R5kIIjSKQnX1wedJe3s8weva95YKUkRqI1aBY/MA+YBNvaTO0JkXvLp+;__kp=be6b9e10-74f8-11ef-aa08-7d8956cd7603;__kps=AATcZArVgS76EPn0FMaV4HEj;__ktd=sii/iz4ePzEaoVirXul7QQ==;__uid=AATcZArVgS76EPn0FMaV4HEj\"}");
        spider.init("{}");
    }

    @Test
    public void homeContent() throws Exception {
        String content = spider.homeContent(true);
        JsonObject map = Json.safeObject(content);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        System.out.println("homeContent--" + gson.toJson(map));

        //Assert.assertFalse(map.getAsJsonArray("list").isEmpty());
    }

    @Test
    public void homeVideoContent() throws Exception {
        String content = spider.homeVideoContent();
        JsonObject map = Json.safeObject(content);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        System.out.println("homeVideoContent--" + gson.toJson(map));

        // Assert.assertFalse(map.getAsJsonArray("list").isEmpty());
    }

    @Test
    public void categoryContent() throws Exception {
        String content = spider.categoryContent("2", "2", true, null);
        JsonObject map = Json.safeObject(content);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("categoryContent--" + gson.toJson(map));
    }

    @Test
    public void detailContent() throws Exception {

        String content = spider.detailContent(Arrays.asList("/voddetail/83764.html"));
        System.out.println("detailContent--" + content);

        JsonObject map = Json.safeObject(content);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("detailContent--" + gson.toJson(map));
    }

    @Test
    public void playerContent() throws Exception {
        String content = spider.playerContent("quark4K", "3f6fa9d56fdb4b84aa3c225325089db1++d632677e166a70887d06d4f9c6dd2d75++3725fb8c10c04++aXSBQBW2/UPeQ/taA28VgmGSdKYdTC+tTz6oWA91noc", new ArrayList<>());
        System.out.println("playerContent--" + content);
        JsonObject map = Json.safeObject(content);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("playerContent--" + gson.toJson(map));
    }

    @Test
    public void searchContent() throws Exception {
        String content = spider.searchContent("红海", false);
        JsonObject map = Json.safeObject(content);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("searchContent--" + gson.toJson(map));
    }

}
