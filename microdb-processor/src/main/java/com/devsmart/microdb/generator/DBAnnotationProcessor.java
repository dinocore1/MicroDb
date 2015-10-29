package com.devsmart.microdb.generator;


import com.devsmart.microdb.Link;
import com.devsmart.microdb.annotations.DBObj;
import com.devsmart.microdb.annotations.DataSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;

public class DBAnnotationProcessor extends AbstractProcessor {


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_6;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> retval = new HashSet<String>();
        retval.add(DBObj.class.getCanonicalName());
        retval.add(Link.class.getCanonicalName());

        return retval;
    }

    private void error(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

    private void error(String message, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, e);
    }

    private void note(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for(Element classElement : roundEnv.getElementsAnnotatedWith(DBObj.class)) {

            if(!classElement.getKind().equals(ElementKind.CLASS)) {
                error("DBObj annotation can only be applied to classes", classElement);
            }

            note("Processing class: " + classElement.getSimpleName());

            ProxyFileGenerator generator = new ProxyFileGenerator(processingEnv, (TypeElement)classElement);
            if(generator.validate()) {
                generator.generate();
            }

        }

        for(Element classElement : roundEnv.getElementsAnnotatedWith(DataSet.class)) {
            if(!classElement.getKind().equals(ElementKind.CLASS)) {
                error("DataSet annotation can only be applied to classes", classElement);
            }

            note("Processing class: " + classElement.getSimpleName());

            DatasetGenerator generator = new DatasetGenerator(processingEnv, (TypeElement)classElement);
            if(generator.validate()) {
                generator.generate();
            }
        }

        return false;
    }
}
