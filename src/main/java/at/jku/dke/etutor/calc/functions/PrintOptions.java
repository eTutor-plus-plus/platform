package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.jena.base.Sys;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PrintOptions extends CorrectnessRule {


    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return the Feedback regarding the correct print options
     */
    @Override
    public Feedback checkCorrectness(XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        return correctPrintOptions(solution, submission);
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return Feedback if the print options of the solution and submission are the same (regarding the functions above)
     */
    public static Feedback correctPrintOptions (XSSFWorkbook solution, XSSFWorkbook submission) {
        if (!correctPrintArea(solution, submission)) {
            return new Feedback(false, "The print area of your submission is not correct!");
        }
        if (!correctRepeatingColumns(solution, submission)) {
            return new Feedback(false, "The repeating columns of your submission are not correct!");
        }
        if (!correctRepeatingRows(solution, submission)) {
            return new Feedback(false, "The repeating rows of your submission are not correct!");
        }
        return new Feedback(true, null);
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return true if the print area of solution and submission are (+- 5 rows and columns) the same
     */
    public static boolean correctPrintArea (XSSFWorkbook solution, XSSFWorkbook submission) {
        int numberOfSheetsSolution = solution.getNumberOfSheets();
        for (int i = 0; i < numberOfSheetsSolution; i++) {
            if (solution.getPrintArea(i) != null) {
                if (submission.getPrintArea(i) != null) {

                    // it should be correct if the print area is 5 +- rows and columns the same as the solution

                    // splits the print area of the solution into each row and column
                    String printAreaSolution = solution.getPrintArea(i);
                    String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
                    List<String> alphabet_list = new ArrayList<>(Arrays.asList(alphabet));
                    String pattern = "([\\s\\S]+)!\\$(\\D)\\$(\\d*):\\$(\\D)\\$(\\d*)";
                    // fails if the name of the sheet is renamed
                    String sheet_name_solution = printAreaSolution.replaceAll(pattern, "$1");
                    int v1_solution = alphabet_list.indexOf(printAreaSolution.replaceAll(pattern, "$2"));
                    int n1_solution = Integer.parseInt(printAreaSolution.replaceAll(pattern, "$3"));
                    int v2_solution = alphabet_list.indexOf(printAreaSolution.replaceAll(pattern, "$4"));
                    int n2_solution = Integer.parseInt(printAreaSolution.replaceAll(pattern, "$5"));


                    // splits the print area of the submission into each row and column
                    String printAreaSubmission = submission.getPrintArea(i);
                    String sheet_name_submission = printAreaSubmission.replaceAll(pattern, "$1");
                    int v1_submission = alphabet_list.indexOf(printAreaSubmission.replaceAll(pattern, "$2"));
                    int n1_submission = Integer.parseInt(printAreaSubmission.replaceAll(pattern, "$3"));
                    int v2_submission = alphabet_list.indexOf(printAreaSubmission.replaceAll(pattern, "$4"));
                    int n2_submission = Integer.parseInt(printAreaSubmission.replaceAll(pattern, "$5"));

                    // check the first Variable (A) smaller
                    if (v1_solution > 5 && v1_solution - v1_submission > 5) {
                        return false;
                    }
                    // check the first Variable (A) bigger
                    else if (v1_solution < 20 && v1_submission - v1_solution > 5) {
                        return false;
                    }
                    // check the second Variable (A) smaller
                    else if (v2_solution > 5 && v2_solution - v2_submission > 5) {
                        return false;
                    }
                    // check the second Variable (A) bigger
                    else if (v2_solution < 20 && v2_submission - v2_solution > 5) {
                        return false;
                    }
                    // check the first number
                    else if (Math.abs(n1_solution - n1_submission) > 5) {
                        return false;
                    }
                    // check the second number
                    else if (Math.abs(n2_solution - n2_submission) > 5) {
                        return false;
                    }


                } else return false;
            }
        }
        return true;
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return true if the repeating rows of solution and submission are the same
     */
    public static boolean correctRepeatingRows (XSSFWorkbook solution, XSSFWorkbook submission) {
        for (int i = 0; i < solution.getNumberOfSheets(); i++) {
            if (solution.getSheetAt(i).getRepeatingRows() != null) {
                if (submission.getSheetAt(i).getRepeatingRows() != null) {
                    if (!solution.getSheetAt(i).getRepeatingRows().toString().equals(submission.getSheetAt(i).getRepeatingRows().toString())) {
                        return false;
                    }
                }
                else return false;
            }

        }
        return true;
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return true if the repeating columns of solution and submission are the same
     */
    public static boolean correctRepeatingColumns (XSSFWorkbook solution, XSSFWorkbook submission) {
        for (int i = 0; i < solution.getNumberOfSheets(); i++) {
            if (solution.getSheetAt(i).getRepeatingColumns() != null) {
                if (submission.getSheetAt(i).getRepeatingColumns() != null) {
                    if (!solution.getSheetAt(i).getRepeatingColumns().toString().equals(submission.getSheetAt(i).getRepeatingColumns().toString())) {
                        return false;
                    }
                }
                else return false;
            }

        }
        return true;
    }
}
