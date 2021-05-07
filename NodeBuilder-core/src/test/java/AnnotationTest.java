import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author HAVVING
 * @since 2021-04-26
 */
public class AnnotationTest {

    @Sub
    private String subField;

    @Target({FIELD, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    public @interface Super {
        String name() default "super";
    }

    @Target(FIELD)
    @Retention(RUNTIME)
    @Super(name = "subSuper")
    public @interface Sub {
        String subName() default "subName";
    }

    @Test
    public void getSuperAnnotationsTest() throws Exception {
        Field field = AnnotationTest.class.getDeclaredField("subField");
        Sub sub = field.getDeclaredAnnotation(Sub.class);
        if (sub.annotationType().isAnnotationPresent(Super.class)) {
            Super su = sub.annotationType().getDeclaredAnnotation(Super.class);
            System.out.println(su.name());
        }
    }
}
