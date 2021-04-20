package com.havving.framework.components;

import com.havving.framework.annotation.Bind;
import com.havving.framework.annotation.Component;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Component 내에 Bind Annotation이 선언된 필드를 스캔하고, 각 객체간의 D/I 정의를 생성
 *
 * @author HAVVING
 * @since 2021-04-19
 */
public class ComponentDependencyResolver {
    @Getter
    private Set<DependencyIdMaps> values;


    /**
     * Bind 가능한 필드인지 확인
     *
     * @param field Component 내에 선언된 필드
     * @return t or f
     */
    public static boolean isBindableField(Field field) {
        return field.getAnnotations().length > 0 &&
                field.getDeclaredAnnotation(Bind.class) != null &&
                field.getType().isAnnotationPresent(Component.class);
    }


    /**
     *
     * @param policyFactory D/I 정책 내장 객체
     * @return 계산이 완료된 D/I 정의
     */
    public Set<DependencyIdMaps> resolve(ComponentPolicyFactory policyFactory) {
        Set<DependencyIdMaps> idMapsSet = new HashSet<>();

        policyFactory.getIds().forEach(id -> {
            BindPolicy policy = policyFactory.getPolicy(id);
            PolicyDefine define = policyFactory.getDefine(policy, id);

            // TODO
        });
        this.values = idMapsSet;

        return this.values;
    }


    /**
     * D/I된 객체들의 Component ID 정의
     */
    @Getter
    @ToString
    public static final class DependencyIdMaps {
        String id;
        Class<?> clz;
        Set<String> singleIds;
        Set<String> instantIds;

        DependencyIdMaps(String id, Class<?> clz, Set<String> singleIds, Set<String> instantIds) {
            this.id = id;
            this.clz = clz;
            this.singleIds = singleIds;
            this.instantIds = instantIds;
        }

    }
}
