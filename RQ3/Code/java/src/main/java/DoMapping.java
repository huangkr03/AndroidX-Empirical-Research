import java.io.*;
import java.util.*;

public class DoMapping {
    public static Map<String, Map<String, Integer>> androidx;
    public static Map<String, Map<String, Integer>> support;
    public static Set<String> supportMapping;
    public static Set<String> androidxMapping;
    public static final double THRESHOLD = 0.95;

    /**
     * Load mapping from androidx-class-mapping-similarity.csv
     */
    public static void loadMapping() {
        supportMapping = new HashSet<>();
        androidxMapping = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(Objects.requireNonNull(DoMapping.class.getResource("androidx-class-mapping-similarity.csv")).getFile()))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                String supportClass = split[0];
                String androidxClass = split[1];
                supportMapping.add(supportClass);
                androidxMapping.add(androidxClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load vector mapping from files
     *
     * @param files:   files to load
     * @param map:     map to store the vector
     * @param mapping: set to store the mapping
     */
    public static void loadVectorMapping(File[] files, Map<String, Map<String, Integer>> map, Set<String> mapping) {
        for (int i = 0; i < files.length; i++) {
            System.err.print("Progress: " + (i + 1) + "/" + files.length);
            // skip if already mapped
            if (mapping.contains(files[i].getName().replace(".txt", ""))) continue;
            File file = files[i];
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                map.put(file.getName().replace(".txt", ""), CosineSimilarity.getVector(sb.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.err.print("\r");
        }
        System.out.println("\n" + map.size() + " files loaded");
    }

    /**
     * Load androidx vector mapping
     */
    public static void loadAndroidx() {
        androidx = new HashMap<>();
        File resourceLoader = new File(Objects.requireNonNull(DoMapping.class.getResource("androidx")).getFile());
        File[] files = resourceLoader.listFiles();
        assert files != null;
        System.out.println("androidx files: " + files.length);
        loadVectorMapping(files, androidx, androidxMapping);
    }

    /**
     * Load support vector mapping
     */
    public static void loadSupport() {
        support = new HashMap<>();
        File resourceLoader = new File(Objects.requireNonNull(DoMapping.class.getResource("support")).getFile());
        File[] files = resourceLoader.listFiles();
        assert files != null;
        System.out.println("support files: " + files.length);
        loadVectorMapping(files, support, supportMapping);
    }

    /**
     * Do mapping
     */
    public static void doMapping() {
        loadMapping();
        loadSupport();
        loadAndroidx();
        Map<String, String> mapping = new HashMap<>();
        Map<String, String> tryMapping = new HashMap<>();
        Map<String, Double> score = new HashMap<>();
        System.out.println("Start mapping");
        int i = 0;
        for (String supportClass : support.keySet()) {
            System.err.print("Progress: " + i + " / " + support.size());
            Map<String, Integer> supportVector = support.get(supportClass);
            double maxSimilarity = 0;
            String maxAndroidxClass = "";
            for (String androidxClass : androidx.keySet()) {
                Map<String, Integer> androidxVector = androidx.get(androidxClass);
                double similarity = CosineSimilarity.calculateSimilarity(supportVector, androidxVector);
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    maxAndroidxClass = androidxClass;
                }
            }
            if (maxSimilarity > THRESHOLD) {
                mapping.put(supportClass, maxAndroidxClass);
            }
            tryMapping.put(supportClass, maxAndroidxClass);
            score.put(supportClass, Math.round(maxSimilarity * 10000.0) / 10000.0);

            System.err.print("\r");
            i++;
        }
        System.out.println("\n" + mapping.size() + " mappings found");

        // store mapping into csv file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("mapping.csv"))) {
            bw.write("support,androidx,similarity\n");
            for (String supportClass : mapping.keySet()) {
                bw.write(supportClass + "," + mapping.get(supportClass) + "," + score.get(supportClass) * 100 + "%");
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("tryMapping.csv"))) {
            bw.write("support,androidx,similarity\n");
            for (String supportClass : tryMapping.keySet()) {
                bw.write(supportClass + "," + tryMapping.get(supportClass) + "," + score.get(supportClass) * 100 + "%");
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        doMapping();
    }
}
