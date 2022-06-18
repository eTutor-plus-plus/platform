package at.jku.dke.etutor.calc.applications;

import at.jku.dke.etutor.calc.functions.*;
import org.apache.jena.base.Sys;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CalcCorrectionApplication {

    private static final String INSTRUCTION = "src/main/resources/calc/instruction_n.xlsx";
    private static final String INSTRUCTION_NEW = "src/main/resources/calc/instruction_new.xlsx";
    private static final String SOLUTION_NEW = "src/main/resources/calc/solution_new.xlsx";
    private static final String SUBMISSION_NEW = "src/main/resources/calc/submission_new.xlsx";
    private static final String INSTRUCTION_K11827238 = "src/main/resources/calc/instruction_K11827238.xlsx";
    private static final String SUBMISSION_K11827238 = "src/main/resources/calc/submission_K11827238.xlsx";
    private static final String SOLUTION = "src/main/resources/calc/solution_n.xlsx";
    private static final String COLORS = "src/main/resources/calc/colors.xlsx";

    public static void main(String[] args) throws Exception {
//
//        FileInputStream excelFile_instruction = new FileInputStream(new File(INSTRUCTION_NEW));
//        XSSFWorkbook workbook_instruction = new XSSFWorkbook(excelFile_instruction);
//
//        FileInputStream excelFile_solution = new FileInputStream(new File(SOLUTION_NEW));
//        XSSFWorkbook workbook_solution= new XSSFWorkbook(excelFile_solution);
//
//        FileInputStream excelFile_submission = new FileInputStream(new File(SUBMISSION_NEW));
//        XSSFWorkbook workbook_submission = new XSSFWorkbook(excelFile_submission);
//
//        Sheet sheet = workbook_instruction.getSheetAt(0);
//
//        System.out.println(correctTask(workbook_instruction,workbook_solution,workbook_submission));




    }



    /**
     * @param instruction Workbook of the created instruction of a student (submission should have the same source)
     * @param solution Workbook of the solution
     * @param submission Workbook of the submission of a student (source should be identical to the instruction file)
     *                   Method Description:
     *                   return the error code of the first sheet in the workbook
     */
    public static String correctTask (XSSFWorkbook instruction, XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {


        try {
            List<XSSFWorkbook> xssfWorkbookList = CreateRandomInstruction.overrideWorkbooks(instruction,solution,submission);
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
