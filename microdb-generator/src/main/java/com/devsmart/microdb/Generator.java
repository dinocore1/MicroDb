package com.devsmart.microdb;


import com.devsmart.microdb.ast.Nodes;
import com.squareup.javapoet.JavaFile;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;

public class Generator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);

    private LinkedHashSet<File> mClassPath = new LinkedHashSet<File>();
    private File mOutputDir;

    public Generator() {

    }

    public void compileFile(File file) throws IOException {
        CompilerContext compilerContext = new CompilerContext();

        FileInputStream fin = new FileInputStream(file);
        ANTLRInputStream inputStream = new ANTLRInputStream(fin);
        MicroDBLexer lexer = new MicroDBLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MicroDBParser parser = new MicroDBParser(tokens);

        MicroDBParser.FileContext root = parser.file();

        SemPass1 semPass1 = new SemPass1(compilerContext);
        Nodes.FileNode fileNode = (Nodes.FileNode) semPass1.visitFile(root);
        SemPass2 semPass2 = new SemPass2(compilerContext);
        semPass2.visit(root);


        for(Nodes.DBONode dbo : fileNode.dboList) {
            JavaCodeGenerator generator = new JavaCodeGenerator(dbo, fileNode);
            JavaFile outputJavaCode = generator.createJavaFile();
            final String output = fileNode.packageName.replaceAll("\\.", File.separator);
            File outputDir = new File(mOutputDir, output);
            outputDir.mkdirs();
            File outputFile = new File(outputDir, dbo.name + ".java");
            FileWriter fout = new FileWriter(outputFile);
            outputJavaCode.writeTo(fout);
            fout.close();
        }
        fin.close();
    }

    public void compileDir(File root) throws IOException {
        for(File f : root.listFiles()) {
            if(f.isDirectory()) {
                compileDir(f);
            } else if(f.getName().endsWith(".dbo")) {
                compileFile(f);
            }
        }
    }


    private static void warn(String msg) {
        System.out.println("Warning: " + msg);
    }

    public static void main(String[] args) {

        Options options = new Options();


        options.addOption(Option.builder("cp")
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .hasArg()
                .argName("path")
                .desc("classpath")
                .build());

        options.addOption(Option.builder("d")
                .hasArg()
                .argName("path")
                .desc("output dir")
                .build());



        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdline = parser.parse(options, args);

            String[] remainingArgs = cmdline.getArgs();
            if(remainingArgs == null || remainingArgs.length == 0) {
                throw new ParseException("no files found");
            }

            Generator gen = new Generator();
            gen.mOutputDir = new File(cmdline.getOptionValue("d", ""));
            if(cmdline.hasOption("cp")) {
                for (String cp : cmdline.getOptionValues("cp")) {
                    gen.mClassPath.add(new File(cp));
                }
            }

            for(String filePath : remainingArgs) {
                File f = new File(filePath);
                if(!f.exists()) {
                    warn("file not found: " + f);
                } else {
                    if(f.isDirectory()) {
                        gen.compileDir(f);
                    } else {
                        gen.compileFile(f);
                    }
                }
            }


        } catch (ParseException e) {
            System.err.println("cmd line parse failed: " + e.getMessage());

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("app [OPTIONS] [FILE]..", options);
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }


    }


}
