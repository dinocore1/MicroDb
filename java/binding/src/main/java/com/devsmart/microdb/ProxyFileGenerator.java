package com.devsmart.microdb;


import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class ProxyFileGenerator {


    private final ProcessingEnvironment mEnv;
    private final TypeElement mClassElement;

    public ProxyFileGenerator(ProcessingEnvironment env, TypeElement classElement) {
        mEnv = env;
        mClassElement = classElement;
    }

    private void error(String message) {
        mEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

    private void error(String message, Element e) {
        mEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, e);
    }

    private void note(String message) {
        mEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    public boolean validate() {
        // Get the package of the class
        Element enclosingElement = mClassElement.getEnclosingElement();
        if (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
            error("MicroDB DBObj does not support nested classes", mClassElement);
            return false;
        }

        if (! mClassElement.getSuperclass().toString().equals(DBObject.class.getCanonicalName())) {
            error("MicroDB DBObj must be derived from " + DBObject.class.getSimpleName(), mClassElement);
            return false;
        }

        return true;
    }

    public void generate() {
        //String qualifiedGeneratedClassName = String.format("%s.%s", Constants.REALM_PACKAGE_NAME, Utils.getProxyClassName(className));
        //JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        //JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));
    }
}
