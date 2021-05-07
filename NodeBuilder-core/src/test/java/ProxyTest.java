import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HAVVING
 * @since 2021-04-26
 */
public class ProxyTest {

    public static class Sample {
        public void run() {
            System.out.println("RUN!!!");
        }
    }

    @Test
    public void castingTest() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Sample.class);
        enhancer.setCallback(NoOp.INSTANCE);

        Object s = enhancer.create();
        System.out.println(s);
    }

    @Test
    public void test() {
        List<String> array = new ArrayList<>();
        array.add("Hello");
        array.add("Proxy");
        array.add("World!!!");
        System.out.println(">>>Create a interface proxy.");

        List<String> proxyArray = (List<String>) Enhancer.create(List.class, new MyInvocationHandler(array));
        for (int i = 0; i < 4; i++) {
            System.out.println(proxyArray.get(i));
        }
        System.out.println(">>>Create a class proxy.");

        proxyArray = (List<String>) Enhancer.create(ArrayList.class, new MyInvocationHandler(array));
        for (int i = 0; i < 4; i++) {
            System.out.println(proxyArray.get(i));
        }

    }


    static class MyInvocationHandler implements MethodInterceptor {
        private List<String> array;

        public MyInvocationHandler(List<String> array) {
            this.array = array;
        }

        /**
         * @param o            "this", the enhanced object
         * @param method       intercepted Method
         * @param objects      argument array; primitive types are wrapped
         * @param methodProxy  used to invoke super (non-intercepted method); may be called
         *                     as many times as needed
         */
        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            if (isFourthGet(method, objects)) {
                return "<<Bow>>";
            }
            return methodProxy.invoke(array, objects);
        }

        private boolean isFourthGet(Method method, Object[] args) {
            return "get".equals(method.getName()) && ((Integer) args[0]) == 3;
        }
    }
}
