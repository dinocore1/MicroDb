package com.devsmart.microdb

import org.gradle.api.Plugin
import org.gradle.api.Project


class DBOCompilerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.afterEvaluate {
            if (isJavaProject(project)) {
                applyToJavaProject(project)
            } else if(isAndroidProject(project)) {
                applyToAndroidProject(project)
            }
        }


    }

    def applyToJavaProject(Project project) {
        File genSrcOutputDir = new File(project.projectDir, "build/generated-sources/java");
        def genTask = project.tasks.create('generateMicroDBSources', MicroDBCompileTask.class, {
            inputDir = new File(project.projectDir, "src/main/java")
            outputDir = genSrcOutputDir
        })

        project.sourceSets {
            main {
                java.srcDir genSrcOutputDir
            }
        }

        project.tasks.compileJava {
            dependsOn(genTask)
        }

        project.tasks.clean.doFirst {
            delete genSrcOutputDir
        }

    }

    def applyToAndroidProject(Project project) {
        def androidExtension
        def variants
        if(hasAndroidPlugin(project)) {
            androidExtension = project.plugins.getPlugin('android').extension
            variants = androidExtension.applicationVariants
        } else if(hasAndroidLibraryPlugin(project)) {
            androidExtension = project.plugins.getPlugin('android-library').extension
            variants = androidExtension.libraryVariants
        }

        variants.all { variant ->
            println "applying to: ${variant.name}"

            File genSrcOutputDir = new File(project.projectDir, "generated-sources/${variant.name}/java");

            def genTask = project.tasks.create("generateMicroDB${variant.name}Sources", MicroDBCompileTask.class, {
                inputDir = androidExtension.sourceSets['main'].java.srcDirs[0]
                outputDir = genSrcOutputDir
                classpath.addAll androidExtension.sourceSets['main'].java.srcDirs
            })

            androidExtension.sourceSets[sourceSetName(variant)].java.srcDirs += genSrcOutputDir

            def javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile

            javaCompile.dependsOn(genTask)

            project.tasks.clean.doFirst {
                delete genSrcOutputDir
            }

        }

    }

    def isJavaProject(project) {
        project.plugins.hasPlugin('java')
    }

    def isAndroidProject(project) {
        hasAndroidPlugin(project) || hasAndroidLibraryPlugin(project)
    }

    def hasAndroidPlugin(project) {
        project.plugins.hasPlugin('com.android.application')
    }

    def hasAndroidLibraryPlugin(project) {
        project.plugins.hasPlugin('com.android.library')
    }

    def sourceSetName(variant) {
        variant.dirName.split('/').last()
    }
}
