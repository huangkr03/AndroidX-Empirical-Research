package com.res.soot.traverse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class MainExtractor extends Extractor {

    public MainExtractor(String apkPath, String androidJar) {
        super(apkPath, androidJar);
    }

    public MyAPK analyze() {
        MyAPK myApk = new MyAPK();
        myApk.compassMethods = analyseSingleApk();
        return myApk;
    }

    private void output(MyAPK myApk, String outputPath) {
        try(FileWriter fileWriter = new FileWriter(outputPath)) {
            String jsonContent = JSON.toJSONString(myApk,
                    SerializerFeature.PrettyFormat,
                    SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteDateUseDateFormat
            );
            fileWriter.write(jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length != 3) {
            System.err.println("Usage:");
            System.err.println("    java -jar executable.jar <android.jar-directory> <apk-path> <output-directory>");
            System.exit(-1);
        }

        String androidJar = args[0];    // android platform path
        String apkPath = args[1];       // apk file path
        String outPath = args[2];       // output dir path

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            try {
                System.out.println("Processing " + apkPath);
                MainExtractor mainExtractor = new MainExtractor(apkPath, androidJar); //analyze apk
                MyAPK myApk = mainExtractor.analyze();
                String outputPath = outPath + File.separator + apkPath.substring(apkPath.contains(File.separator) ? apkPath.lastIndexOf(File.separator) + 1 : 0, apkPath.lastIndexOf('.')) + ".json";
                mainExtractor.output(myApk, outputPath); //output json file
            } catch (Exception e) {
                System.err.println("Error processing " + apkPath);
                e.printStackTrace();
            }
            return "Done";
        });

        try {
            future.get(10, TimeUnit.MINUTES);
        } catch (Exception | Error e) {
            System.exit(-1);
        } finally {
            future.cancel(true); // Cancel the task if it hasn't completed
            executor.shutdownNow(); // Shut down the executor
        }
    }
}
