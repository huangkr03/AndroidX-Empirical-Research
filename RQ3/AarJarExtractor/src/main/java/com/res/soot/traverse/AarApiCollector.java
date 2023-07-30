package com.res.soot.traverse;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import soot.*;
import com.alibaba.fastjson.*;
import soot.options.Options;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AarApiCollector {
    private String apkPath;
    private String androidJar;

    public void bundledAnalyze(Artifact artifact) {
        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            MySootClass mySootClass = new MySootClass();
            mySootClass.name = sootClass.getName();
            for (SootMethod method : sootClass.getMethods()) {
                mySootClass.methods.add(method.getSignature());
            }
            for (SootField field : sootClass.getFields()) {
                mySootClass.fields.add(field.getSignature());
            }
            artifact.bundledClasses.add(mySootClass);
        }
    }

    public void setUp() {
        Options.v().set_src_prec(Options.src_prec_apk);

        List<String> pathList = new ArrayList<String>();
        pathList.add(apkPath);

        Options.v().set_process_dir(pathList);
        Options.v().set_force_android_jar(androidJar);
        Options.v().set_keep_line_number(true);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_allow_phantom_refs(true);

        for (String cl : SourceLocator.v().getClassesUnder(apkPath)) {
            SootClass theClass = Scene.v().loadClass(cl, 3);
            if (!theClass.isPhantom()) {
                theClass.setApplicationClass();
            }
        }
    }

    public static void Aarunzip(String zipFilePath, String dest) {
        int index = zipFilePath.lastIndexOf('.');
        String artifact = zipFilePath.substring(zipFilePath.lastIndexOf(File.separator) + 1, index);

        String destDirectory = dest + File.separator + artifact + File.separator + "unzip" + File.separator;//zipFilePath.substring(0, zipFilePath.lastIndexOf('.')) + File.separator + "unzip" + File.separator;
        byte[] buffer = new byte[1024];

        try {
            // create output directory if it doesn't exist
            File destDir = new File(destDirectory);
            System.out.println(destDir.getAbsolutePath());
            if (!destDir.exists()) {
                System.out.println(destDir.mkdirs());
            }

            // create ZipInputStream object
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));

            // iterates over entries in the zip file
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extract it
                    extractFile(zipIn, filePath, buffer);
                } else {
                    // if the entry is a directory, create the directory
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            zipIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractFile(ZipInputStream zipIn, String filePath, byte[] buffer) throws IOException {
        if (filePath.indexOf('/') != -1) {
            File directory = new File(filePath.substring(0, filePath.lastIndexOf("/")));
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        int len;
        while ((len = zipIn.read(buffer)) > 0) {
            bos.write(buffer, 0, len);
        }
        bos.close();
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage:");
            System.err.println("    java -jar executable.jar <aar-path> <android-jar-path> <output-directory>");
            System.exit(-1);
        }

        String aarOrJarPath = args[0];
        String androidJarPath = args[1];
        String outputDirectory = args[2];

        try {
            if (args[0].endsWith(".aar")) {
                Aarunzip(aarOrJarPath, args[2]);
                AarApiCollector analysisJar = new AarApiCollector();
                analysisJar.androidJar = androidJarPath;

                int index = aarOrJarPath.lastIndexOf('.');
                String artifact = aarOrJarPath.substring(aarOrJarPath.lastIndexOf(File.separator) + 1, index);
                analysisJar.apkPath = outputDirectory + File.separator + artifact + File.separator + "unzip" + File.separator + "classes.jar";
                Artifact a = new Artifact(artifact);
                analysisJar.setUp();
                analysisJar.bundledAnalyze(a);
                File output = new File(outputDirectory + File.separator + artifact + ".json");
                // write to file
                FileWriter writer = new FileWriter(output);
                writer.write(JSON.toJSONString(a, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                        SerializerFeature.WriteDateUseDateFormat));
                writer.close();
            } else { //JAR
                AarApiCollector analysisJar = new AarApiCollector();
                analysisJar.androidJar = args[1];

                int index = aarOrJarPath.lastIndexOf('.');
                String artifact = aarOrJarPath.substring(aarOrJarPath.lastIndexOf(File.separator) + 1, index);
                analysisJar.apkPath = androidJarPath;//args[2] + File.separator + artifact + File.separator + "unzip" + File.separator + "classes.jar";
                Artifact a = new Artifact(artifact);
                analysisJar.setUp();
                analysisJar.bundledAnalyze(a);
                File output = new File(args[2] + File.separator + artifact + ".json");
                // write to file
                FileWriter writer = new FileWriter(output);
                writer.write(JSON.toJSONString(a, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                        SerializerFeature.WriteDateUseDateFormat));
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Artifact {
    @JSONField(name = "name", ordinal = 0)
    public String name;
    @JSONField(name = "bundledClasses", ordinal = 1)
    public List<MySootClass> bundledClasses = new ArrayList<>();

    public Artifact(String name) {
        this.name = name;
    }
}


