package com.havving.framework.web;

import com.google.gson.TypeAdapter;
import lombok.Data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Rest 정의를 담는 객체
 *
 * @author HAVVING
 * @since 2021-04-28
 */
@Data
public class RestDefine implements Externalizable {
    private static final long serialVersionUID = -7377747480475401654L;

    private String componentId;
    private String methodName;
    private String url;
    private HttpProvider.HttpMethod accept;
    private MethodArgs[] methodArgs;
    private String contentType;
    private String returnType;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(componentId);
        out.writeObject(methodName);
        out.writeObject(url);
        out.writeObject(accept);
        out.writeObject(methodArgs);
        out.writeObject(contentType);
        out.writeObject(returnType);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.componentId = (String) in.readObject();
        this.methodName = (String) in.readObject();
        this.url = (String) in.readObject();
        this.accept = (HttpProvider.HttpMethod) in.readObject();
        this.methodArgs = (MethodArgs[]) in.readObject();
        this.contentType = (String) in.readObject();
        this.returnType = (String) in.readObject();
    }


    /**
     * !!warning
     * type으로 primitive type이 아닌 경우, TypeAdapter를 등록할 경우에만 RestBinder의 argument로 사용했을 때 에러가 발생하지 않음
     */
    @Data
    public static class MethodArgs implements Externalizable {
        private static final long serialVersionUID = -5125832599981932774L;

        private String name;
        private String adapterName;
        private String[] genericTypeNames;
        private transient Class<?> type;
        private transient TypeAdapter<?> typeAdapter;

        public MethodArgs(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }

        public MethodArgs(String name, Class<?> type, Type[] genericTypes) {
            this.name = name;
            this.type = type;
            this.genericTypeNames = Stream.of(genericTypes)
                    .map(Type::getTypeName)
                    .collect(Collectors.toList())
                    .toArray(new String[0]);
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(name);
            out.writeObject(adapterName);
            out.writeObject(genericTypeNames);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            this.name = (String) in.readObject();
            this.genericTypeNames = (String[]) in.readObject();
            this.adapterName = (String) in.readObject();
            if (adapterName != null)
                try {
                    this.typeAdapter = (TypeAdapter<?>) Class.forName(this.adapterName).newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
        }
    }
}
