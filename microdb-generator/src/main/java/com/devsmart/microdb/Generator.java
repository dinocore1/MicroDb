package com.devsmart.microdb;


import com.devsmart.microdb.ast.Nodes;
import com.squareup.javapoet.JavaFile;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;

public class Generator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);

    private LinkedHashSet<File> mClassPath = new LinkedHashSet<File>();
    private File mOutputDir;
    private Nodes.FileNode mFileNode;

    public Generator() {

    }

    public void writeOutputFiles() throws IOException {
        for(Nodes.DBONode dbo : mFileNode.dboList) {
            JavaCodeGenerator generator = new JavaCodeGenerator(dbo, mFileNode);

            String code = generator.generateCode();
            final String output = mFileNode.packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
            File outputDir = new File(mOutputDir, output);
            outputDir.mkdirs();
            File outputFile = new File(outputDir, dbo.name + ".java");
            FileWriter fout = new FileWriter(outputFile);
            fout.write(code);
            fout.close();

            /*
            JavaFile outputJavaCode = generator.createJavaFile();
            final String output = fileNode.packageName.replaceAll("\\.", File.separator);
            File outputDir = new File(mOutputDir, output);
            outputDir.mkdirs();
            File outputFile = new File(outputDir, dbo.name + ".java");
            FileWriter fout = new FileWriter(outputFile);
            outputJavaCode.writeTo(fout);
            fout.close();
            */
        }
    }

    /**
     * Compile a DBO file. This function will not write the output files. To do so,
     * call @{code writeOutputFiles()} after calling this method.
     * @param inputStream
     * @return true if compile completed without any errors.
     */
    public boolean compileInputStream(ANTLRInputStream inputStream) {
        CompilerContext compilerContext = new CompilerContext();

        MicroDBLexer lexer = new MicroDBLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MicroDBParser parser = new MicroDBParser(tokens);
        parser.addErrorListener(compilerContext.parserErrorHandler);

        MicroDBParser.FileContext root = parser.file();

        SemPass1 semPass1 = new SemPass1(compilerContext);
        mFileNode = (Nodes.FileNode) semPass1.visitFile(root);

        if(compilerContext.hasErrors()) {
            compilerContext.reportMessages(System.err);
            return false;
        }

        SemPass2 semPass2 = new SemPass2(compilerContext);
        semPass2.visit(root);

        if(compilerContext.hasErrors()) {
            compilerContext.reportMessages(System.err);
            return false;
        }

        return true;
    }

    /**
     * compile a DBO file and write output files.
     * @param file
     * @return true if compile completed without any errors.
     * @throws IOException
     */
    public boolean compileFile(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        ANTLRInputStream inputStream = new ANTLRInputStream(fin);
        inputStream.name = file.getAbsolutePath();
        final boolean success = compileInputStream(inputStream);

        if(success) {
            writeOutputFiles();
        }

        return success;
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
