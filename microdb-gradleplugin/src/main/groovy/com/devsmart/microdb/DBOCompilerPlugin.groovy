package com.devsmart.microdb

import org.gradle.api.Plugin
import org.gradle.api.Project


class DBOCompilerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        if (isJavaProject(project)) {
            applyToJavaProject(project)
        }

    }

    def applyToJavaProject(project) {
        File genSrcOutputDir = new File("src/generated-sources/main/java");
        project.tasks.create('generateMicroDBSources', MicroDBCompileTask.class, {
            inputDir = new File("src/main/java")
            outputDir = genSrcOutputDir
        })

        project.sourceSets {
            generated {
                java.srcDir genSrcOutputDir
            }
        }


        project.tasks.compileJava {
            dependsOn('generateMicroDBSources')
            source += project.sourceSets.generated.java
        }


        //project.sourceSets.main.java.srcDirs += genSrcOutputDir
        //project.tasks.getByName('compileJava').dependsOn 'generateMicroDBSources'
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
}
