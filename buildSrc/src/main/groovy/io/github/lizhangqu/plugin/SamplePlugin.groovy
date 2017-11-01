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


class SamplePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        AppPlugin appPlugin = project.plugins.getPlugin(AppPlugin)
        LibraryPlugin libraryPlugin = project.plugins.findPlugin(LibraryPlugin)

        boolean isApp = project.plugins.hasPlugin(AppPlugin)
        boolean isLibrary = project.plugins.hasPlugin(LibraryPlugin)

        project.println "appPlugin:${appPlugin}"
        project.println "libraryPlugin:${libraryPlugin}"

        project.println "isApp:${isApp}"
        project.println "isLibrary:${isLibrary}"

        AppExtension appExtension = project.extensions.findByType(AppExtension)
        LibraryExtension libraryExtension = project.extensions.findByType(LibraryExtension)

        project.println "appExtension:${appExtension}"
        project.println "libraryExtension:${libraryExtension}"



        if (isApp) {
            TaskManager taskManager = BasePlugin.getMetaClass().getProperty(appPlugin, "taskManager") as TaskManager
            project.println "taskManager:${taskManager}"

            appExtension.applicationVariants.all { ApplicationVariantImpl variant ->

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
            libraryExtension.libraryVariants.all { LibraryVariantImpl variant ->
                //ignore
            }
        }
    }
}
