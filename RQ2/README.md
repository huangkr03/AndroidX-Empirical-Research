# RQ2

## About Analyzer

Analyzer is used to extract APK utilized APIs, to run the code, you should install Maven and install necessary dependencies. And the program entry is in `MainExtractor.java`, you should package the whole program into an executable jar file and use command `java -jar executable.jar <android.jar-directory> <apk-path> <output-directory>` to run. 

## About Data

Because the limit space of github upload, we cannot upload all our apk dataset, so instead we just upload all apk id, see `F-Droid_apk.txt` and `Gpaly_apk.txt`. And the file `data\Most_Frequently_Used_APIs.txt` is our result about most frequently-utilized AndroidX classes by taxon, with average number of utilized classes per apk.