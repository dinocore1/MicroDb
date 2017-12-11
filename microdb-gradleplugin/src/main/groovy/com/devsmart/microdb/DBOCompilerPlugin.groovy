package com.devsmart.microdb

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.plugins.ide.idea.IdeaPlugin


class DBOCompilerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.afterEvaluate {
            if(isAndroidProject(project)) {
                println("Android Project Detected")
                //applyToAndroidProject(project)
                addMicroDbTasks(project)
            } else if (isJavaProject(project)) {
                println("Java Project Detected")
                //applyToJavaProject(project)
                addMicroDbTasks(project)
            }
        }

    }

    private addMicroDbTasks(Project project) {
        project.sourceSets.all { SourceSet sourceSet ->
            addTasksToProjectForSourceSet(project, sourceSet)
        }
    }

    private def addTasksToProjectForSourceSet(Project project, SourceSet sourceSet) {
        def microdbConfigName = (sourceSet.getName().equals(SourceSet.MAIN_SOURCE_SET_NAME) ? "microdb" : sourceSet.getName() + "microdb")

        final File genSrcOutputDir = new File(project.projectDir, "microdb-generated/${sourceSet.name}")
        def genMicroDBSourcesTaskName = sourceSet.getTaskName('generate', 'microdb')
        def generateJavaTask = project.tasks.create(genMicroDBSourcesTaskName, MicroDBCompileTask.class, {
            inputDir = new File(project.projectDir, "src/${sourceSet.name}/java")
            outputDir = genSrcOutputDir
        })

        sourceSet.java.srcDir genSrcOutputDir
        String compileJavaTaskName = sourceSet.getCompileTaskName("java")
        Task compileJavaTask = project.tasks.getByName(compileJavaTaskName)
        compileJavaTask.dependsOn(generateJavaTask)

        project.tasks.clean.doFirst {
            delete genSrcOutputDir
        }
    }

    boolean isJavaProject(project) {
        return project.plugins.hasPlugin('java')
    }

    boolean isAndroidProject(project) {
        return hasAndroidPlugin(project) || hasAndroidLibraryPlugin(project)
    }

    boolean hasAndroidPlugin(project) {
        return project.plugins.hasPlugin('com.android.application')
    }

    boolean hasAndroidLibraryPlugin(project) {
        project.plugins.hasPlugin('com.android.library')
    }

    def sourceSetName(variant) {
        variant.dirName.split('/').last()
    }
}
