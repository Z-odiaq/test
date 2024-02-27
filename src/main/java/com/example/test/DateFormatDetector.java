package com.example.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DateFormatDetector {

    public static void main(String[] args) {
        List<String> dates = readDatesFromFile("dates.csv"); // Replace with your file path
        String dateFormat = detectDateFormat(dates);
        if (dateFormat == null) {
            dateFormat = manualDateFormatInput();
        }

        // Print the final date format
        System.out.println("Final Date Format: " + dateFormat);
    }

    // Function to read dates from CSV or JSON file
    private static List<String> readDatesFromFile(String filePath) {
        List<String> dates = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dates.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dates;
    }

    // Function to detect date format
    private static String detectDateFormat(List<String> dates) {
        for (String date : dates) {
            //seperate the date parts
            String[] dateParts = date.split("[/-]");
            if (dateParts[0].length() == 4) {
                // If the first part is 4 digits, assume it's the year
                //test whiich is month and which is day
                if (dateParts[1].length() <= 12 && dateParts[2].length() >= 12) {
                    return "yyyy-MM-dd";
                } else if (dateParts[2].length() <= 12 && dateParts[1].length() >= 12) {
                    return "yyyy-dd-MM";
                } else {
                    return null;
                }
            } else if (dateParts[2].length() == 4) {
                // If the last part is 4 digits, assume it's the year
                //test whiich is month and which is day
                if (dateParts[0].length() <= 12 && dateParts[1].length() >= 12) {
                    return "MM-DD-yyyy";
                } else if (dateParts[1].length() <= 12 && dateParts[0].length() >= 12) {
                    return "DD-MM-yyyy";
                } else {
                    return null;
                }
            } else {
                // If the first part is 2 digits, assume it's the day
                //test whiich is month and which is year
                if (dateParts[0].length() >= 31) {
                    if (dateParts[1].length() <= 12 && dateParts[2].length() >= 12) {
                        return "YY-MM-DD";
                    } else if (dateParts[2].length() <= 12 && dateParts[1].length() >= 12) {
                        return "YY-DD-MM";
                    } else {
                        return null;
                    }
                } else if (dateParts[1].length() >= 31) {
                    if (dateParts[0].length() <= 12 && dateParts[2].length() >= 12) {
                        return "MM-YY-DD";
                    } else if (dateParts[2].length() <= 12 && dateParts[0].length() >= 12) {
                        return "DD-YY-MM";
                    } else {
                        return null;
                    }
                } else if (dateParts[2].length() >= 31) {
                    if (dateParts[0].length() <= 12 && dateParts[1].length() >= 12) {
                        return "MM-DD-YY";
                    } else if (dateParts[1].length() <= 12 && dateParts[0].length() >= 12) {
                        return "MM-MDD-YY";
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
        return null; // Could not determine format
    }

    // Function to manually input date format from the user
    private static String manualDateFormatInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the date format (e.g., MM/dd/yyyy): ");
        return scanner.nextLine();
    }


}
