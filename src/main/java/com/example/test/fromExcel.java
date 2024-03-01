package com.example.test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class fromExcel {

    public static void main(String[] args) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
            JsonNode regexRoot = objectMapper.readTree(new File("C:/Users/moham/IdeaProjects/test/src/main/java/com/example/test/regex.json"));
            List<String> dateExamples = readLatestStartDatesFromExcel("C:/Users/moham/IdeaProjects/test/src/main/java/com/example/test/dates.xlsx");
            Map<String, String> datePatternsMap = readRegexFromJson(regexRoot);


            Map<String, String> results = detectDateFormatWithHashMap(dateExamples, datePatternsMap);

            for (Map.Entry<String, String> entry : results.entrySet()) {
                System.out.println("With HashMap | Input: " + entry.getKey() + " | Detected Format: " + entry.getValue());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> readLatestStartDatesFromExcel(String filePath) throws IOException {
        FileInputStream excelFile = new FileInputStream(filePath);
        XSSFWorkbook  workbook = null;
        try {
            workbook = new XSSFWorkbook(excelFile ); // Adjust row batch size as needed
        } catch (IOException e) {
            throw e;
        } finally {
            if (workbook != null) {
                try {
                    workbook.close(); // Close the workbook to release resources
                } catch (IOException e) {
                    e.printStackTrace(); // Log closing error but don't throw
                }
            }
        }

        Sheet sheet = workbook.getSheetAt(0);

        // Find the row with "Latest Start Date" header
        int headerRowIndex = -1;
        for (int i = 0; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(0); // Check the first cell for header
                if (cell != null && cell.getStringCellValue().equalsIgnoreCase("Latest Start Date")) {
                    headerRowIndex = i;
                    break;
                }
            }
        }
        int count = 0;
        // Collect dates from the "Latest Start Date" column (assuming column A)
        List<String> latestStartDates = new ArrayList<>();
        if (headerRowIndex != -1) {
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) { // Start from the row after the header
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(0); // Get the cell from column A
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                latestStartDates.add(cell.getStringCellValue());
                                count++;
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    Date dateValue = cell.getDateCellValue();
                                    latestStartDates.add(dateValue.toString()); // Convert date to string
                                    count++;
                                } else {
                                    latestStartDates.add(String.valueOf(cell.getNumericCellValue()));
                                    count++;
                                }
                                break;
                            default:
                                System.out.println("Unsupported cell type: " + cell.getCellType());
                        }
                    }
                }
            }
        } else {
            System.out.println("Header 'Latest Start Date' not found in the Excel file.");
        }
        System.out.println("Total dates read: " + count);
        return latestStartDates;
    }


    private static Map<String, String> readRegexFromJson(JsonNode regexRoot) {
        Map<String, String> datePatternsMap = new HashMap<>();
        for (JsonNode regexNode : regexRoot) {
            datePatternsMap.put(regexNode.get("regex").asText(), regexNode.get("pattern").asText());
            System.out.println("Regex loaded: " + regexNode.get("regex").asText() + " | Pattern: " + regexNode.get("pattern").asText());
        }
        return datePatternsMap;
    }

    private static Map<String, String> detectDateFormatWithHashMap(List<String> dateExamples, Map<String, String> datePatternsMap) {
        Map<String, String> results = new HashMap<>();

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
