package com.res.soot.traverse;

import soot.*;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.options.Options;
import soot.util.Chain;

import java.util.*;


public class Extractor {
    private final String apkPath;

    private final String androidJar;

    private HashSet<SootMethod> usedMethods = new HashSet<>();

    private HashSet<SootField> usedFields = new HashSet<>();

    public Extractor(String apkPath, String androidJar) {
        this.apkPath = apkPath;
        this.androidJar = androidJar;
    }

    private boolean isExcluded(String cl) {
        return cl.startsWith("android.")
                || cl.startsWith("androidx.")
                || cl.startsWith("com.android.")
                || cl.startsWith("java.")
                || cl.startsWith("javax.")
                || cl.startsWith("sun.");
    }


    private void setUpSoot() {
        G.reset();
        Options.v().set_src_prec(Options.src_prec_apk);
        String androidJarPath = Scene.v().getAndroidJarPath(androidJar, apkPath);
        List<String> pathList = new ArrayList<>();
        pathList.add(apkPath);
        pathList.add(androidJarPath);

        Options.v().set_process_dir(pathList);
        Options.v().set_force_android_jar(androidJar);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_no_writeout_body_releasing(true);
        Options.v().set_whole_program(false);

        Scene.v().loadNecessaryClasses();
    }


    private void analyseSupperClassFields(SootField sootField, SootMethod caller) {
        SootClass declaringClass = sootField.getDeclaringClass();
        while (declaringClass.hasSuperclass()) {
            SootClass superClass = declaringClass.getSuperclass();
            String subSignature = sootField.getSubSignature();
            if (superClass.declaresField(subSignature)) {
                String signature = superClass.getField(subSignature).getSignature();
                if (signature.startsWith("<androidx") || signature.startsWith("<android.support")) {
                    usedFields.add(superClass.getField(subSignature));
                    break;
                }
            }
            declaringClass = superClass;
        }
        //traverse the interfaces
        declaringClass = sootField.getDeclaringClass();
        if (declaringClass.getInterfaceCount() != 0) { //if the class implements interfaces
            Chain<SootClass> interfaces = declaringClass.getInterfaces();
            for(SootClass inter: interfaces) {
                String subSignature = sootField.getSubSignature();
                if (inter.declaresField(subSignature)) {
                    String signature = inter.getField(subSignature).getSignature();
                    if (signature.startsWith("<androidx") || signature.startsWith("<android.support")) {
                        usedFields.add(inter.getField(subSignature));
                    }
                }
            }
        }
    }

    private void analyseSupperClassMethods(SootMethod sootMethod, SootMethod caller) {
        SootClass declaringClass = sootMethod.getDeclaringClass();
        while (declaringClass.hasSuperclass()) {
            SootClass superClass = declaringClass.getSuperclass();
            String subSignature = sootMethod.getSubSignature();
            if (superClass.declaresMethod(subSignature)) {
                String signature = superClass.getMethod(subSignature).getSignature();
                if (signature.startsWith("<androidx") || signature.startsWith("<android.support")) {
                    usedMethods.add(superClass.getMethod(subSignature));
                    break;
                }
            }
            declaringClass = superClass;
        }
        //traverse the interfaces
        declaringClass = sootMethod.getDeclaringClass();
        if (declaringClass.getInterfaceCount() != 0) { //if the class implements interfaces
            Chain<SootClass> interfaces = declaringClass.getInterfaces();
            interfaces.forEach(inter -> {
                String subSignature = sootMethod.getSubSignature();
                if (inter.declaresMethod(subSignature)) {
                    String signature = inter.getMethod(subSignature).getSignature();
                    if (signature.startsWith("<androidx") || signature.startsWith("<android.support")) {
                        usedMethods.add(inter.getMethod(subSignature));
                    }
                }
            });
        }
    }

    public List<MySootClass> analyseSingleApk() {
        setUpSoot();

        for (SootClass sc : Scene.v().getApplicationClasses()) {
            if (!isExcluded(sc.getName())) {
                for (SootMethod sm : sc.getMethods()) {
                    try { //try to retrieve active body
                        sm.retrieveActiveBody();
                    } catch (Exception ignored) {}
                    if (sm.hasActiveBody()) {
                        for (Unit unit : sm.getActiveBody().getUnits()) {
                            for (ValueBox box : unit.getUseAndDefBoxes()) {
                                if (box.getValue() instanceof FieldRef) {
                                    FieldRef ref = (FieldRef) box.getValue();
                                    SootField sootField = ref.getField();
                                    if (sootField.getSignature().startsWith("<androidx") || sootField.getSignature().startsWith("<android.support")) {
                                        usedFields.add(sootField);
                                    }
                                    else { //analyse super class
                                        analyseSupperClassFields(sootField, sm);
                                    }
                                }
                                if (box.getValue() instanceof InvokeExpr) {
                                    InvokeExpr invokeExpr = (InvokeExpr) box.getValue();
                                    SootMethod sootMethod = invokeExpr.getMethod();
                                    String signature = sootMethod.getSignature();
                                    if (signature.startsWith("<androidx") || signature.startsWith("<android.support")) {
                                        usedMethods.add(sootMethod);
                                    }
                                    else { //analyse super class
                                        analyseSupperClassMethods(sootMethod, sm);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        HashMap<String, MySootClass> classes = new HashMap<>();
        for (SootMethod method : usedMethods) {
            SootClass sootClass = method.getDeclaringClass();
            if (classes.containsKey(sootClass.getName())) {
                classes.get(sootClass.getName()).methods.add(method.getSignature());
            } else {
                MySootClass mySootClass = new MySootClass();
                mySootClass.name = sootClass.getName();
                classes.put(sootClass.getName(), mySootClass);
                mySootClass.methods.add(method.getSignature());
            }
        }
        for (SootField field : usedFields) {
            SootClass sootClass = field.getDeclaringClass();
            if (classes.containsKey(sootClass.getName())) {
                classes.get(sootClass.getName()).fields.add(field.getSignature());
            } else {
                MySootClass mySootClass = new MySootClass();
                mySootClass.name = sootClass.getName();
                classes.put(sootClass.getName(), mySootClass);
                mySootClass.methods.add(field.getSignature());
            }
        }
        //return values to list
        return new ArrayList<>(classes.values());
    }

}
