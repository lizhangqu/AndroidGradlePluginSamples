package io.github.lizhangqu.plugin

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

class SampleExtension {
    int compileSdkVersion
    String buildToolsVersion
    final SampleConfig defaultSampleConfig
    final NamedDomainObjectContainer<SampleConfig> sampleConfig

    SampleExtension(Project project,
                    Instantiator instantiator) {
        this.defaultSampleConfig = instantiator.newInstance(SampleConfig.class, "main")
        this.sampleConfig = project.container(SampleConfig.class, new SampleConfigFactory(instantiator))
    }

    void compileSdkVersion(int compileSdkVersion) {
        this.compileSdkVersion = compileSdkVersion
    }

    void buildToolsVersion(String buildToolsVersion) {
        this.buildToolsVersion = buildToolsVersion
    }

    void defaultSampleConfig(Action<SampleConfig> action) {
        action.execute(defaultSampleConfig)
    }

    void sampleConfig(Action<? super NamedDomainObjectContainer<SampleConfig>> action) {
        action.execute(sampleConfig)
    }
}
