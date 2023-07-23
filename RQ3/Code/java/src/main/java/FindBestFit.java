import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class FindBestFit {
    public static Map<String, Map<String, Integer>> androidx;
    public static Set<String> androidxMapping;

    /**
     * Load mapping from androidx-class-mapping-similarity.csv
     */
    public static void loadMapping() {
        androidxMapping = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(Objects.requireNonNull(DoMapping.class.getResource("androidx-class-mapping-similarity.csv")).getFile()))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                String androidxClass = split[1];
                androidxMapping.add(androidxClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void loadAndroidx() {
        androidx = new HashMap<>();
        File resourceLoader = new File(Objects.requireNonNull(DoMapping.class.getResource("androidx")).getFile());
        File[] files = resourceLoader.listFiles();
        assert files != null;
        System.out.println("androidx files: " + files.length);
        DoMapping.loadVectorMapping(files, androidx, androidxMapping);
    }

    public static void findBestFit(String support) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(FindBestFit.class.getResource("support/" + support + ".txt").getFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Integer> supportVector = CosineSimilarity.getVector(sb.toString());
        double max = 0;
        String bestFit = "";
        for (String androidxClass : androidx.keySet()) {
            double similarity = CosineSimilarity.calculateSimilarity(supportVector, androidx.get(androidxClass));
            if (similarity > max) {
                max = similarity;
                bestFit = androidxClass;
            }
        }
        System.out.println("Best fit: " + bestFit + " with similarity: " + max);
    }

    public static void main(String[] args) {
        loadMapping();
        loadAndroidx();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String support = scanner.next();
            findBestFit(support);
        }
    }
}
