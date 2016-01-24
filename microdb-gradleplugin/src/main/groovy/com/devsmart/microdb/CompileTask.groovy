package com.devsmart.microdb

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


class CompileTask extends DefaultTask {


    @InputDirectory
    def File inputDir

    @OutputDirectory
    def File outputDir


    @TaskAction
    def compileDBOSources() {

        inputs.outOfDate { change ->
            println "out of date: ${change.file.name}"

        }

        inputs.removed { change ->
            println "removed: ${change.file.name}"
        }

    }
}
