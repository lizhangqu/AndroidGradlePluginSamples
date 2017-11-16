package io.github.lizhangqu.plugin

import com.android.annotations.NonNull
import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class SampleTransform extends Transform {

    Project project

    SampleTransform(Project prject) {
        this.project = prject
    }

    @Override
    String getName() {
        return "sample"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    Set<QualifiedContent.Scope> getReferencedScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Context context = transformInvocation.context
        boolean incremental = transformInvocation.incremental
        TransformOutputProvider transformOutputProvider = transformInvocation.outputProvider
        Collection<TransformInput> referencedInputs = transformInvocation.referencedInputs
        Collection<TransformInput> secondaryInputs = transformInvocation.secondaryInputs
        Collection<TransformInput> inputs = transformInvocation.inputs

        project.logger.error "${context.path}"
        project.logger.error "${context.temporaryDir}"
        project.logger.error "${incremental}"

        inputs.each { TransformInput transformInput ->
            Collection<DirectoryInput> directoryInputs = transformInput.directoryInputs
            Collection<JarInput> jarInputs = transformInput.jarInputs

            directoryInputs.each { DirectoryInput directoryInput ->
                project.logger.error "directoryInput:${directoryInput.getFile()}"
                File destFile = transformOutputProvider.getContentLocation(directoryInput.getName(), getInputTypes(), getScopes(), Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.getFile(), destFile)
            }

            jarInputs.each { JarInput jarInput ->
                project.logger.error "jarInput:${jarInput.getFile()}"
                File destFile = transformOutputProvider.getContentLocation(jarInput.getName(), getInputTypes(), getScopes(), Format.JAR)
                FileUtils.copyFile(jarInput.getFile(), destFile)
            }
        }

        referencedInputs.each { TransformInput transformInput ->
            Collection<DirectoryInput> directoryInputs = transformInput.directoryInputs
            Collection<JarInput> jarInputs = transformInput.jarInputs

            directoryInputs.each { DirectoryInput directoryInput ->
                project.logger.error "referencedInputs directoryInput:${directoryInput.getFile()}"
            }

            jarInputs.each { JarInput jarInput ->
                project.logger.error "referencedInputs jarInput:${jarInput.getFile()}"
            }
        }
    }
}
