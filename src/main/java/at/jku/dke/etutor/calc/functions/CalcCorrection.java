package at.jku.dke.etutor.calc.functions;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class CalcCorrection {

    /**
     * Corrects a calc submission and returns feedback
     *
     * @param instruction workbook of the instruction
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return a string containing the feedback
     */
    public static String correctTask (XSSFWorkbook instruction, XSSFWorkbook solution, XSSFWorkbook submission) throws IOException {

        try {
            List<XSSFWorkbook> xssfWorkbookList = CreateRandomInstruction.overriteWorkbooks(instruction,solution,submission);
            Sheet sheet_solution = xssfWorkbookList.get(0).getSheetAt(1);
            Sheet sheet_submission = xssfWorkbookList.get(1).getSheetAt(1);

            if (!FillColorHex.isSheetUnchanged(sheet_solution, sheet_submission)) {
                return  "Your submission has syntax errors. Please do not change the colors of the cells!";
            }
            if (!Objects.equals(CorrectDropDown.correctDropDown(xssfWorkbookList.get(0), xssfWorkbookList.get(1), sheet_solution, sheet_submission),"Your Dropdown and the Values are correct !")) {
                return CorrectDropDown.correctDropDown(xssfWorkbookList.get(0), xssfWorkbookList.get(1), sheet_solution, sheet_submission);
            }
            if (!CorrectCalculations.isCorrectCalculated(xssfWorkbookList.get(0), xssfWorkbookList.get(1),sheet_solution, sheet_submission )) {
                return "Your calculated Values are not correct !";
            }
            if (!DataValidation.checkDataValidation(sheet_solution, sheet_submission)) {
                return "Please check your Data Validation";
            }
            if (!ProtectionCheck.correctProtected(sheet_solution, sheet_submission)) {
                return "Your Sheet is not correct protected";
            }
            if (!CorrectSheetFormat.isCorrectHidden(sheet_solution, sheet_submission)) {
                return "Your Row / Columns are not correct hidden";
            }
            if (!CorrectSheetFormat.isCorrectFormatted(sheet_solution, sheet_submission)) {
                return "Your Cells are not correct formated";
            }
            return "Congratulation! Your Submission is correct";


        }catch (Exception e) {
            return "Your submission has Syntax Errors, please Contact the Admin of your Program!";
        }
    }
}
