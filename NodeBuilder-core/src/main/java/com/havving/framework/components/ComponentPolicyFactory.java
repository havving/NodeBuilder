package com.havving.framework.components;

import com.havving.framework.annotation.Component;
import com.havving.framework.exception.UnMatchedInjectionDefine;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Component의 Life-Cycle 정책을 담는 객체
 *
 * @author HAVVING
 * @since 2021-04-19
 */
public class ComponentPolicyFactory extends EnumMap<BindPolicy, Set<PolicyDefine>> {
    private final Set<String> ids;

    ComponentPolicyFactory() {
        super(BindPolicy.class);
        super.put(BindPolicy.Singleton, new HashSet<>());
        super.put(BindPolicy.Instant, new HashSet<>());
        this.ids = new HashSet<>();
    }

    public void put(Class<?> clz, PolicyDefine policyDefine) {
        if (clz.getAnnotation(Component.class).singleton()) {
            get(BindPolicy.Singleton).add(policyDefine);
        } else {
            get(BindPolicy.Instant).add(policyDefine);
        }
        this.ids.add(policyDefine.getObjectName());
    }

    public Set<String> getIds() {
        return ids;
    }

    public BindPolicy getPolicy(String id) {
        Set<PolicyDefine> instants = super.get(BindPolicy.Instant);
        long inst = instants.stream().filter(e -> e.getObjectName().equals(id)).count() > 0 ? 1 : 0;
        if (inst == 1) return BindPolicy.Instant;

        Set<PolicyDefine> singles = super.get(BindPolicy.Singleton);
        inst = singles.stream().filter(e -> e.getObjectName().equals(id)).count() > 0 ? 1 : 0;
        if (inst == 0) throw new UnMatchedInjectionDefine(id + " is not present in policy defines.");

        return BindPolicy.Singleton;
    }

    public PolicyDefine getDefine(BindPolicy policy, String id) {
        Set<PolicyDefine> defines = get(policy);

        return defines.stream().filter(e -> e.getObjectName().equals(id)).limit(1).iterator().next();
    }
}
