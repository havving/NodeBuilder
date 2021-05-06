import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Getter;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * @author HAVVING
 * @since 2021-04-20
 */
public class ReflectionTest {

    @Test
    @Ignore
    public void test() {
        Class<?> clazz = Article.class;  // Article의 Class를 가져온다.
        Field[] fields = clazz.getDeclaredFields(); // Article의 모든 필드를 가져온다.
        Method[] methods = clazz.getDeclaredMethods();  // Article의 모든 메서드를 가져온다.

        for (final Field field : fields) { // field의 type, name을 출력한다.
            System.out.printf("type: %s   name: %s\n", field.getType(), field.getName());
        }

        for (final Method method : methods) {
            System.out.println(method.toString());
        }
    }


    @Test
    @Ignore
    public void test2() {
        Class clazz = null;  // Article의 Class를 가져온다.
        try {
//            clazz = Class.forName("com.havving.framework.config.NodeConfig");
            clazz = Class.forName("NodeConfig");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = clazz.getDeclaredFields(); // Article의 모든 필드를 가져온다.

        for (final Field field : fields) { // field의 type, name을 출력한다.
            System.out.printf("type: %s   name: %s\n", field.getType(), field.getName());
        }
    }


    @Getter
    class Article {
        private int id;
        private String title;
        private LocalDateTime date;
    }


    @Test
    @Ignore
    public void createJson() {
        Gson gson = new Gson();
        JsonObject object = new JsonObject();
        object.addProperty("name", "Jin");
        object.addProperty("department", "Google");
        object.addProperty("employeeNumber", 220);

        String json = gson.toJson(object);
        System.out.println(json);
    }
}
