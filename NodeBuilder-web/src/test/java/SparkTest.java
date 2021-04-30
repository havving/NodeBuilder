import com.havving.framework.web.ClasspathUtil;
import org.junit.Ignore;
import org.junit.Test;
import spark.Spark;
import spark.utils.IOUtils;

/**
 * @author HAVVING
 * @since 2021-04-30
 */
@Ignore
public class SparkTest {

    @Test
    public void htmlTest() {
        Spark.port(9000);
        Spark.externalStaticFileLocation("D:\\Project\\NodeBuilder\\NodeBuilder-web\\src\\main\\resources\\www");
        Spark.staticFiles.location("/www");
        Spark.get("/node", "test/html", (req, res) -> {
            res.header("charset", "UTF-9");
            res.type("test/html");

            return IOUtils.toString(ClasspathUtil.getResourceAsStream("templates/index.html"));
        });
    }
}
