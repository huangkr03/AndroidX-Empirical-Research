package com.res;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.alibaba.fastjson.*;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;


public class StackOverFlowCollector {
    public static List<Question> getQuestions(String s) throws IOException {
        //String s = "https://api.stackexchange.com/2.3/questions?page=1&order=desc&sort=votes&tagged=java&site=stackoverflow";
        URL url = new URL(s);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        int responeseCode = conn.getResponseCode();
        String respomseMessage = conn.getResponseMessage();
        String contentEncoding = conn.getContentEncoding();
        System.out.println(responeseCode);
        System.out.println(respomseMessage);
        System.out.println(contentEncoding);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(conn.getInputStream())));
        String inputLine = null;
        while ((inputLine = in.readLine()) != null) {
            stringBuilder.append(inputLine);
        }
        in.close();

        JSONObject jsonObject = JSONObject.parseObject(stringBuilder.toString());
        JSONArray items = jsonObject.getJSONArray("items");
        ArrayList<Question> questions = new ArrayList<>();

        for (Object obj : items) {
            JSONObject json = (JSONObject) obj;
            JSONArray tags = json.getJSONArray("tags");
            List<String> s_tags = new ArrayList<>();
            StringBuilder ss = new StringBuilder();
            for (int i = 0; i < tags.size(); i++) {
                //ss.append(tags.getString(i)).append(", ");
                s_tags.add(tags.getString(i));
            }
            JSONArray answers = json.getJSONArray("answers");
            List<Answer> s_answers = new ArrayList<>();
            if (answers != null) {
                for (int i = 0; i < answers.size(); i++) {
                    JSONObject ans = answers.getJSONObject(i);
                    Answer a = new Answer(ans.getIntValue("answer_id"), ans.getBooleanValue("is_accepted"), ans.getString("body_markdown"));
                    s_answers.add(a);
                }
            }

            questions.add(new Question(json.getIntValue("question_id"),json.getString("title"),json.getIntValue("score"),
                    json.getIntValue("view_count"),s_tags,json.getIntValue("creation_date"), json.getString("body_markdown"), s_answers));
        }
        return questions;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Library library = new Library("androidx");
        for (int i = 1; i <= 25; i++) {
            String uri = "https://api.stackexchange.com/2.3/questions?page=" + i + "&pagesize=100&order=desc&sort=activity&tagged=androidx&site=stackoverflow&filter=!*Mg4PjfftzEy7OEn&key=6Y7wTBJ4eSJy9QHxrBBKIA((";
            try {
                List<Question> questions = getQuestions(uri);
                library.questions.addAll(questions);
                Thread.sleep(10000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        //write to file
        String json = JSON.toJSONString(library, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
        FileWriter fileWriter = new FileWriter("androidx_.json");
        fileWriter.write(json);
        fileWriter.close();
    }
}

class Library {
    @JSONField(name = "name", ordinal = 1)
    public String name;
    @JSONField(name = "questions", ordinal = 2)
    public List<Question> questions = new ArrayList<>();

    public Library(String name) {
        this.name = name;
    }

    public Library() {
    }
}

class Answer {
    @JSONField(name = "answer_id", ordinal = 1)
    public int id;
    @JSONField(name = "is_accepted", ordinal = 2)
    public boolean is_accepted;
    @JSONField(name = "answer_body", ordinal = 3)
    public String body;

    public Answer(int id, boolean is_accepted, String body) {
        this.id = id;
        this.is_accepted = is_accepted;
        this.body = body;
    }

    public Answer() {
    }
}

class Question {
    @JSONField(name = "question_id", ordinal = 1)
    public int id;
    @JSONField(name = "title", ordinal = 2)
    public String title;
    @JSONField(name = "score", ordinal = 3)
    public int score;
    @JSONField(name = "view_count", ordinal = 4)
    public int view_count;
    @JSONField(name = "tags", ordinal = 5)
    public List<String> tags;
    @JSONField(name = "createdyear", ordinal = 6)
    public int createdyear;
    @JSONField(name = "question_body", ordinal = 7)
    public String body;
    @JSONField(name = "answers", ordinal = 8)
    public List<Answer> answers = new ArrayList<>();

    public int year;

    public Question(int id, String title, int score, int view_count, List<String> tags, int createdyear, String body, List<Answer> answers) {
        this.id = id;
        this.title = title;
        this.score = score;
        this.view_count = view_count;
        this.tags = tags;
        this.createdyear = createdyear;
        this.body = body;
        this.answers = answers;
    }

    public Question() {
    }
}