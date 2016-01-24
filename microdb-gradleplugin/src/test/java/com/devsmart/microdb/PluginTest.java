package com.devsmart.microdb;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

public class PluginTest {

    @Test
    public void compilePluginTest() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("java");
        project.getPluginManager().apply("com.devsmart.microdb");

        assertNotNull(project.getTasks().findByName("generateMicroDBSources"));
    }
}
