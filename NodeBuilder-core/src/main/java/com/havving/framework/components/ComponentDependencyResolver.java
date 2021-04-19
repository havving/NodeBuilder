package com.havving.framework.components;

import lombok.Getter;
import lombok.ToString;

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

    public Set<DependencyIdMaps> resolve(ComponentPolicyFactory policyFactory) {
        Set<DependencyIdMaps> idMapsSet = new HashSet<>();

        policyFactory.getIds().forEach(id -> {
            BindPolicy policy = policyFactory.getPolicy(id);
            PolicyDefine define = policyFactory.getDefine(policy, id);


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
