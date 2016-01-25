package com.devsmart.microdb

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import com.devsmart.microdb.Generator;


class MicroDBCompileTask extends DefaultTask {


    @InputDirectory
    def File inputDir

    @OutputDirectory
    def File outputDir


    @TaskAction
    def compileDBOSources(IncrementalTaskInputs inputs) {

        inputs.outOfDate { change ->
            if(change.file.absolutePath.endsWith(".dbo")) {
                println "MicroDB: generating DBO for: ${change.file}"
                Generator gen = new Generator()
                gen.mOutputDir = outputDir
                gen.mClassPath += [inputDir, outputDir]

                gen.compileFile(change.file)
            }

        }

        inputs.removed { change ->
            println "removed: ${change.file.name}"
        }

    }
}
