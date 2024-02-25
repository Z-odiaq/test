package com.example.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class DateFormatDetectorComparison {

    public static void main(String[] args) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(new File("C:/Users/moham/IdeaProjects/test/src/main/java/com/example/test/dates.json"));

            List<String> dateExamples = new ArrayList<>();
            for (JsonNode dateNode : root) {
                dateExamples.add(dateNode.asText());
            }

            long startTimeWithHashMap = System.currentTimeMillis();
            Map<String, String> results = detectDateFormatWithHashMap(dateExamples);
            long endTimeWithHashMap = System.currentTimeMillis();

            for (Map.Entry<String, String> entry : results.entrySet()) {
                System.out.println("With HashMap | Input: " + entry.getKey() + " | Detected Format: " + entry.getValue());
            }

            System.out.println("Execution time with HashMap: " + (endTimeWithHashMap - startTimeWithHashMap) + " milliseconds");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> detectDateFormatWithHashMap(List<String> dateExamples) {
        Map<String, String> results = new HashMap<>();
        Map<String, String> datePatternsMap = new HashMap<>();
        datePatternsMap.put("\\d{4}-\\d{2}-\\d{2}", "yyyy-MM-dd");
        datePatternsMap.put("\\d{2}/\\d{2}/\\d{4}", "MM/dd/yyyy");
        datePatternsMap.put("\\d{2}-\\d{2}-\\d{4}", "MM-dd-yyyy");
        datePatternsMap.put("\\d{4}/\\d{2}/\\d{2}", "yyyy/MM/dd");
        datePatternsMap.put("\\d{2}\\.\\d{2}\\.\\d{4}", "MM.dd.yyyy");
        datePatternsMap.put("\\d{4}\\.\\d{2}\\.\\d{2}", "yyyy.MM.dd");
        datePatternsMap.put("\\d{2}-(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4}", "dd-MMM-yyyy");
        datePatternsMap.put("\\d{2}-(?:January|February|March|April|May|June|July|August|September|October|November|December)-\\d{4}", "dd-MMMMM-yyyy");
        datePatternsMap.put("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}", "yyyy-MM-dd HH:mm:ss");

        for (String input : dateExamples) {
            for (Map.Entry<String, String> entry : datePatternsMap.entrySet()) {
                Pattern regex = Pattern.compile(entry.getKey());
                Matcher matcher = regex.matcher(input);
                if (matcher.matches()) {
                    results.put(input, entry.getValue());
                    break;
                }
            }
        }

        return results;
    }


}
