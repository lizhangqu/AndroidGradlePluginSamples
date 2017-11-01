package io.github.lizhangqu.plugin

import org.gradle.api.NamedDomainObjectFactory
import org.gradle.internal.reflect.Instantiator

class SampleConfigFactory implements NamedDomainObjectFactory<SampleConfig> {
    Instantiator instantiator

    SampleConfigFactory(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    SampleConfig create(String name) {
        return instantiator.newInstance(SampleConfig.class, name)
    }
}
