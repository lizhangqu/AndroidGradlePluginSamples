package io.github.lizhangqu.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.SdkHandler
import com.android.build.gradle.internal.TaskManager
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.api.LibraryVariantImpl
import com.android.build.gradle.internal.ndk.NdkHandler
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.ApplicationVariantData
import com.android.builder.core.AndroidBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ComponentMetadataDetails
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.plugins.PluginContainer
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject


class SamplePlugin implements Plugin<Project> {
    protected Instantiator instantiator

    static def applyPlugin(Project project, Class<? extends Plugin> pluginClazz) {
        PluginContainer pluginManager = project.getPlugins()
        if (pluginManager.hasPlugin(pluginClazz)) {
            return
        }
        pluginManager.apply(pluginClazz)
    }

    @Inject
    SamplePlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
        //如果没有应用，则应用
        applyPlugin(project, AppPlugin.class)

        //create extension
        project.getExtensions().create("sample", SampleExtension.class, project, instantiator)

        AppPlugin appPlugin = project.getPlugins().findPlugin(AppPlugin.class)
        LibraryPlugin libraryPlugin = project.getPlugins().findPlugin(LibraryPlugin.class)

        boolean isApp = project.getPlugins().hasPlugin(AppPlugin.class)
        boolean isLibrary = project.getPlugins().hasPlugin(LibraryPlugin.class)

        project.println "appPlugin:${appPlugin}"
        project.println "libraryPlugin:${libraryPlugin}"

        project.println "isApp:${isApp}"
        project.println "isLibrary:${isLibrary}"

        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class)
        LibraryExtension libraryExtension = project.getExtensions().findByType(LibraryExtension.class)

        project.println "appExtension:${appExtension}"
        project.println "libraryExtension:${libraryExtension}"



        if (isApp) {
            TaskManager taskManager = BasePlugin.getMetaClass().getProperty(appPlugin, "taskManager") as TaskManager
            project.println "taskManager:${taskManager}"

            appExtension.getApplicationVariants().all { ApplicationVariantImpl variant ->

                ApplicationVariantData variantData = variant.getMetaClass().getProperty(variant, 'variantData') as ApplicationVariantData
                VariantScope variantScope = variantData.getScope()
                GlobalScope globalScope = variantScope.getGlobalScope()
                AndroidBuilder androidBuilder = globalScope.getAndroidBuilder()

                SdkHandler sdkHandler = globalScope.getSdkHandler()
                NdkHandler ndkHandler = globalScope.getNdkHandler()
                project.println "androidBuilder:${androidBuilder}"
                project.println "sdkHandler:${sdkHandler}"
                project.println "ndkHandler:${ndkHandler}"


                project.println "variantData:${variantData}"
                project.println "variantScope:${variantScope}"
                project.println "globalScope:${globalScope}"

            }
        } else {
            libraryExtension.getLibraryVariants().all { LibraryVariantImpl variant ->
                //ignore
            }
        }

        //get all configurations
        project.getConfigurations().all { Configuration configuration ->
            project.logger.error "configuration:${configuration}"
        }
        //get a configuration
        Configuration compileConfiguration = project.getConfigurations().getByName("compile")
        //create a configuration
        project.getConfigurations().create("customCompile") { Configuration configuration ->
            compileConfiguration.extendsFrom(configuration)
        }
        //get all repositorys
        project.getRepositories().all { ArtifactRepository repository ->
            project.logger.error "repository:${repository.getName()}"
        }
        //add a dependency
        project.getDependencies().add("compile", "com.android.support:multidex:1.0.1")
        //get all components
        project.getDependencies().getComponents().all { ComponentMetadataDetails componentMetadataDetails ->
            project.logger.error "componentMetadataDetails:${componentMetadataDetails.getId()}"
        }
        //read extension
        project.afterEvaluate {
            SampleExtension sampleExtension = project.getExtensions().findByType(SampleExtension.class)
            project.logger.error "${sampleExtension.buildToolsVersion}"
            project.logger.error "${sampleExtension.compileSdkVersion}"
            project.logger.error "${sampleExtension.defaultSampleConfig}"
            sampleExtension.sampleConfig.all { SampleConfig sampleConfig ->
                project.logger.error "${sampleConfig.name}:${sampleConfig}"
            }
        }

        //get a dependency file
        Dependency dependency = project.getDependencies().create("com.android.support:multidex:1.0.1")
        Configuration configuration = project.getConfigurations().detachedConfiguration(dependency)
        //不进行传递依赖
        configuration.setTransitive(false)
        project.logger.error "dependency files:${configuration.getFiles()}"

    }
}

