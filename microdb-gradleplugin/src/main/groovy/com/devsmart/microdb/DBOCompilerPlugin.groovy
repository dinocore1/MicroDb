package com.devsmart.microdb

import org.gradle.api.Plugin
import org.gradle.api.Project


class DBOCompilerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.tasks.create('generateMicrodbSources', CompileTask.class, {
            //inputDir = file("$proj"project.sourceSets.
            outputDir = file("$buildDir/generated-sources")
        })


    }
}
