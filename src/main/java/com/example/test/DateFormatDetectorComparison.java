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


    private static final String REGEX = "(\\d{1,2}|\\d{4})[/-]?(\\d{1,2})[/-]?(\\d{1,2}|\\d{4})";

    public static String extractDateParts(String dateString) {
        Matcher matcher = Pattern.compile(REGEX).matcher(dateString);
        if (matcher.matches()) {
            String first = matcher.group(1);
            String second = matcher.group(2);
            String third = matcher.group(3);

            // sum the number of digits in the year day and month
            int sum = first.length() + second.length() + third.length();
            if (sum == 8) {
                // test if the first number is the year
                if (first.length() == 4 && isValidYear(first)) {
                    if (isValidDay(second) && isValidMonth(third) ) {
                        return second + "-" + third + "-" + first;
                    } else  if (isValidDay(third) && isValidMonth(second) ) {
                        return third + "-" + second + "-" + first;
                    } else {
                        return null;
                    }

                } else {
                    return null;
                }
            } else if (sum == 6) {
                if (isValidDay(first) && isValidMonth(second) && isValidYear(third)) {
                    return first + "-" + second + "-" + third;
                } else if (isValidDay(second) && isValidMonth(first) && isValidYear(third)) {
                    return second + "-" + first + "-" + third;
                } else if (isValidDay(third) && isValidMonth(first) && isValidYear(second)) {
                    return third + "-" + first + "-" + second;
                } else {
                    return null;
                }
            } else if (sum == 4) {
                // Validation failed, return null
                return null;
            } else {
                // Validation failed, return null
                return null;
            }

            if (isValidDay(day) && isValidMonth(month) && isValidYear(year)) {
                return new String[]{day, month, year};
            } else {
                // Validation failed, return null
                return null;
            }
        } else {
            // No match, return null
            return null;
        }
    }


    private static Map<String, String> detectDateFormatWithHashMap(List<String> dateExamples) {
        Map<String, String> results = new HashMap<>();
        Map<String, String> datePatternsMap = new HashMap<>();
        datePatternsMap.put("\\d{1,2,4}-\\d{1,2,4}-\\d{1,2,4}", "yyyy-MM-dd");
        datePatternsMap.put("\\d{2}/\\d{2}/\\d{4}", "MM/dd/yyyy");
        datePatternsMap.put("\\d{2}-\\d{2}-\\d{4}", "MM-dd-yyyy");
        datePatternsMap.put("\\d{4}/\\d{2}/\\d{2}", "yyyy/MM/dd");
        datePatternsMap.put("\\d{2}\\.\\d{2}\\.\\d{4}", "MM.dd.yyyy");
        datePatternsMap.put("\\d{4}\\.\\d{2}\\.\\d{2}", "yyyy.MM.dd");
        datePatternsMap.put("\\d{2}\\.\\d{2}\\.\\d{2}", "dd.MM.yy");
        datePatternsMap.put("\\d{1}\\.\\d{1}\\.\\d{4}", "d.M.yyyy");
        datePatternsMap.put("\\d{1}\\.\\d{1}\\.\\d{2}", "d.M.yy");
        datePatternsMap.put("\\d{1}\\.\\d{1}\\.\\d{1}", "d.M.y");
        datePatternsMap.put("\\d{1}\\.\\d{2}\\.\\d{2}", "d.MM.yy");
        datePatternsMap.put("\\d{2}-(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{4}", "dd-MMM-yyyy");
        datePatternsMap.put("\\d{2}-(?:January|February|March|April|May|June|July|August|September|October|November|December)-\\d{4}", "dd-MMMMM-yyyy");
        datePatternsMap.put("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}", "yyyy-MM-dd HH:mm:ss");
        String regex = "^(?:(?:(?:(?:0?[1-9]|1[0-2])[-/])?(?:0?[1-9]|[12][0-9]|3[01])|(?:(?:0?[13-9]|1[0-2])[-/](?:0?[1-9]|[12][0-9]|30)|(?:0?[13578]|1[02])[-/](?:0?[1-9]|[12][0-9]|3[01])|(?:0?[2469]|11)[-/](?:0?[1-9]|[12][0-9]|30))|(?:(?:19|20)\\d\\d[-/](?:0?[1-9]|1[0-2])|(?:19|20)\\d\\d[-/](?:0?[1-9]|[12][0-9]|3[01])|(?:(?:0?[1-9]|1[0-2])[-/])?(?:0?[1-9]|[12][0-9]|3[01]))))$";

        for (String input : dateExamples) {
            for (Map.Entry<String, String> entry : datePatternsMap.entrySet()) {
                Pattern regex = Pattern.compile(entry.getKey());
                Matcher matcher = regex.matcher(input);
                if (matcher.matches()) {

                    //test which one is the month and which one is the day
                    String[] dateParts = input.split("[^0-9]");
                    String month = dateParts[1];
                    String day = dateParts[0];
                    if (Integer.parseInt(month) > 12) {
                        month = dateParts[0];
                        day = dateParts[1];
                    }
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(entry.getValue());
                    try {
                        Date date = simpleDateFormat.parse(input);
                        if (simpleDateFormat.format(date).equals(input)) {
                            results.put(input, entry.getValue());
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    results.put(input, entry.getValue());
                    break;
                }
            }
        }

        return results;
    }

    private static boolean isValidDay(String day) {
        return Integer.parseInt(day) >= 1 && Integer.parseInt(day) <= 31;
    }

    private static boolean isValidMonth(String month) {
        return Integer.parseInt(month) >= 1 && Integer.parseInt(month) <= 12;
    }

    private static boolean isValidYear(String year) {
        return year.length() == 4;
    }
}
