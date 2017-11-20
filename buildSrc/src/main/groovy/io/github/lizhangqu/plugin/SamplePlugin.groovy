package io.github.lizhangqu.plugin

import com.android.build.gradle.AndroidGradleOptions
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.SdkHandler
import com.android.build.gradle.internal.TaskManager
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.api.LibraryVariantImpl
import com.android.build.gradle.internal.core.GradleVariantConfiguration
import com.android.build.gradle.internal.dsl.CoreBuildType
import com.android.build.gradle.internal.dsl.CoreProductFlavor
import com.android.build.gradle.internal.ndk.NdkHandler
import com.android.build.gradle.internal.scope.AndroidTask
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.internal.scope.VariantOutputScope
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.ApplicationVariantData
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.internal.variant.BaseVariantOutputData
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.build.gradle.tasks.MergeManifests
import com.android.builder.Version
import com.android.builder.core.AndroidBuilder
import com.android.builder.core.VariantConfiguration
import com.android.builder.dependency.level2.AndroidDependency
import com.android.builder.model.ApiVersion
import com.android.manifmerger.ManifestMerger2
import com.android.manifmerger.ManifestProvider
import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ComponentMetadataDetails
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.plugins.PluginContainer
import org.gradle.internal.reflect.Instantiator
import org.gradle.util.NameMatcher

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

        project.afterEvaluate {
            NameMatcher matcher = new NameMatcher()
            String actualName = matcher.find("aR", project.getTasks().asMap.keySet())
            project.logger.error("taskName:${actualName}")
//            project.logger.error("${project.gradle.startParameter.taskNames}")
        }

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

//        appExtension.registerTransform()

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

                //gradle plugin version
                project.logger.error "version:${com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION}"

                //merge manifest
                List<BaseVariantOutputData> baseVariantOutputDatas = variantData.getOutputs()
                baseVariantOutputDatas.each { BaseVariantOutputData variantOutputData ->
                    project.println "variantOutputData:${variantOutputData}"
                    VariantOutputScope scope = variantOutputData.scope

                    final VariantConfiguration<CoreBuildType, CoreProductFlavor, CoreProductFlavor> config =
                            scope.getVariantScope().getVariantData().getVariantConfiguration()

                    File mainManifestFile = config.getMainManifest()
                    List<File> manifestOverlaysFile = config.getManifestOverlays()

                    List<ManifestProvider> manifests = Lists.newArrayList(
                            config.getFlatPackageAndroidLibraries())
                    //noinspection GroovyAssignabilityCheck
                    manifests.addAll(config.getFlatAndroidAtomsDependencies())

                    if (scope.getVariantScope().getMicroApkTask() != null &&
                            variantData.getVariantConfiguration().getBuildType().
                                    isEmbedMicroApp()) {
                        //noinspection GroovyAssignabilityCheck
                        manifests.add(new ManifestProvider() {
                            @Override
                            File getManifest() {
                                return scope.getVariantScope().getMicroApkManifestFile()
                            }

                            @Override
                            String getName() {
                                return "Wear App sub-manifest"
                            }
                        })
                    }

                    if (scope.getCompatibleScreensManifestTask() != null) {
                        //noinspection GroovyAssignabilityCheck
                        manifests.add(new ManifestProvider() {
                            @Override
                            File getManifest() {
                                return scope.getCompatibleScreensManifestFile()
                            }

                            @Override
                            String getName() {
                                return "Compatible-Screens sub-manifest"
                            }
                        })
                    }

                    String packageOverride = config.getIdOverride()
                    int versionCode = config.getVersionCode()
                    String versionName = config.getVersionName()

                    ApiVersion minSdk = config.getMergedFlavor().getMinSdkVersion()
                    String minSdkVersion = minSdk == null ? null : minSdk.getApiString()

                    ApiVersion targetSdk = config.getMergedFlavor().getTargetSdkVersion()
                    String targetSdkVersion = targetSdk == null ? null : targetSdk.getApiString()

                    Integer maxSdkVersion = config.getMergedFlavor().getMaxSdkVersion()

                    File manifestOutputFile = scope.getManifestOutputFile()
                    File instantRunManifestOutputFile = scope.getVariantScope().getInstantRunManifestOutputFile()
                    Map<String, Object> manifestPlaceholders = config.getManifestPlaceholders()


                    ImmutableList.Builder<ManifestMerger2.Invoker.Feature> optionalFeaturesBuilder =
                            ImmutableList.builder()

                    def pluginTaskManager = BasePlugin.getMetaClass().getProperty(appPlugin, "taskManager") as TaskManager
                    if (pluginTaskManager.getIncrementalMode(
                            variantScope.getVariantConfiguration()) != TaskManager.IncrementalMode.NONE) {
                        optionalFeaturesBuilder.add(ManifestMerger2.Invoker.Feature.INSTANT_RUN_REPLACEMENT)
                    }
                    if (AndroidGradleOptions.getTestOnly(project)) {
                        optionalFeaturesBuilder.add(ManifestMerger2.Invoker.Feature.TEST_ONLY)
                    }
                    ImmutableList<ManifestMerger2.Invoker.Feature> optionalFeatures = optionalFeaturesBuilder.build()

                    File manifestReportFile = variantScope.getManifestReportFile()

                    androidBuilder.mergeManifestsForApplication(
                            mainManifestFile,
                            manifestOverlaysFile,
                            manifests,
                            packageOverride,
                            versionCode,
                            versionName,
                            minSdkVersion,
                            targetSdkVersion,
                            maxSdkVersion,
                            manifestOutputFile.getAbsolutePath() + ".generate.xml",
                            null,
                            instantRunManifestOutputFile.getAbsolutePath() + ".generate.xml",
                            ManifestMerger2.MergeType.APPLICATION,
                            manifestPlaceholders,
                            optionalFeatures,
                            manifestReportFile
                    )


                }


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

