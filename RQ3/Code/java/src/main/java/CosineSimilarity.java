import java.io.*;
import java.net.URL;
import java.util.*;

public class CosineSimilarity {
    public static double calculateSimilarity(Map<String, Integer> vector1, Map<String, Integer> vector2) {
        Set<String> words = new HashSet<>();
        words.addAll(vector1.keySet());
        words.addAll(vector2.keySet());
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (String word : words) {
            int count1 = vector1.getOrDefault(word, 0);
            int count2 = vector2.getOrDefault(word, 0);
            dotProduct += count1 * count2;
            norm1 += count1 * count1;
            norm2 += count2 * count2;
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    public static double calculateSimilarity(String code1, String code2) throws IOException {
        // write to file aa.txt and bb.txt
        BufferedWriter writer1 = new BufferedWriter(new FileWriter("aa.txt"));
        BufferedWriter writer2 = new BufferedWriter(new FileWriter("bb.txt"));

        writer1.write(code1);
        writer2.write(code2);
        writer1.close();
        writer2.close();
        Map<String, Integer> vector1 = getVector(code1);
        Map<String, Integer> vector2 = getVector(code2);
        return calculateSimilarity(vector1, vector2);
    }

    public static Map<String, Integer> getVector(String code) {
        code = code.replaceAll("'", "");
        // remove package names
        String regex = "(\\w+\\.)+(\\w+)";
        code = code.replaceAll(regex, "$2");

        Map<String, Integer> vector = new HashMap<>();
        String[] words = code.split("[^\\w$@]+"); // split by non-word characters except $, @
        for (String word : words) {
            // ignore words that start with $, @, or a letter followed by digits
            if (word.startsWith("$") || word.startsWith("@") || word.startsWith("label") || word.matches("[a-zA-Z]\\d+$")) continue;
            if (word.length() > 0) vector.put(word, vector.getOrDefault(word, 0) + 1);
        }
        return vector;
    }

    public static double findSimilarity(String path1, String path2) throws IOException{
        StringBuilder s1 = new StringBuilder();
        StringBuilder s2 = new StringBuilder();
        URL u1 = CosineSimilarity.class.getResource(path1);
        URL u2 = CosineSimilarity.class.getResource(path2);
        // check if two files exist
        if (u1 == null || u2 == null) return -1;
        BufferedReader reader1 = new BufferedReader(new FileReader(u1.getFile()));
        BufferedReader reader2 = new BufferedReader(new FileReader(u2.getFile()));
        reader1.lines().forEach(s -> s1.append(s).append("\n"));
        reader2.lines().forEach(s -> s2.append(s).append("\n"));
        reader1.close();
        reader2.close();

        // write to file a.txt and b.txt
        BufferedWriter writer1 = new BufferedWriter(new FileWriter("a.txt"));
        BufferedWriter writer2 = new BufferedWriter(new FileWriter("b.txt"));

        writer1.write(s1.toString());
        writer2.write(s2.toString());
        writer1.close();
        writer2.close();

        return calculateSimilarity(s1.toString(), s2.toString());
    }

    public static void checkMappingTable() throws IOException {
        // read androidx-class-mapping.csv
        BufferedReader reader = new BufferedReader(new FileReader(Objects.requireNonNull(CosineSimilarity.class.getResource("androidx-class-mapping.csv")).getFile()));
        String headLine = reader.readLine(); // skip the first line
        headLine += ",similarity";
        String line;
        ArrayList<String> lines = new ArrayList<>();
        lines.add(headLine);
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",");
            // skip the line if the first token does not start with "android.support" or the second token does not start with "androidx"
//            if (!tokens[0].startsWith("android.support") || !tokens[1].startsWith("androidx")) continue;
            if (!tokens[0].startsWith("android.support")) continue;
            String support = tokens[0];
            String x = tokens[1];
            String path1 = String.format("support/%s.txt", support);
            String path2 = String.format("androidx/%s.txt", x);
            String path3 = String.format("support/%s.txt", x);
            double similarity = findSimilarity(path1, path2);
            if (similarity == -1) similarity = findSimilarity(path1, path3);
            // 保留4位小数
            similarity = (double) Math.round(similarity * 10000) / 10000;
            line += "," + similarity;
            lines.add(line);
//            System.out.println("The cosine similarity between the two Java classes is " + similarity);
        }
        reader.close();

        // write to androidx-class-mapping-similarity.csv
        BufferedWriter writer = new BufferedWriter(new FileWriter("androidx-class-mapping-similarity.csv"));
        for (String l : lines) {
            writer.write(l);
            writer.newLine();
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        String support = "android.support.graphics.drawable.AndroidResources";
        String x = "androidx.vectordrawable.graphics.drawable.AndroidResources";
        String path1 = String.format("support/%s.txt", support);
        String path2 = String.format("androidx/%s.txt", x);
        double similarity = findSimilarity(path1, path2);
        System.out.println("The cosine similarity between the two Java classes is " + similarity);
//        checkMappingTable();
    }
}
