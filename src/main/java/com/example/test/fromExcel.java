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
    static String datesField = "";
    static String datesFile = "";
    static String dateFormat = "";
    public static void main(String[] args) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
            JsonNode regexRoot = objectMapper.readTree(new File("C:/Users/moham/IdeaProjects/test/src/main/java/com/example/test/config.json"));
            Map<String, String> datePatternsMap = readRegexFromJson(regexRoot);
            List<String> dateExamples = readLatestStartDatesFromExcel(datesFile);



            Map<String, String> results = detectDateFormatWithHashMap(dateExamples, datePatternsMap);

           // for (Map.Entry<String, String> entry : results.entrySet()) {
            //    System.out.println("Input: " + entry.getKey() + " | Detected Format: " + entry.getValue());
            //}

        } catch (IOException e) {
            System.out.println("Error reading JSON file: " + e.getMessage());
        }
    }

    private static boolean isJSONValid(String test) {
        try {
            new ObjectMapper().readTree(test);
            return true;
        } catch (IOException e) {
            return false;
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
                    System.out.println("Error closing workbook: " + e.getMessage());
                     // Log closing error but don't throw
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
                //cell not blank and cell type not equal to blank and cell value equal to "Latest Start Date"
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
                            case BLANK:

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
        if (!isJSONValid(regexRoot.toString())) {
            throw new IllegalArgumentException("Root node must be an array");
        }
        Map<String, String> datePatternsMap = new HashMap<>();
        for (JsonNode regexNode : regexRoot) {
            if (regexNode.has("datesFieldName") && regexNode.has("datesFile") && regexNode.has("dateFormat")) {
                datesField = regexNode.get("datesFieldName").asText();
                 datesFile = regexNode.get("datesFile").asText();
                 dateFormat = regexNode.get("dateFormat").asText();
                System.out.println("Dates Field Name: " + datesField + "\nDates File: " + datesFile + "\nDate Format: " + dateFormat);
            } else if (regexNode.has("regex") ) {
                datePatternsMap.put(regexNode.get("pattern").asText(), regexNode.get("regex").asText());
                System.out.println("Regex loaded: " + regexNode.get("regex").asText() + " | Pattern: " + regexNode.get("pattern").asText());            }

        }
        return datePatternsMap;
    }

    private static Map<String, String> detectDateFormatWithHashMap(List<String> dateExamples, Map<String, String> datePatternsMap) {
        Map<String, String> results = new HashMap<>();

        for (String input : dateExamples) {
            for (Map.Entry<String, String> entry : datePatternsMap.entrySet()) {
                Pattern regex = Pattern.compile(entry.getValue());
                Matcher matcher = regex.matcher(input);

                if (matcher.matches()) {
                    //change any delimiter like , ; :  / to -
                    input = input.replaceAll("[,;:\\/_.*]", "-");
                    String patt = entry.getKey().replaceAll("[,;:\\/_.*]", "-");
                    validateDatePattern(input, patt);
                    results.put(input, entry.getKey());
                    System.out.println("Input: " + input + " | Detected Format: " + entry.getKey());

                }else {
                    results.put(input, "Unmatched");
                }

            }
        }

        return results;
    }
    private static void validateDatePattern(String input, String pattern) {
        Boolean d,m,y = false;

        if (pattern.split("-")[0] == "dd" || pattern.split("-")[0] == "d"){
            if (Integer.parseInt(input.split("-")[0]) < 31 && Integer.parseInt(input.split("-")[0]) > 12) {
                d = true;
            }else {
                d = false;
            }
        } else if (pattern.split("-")[0] == "MM" || pattern.split("-")[0] == "M"){
            if (Integer.parseInt(input.split("-")[0]) < 12 ) {
                m = true;
            } else {
                m = false;
            }
        } else if (pattern.split("-")[0] == "yyyy" || pattern.split("-")[0] == "yy" || pattern.split("-")[0] == "Y"){
            if (Integer.parseInt(input.split("-")[0]) < 1000) {
                System.out.println("Invalid date pattern: " + pattern);
            }
        }
    }
}
