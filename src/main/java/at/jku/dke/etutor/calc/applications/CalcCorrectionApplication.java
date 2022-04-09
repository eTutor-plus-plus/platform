package at.jku.dke.etutor.calc.applications;

import at.jku.dke.etutor.calc.functions.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class CalcCorrectionApplication {

    private static final String INSTRUCTION = "src/main/resources/calc/instruction_n.xlsx";
    private static final String INSTRUCTION_K11827238 = "src/main/resources/calc/instruction_K11827238.xlsx";
    private static final String SUBMISSION_K11827238 = "src/main/resources/calc/submission_K11827238.xlsx";
    private static final String SOLUTION = "src/main/resources/calc/solution_n.xlsx";

    public static void main(String[] args) throws Exception {

        FileInputStream excelFile_instruction = new FileInputStream(new File(INSTRUCTION));
        XSSFWorkbook workbook_instruction = new XSSFWorkbook(excelFile_instruction);

//        CreateRandomInstruction.createRandomInstruction(workbook_instruction, "instruction_K11827238.xlsx");

        runCorrection(INSTRUCTION_K11827238,SOLUTION,SUBMISSION_K11827238);



//        FileInputStream excelFile_instruction = new FileInputStream(new File(INSTRUCTION));
//        XSSFWorkbook workbook_instruction = new XSSFWorkbook(excelFile_instruction);
//
//        FileInputStream excelFile_submission = new FileInputStream(new File(SUBMISSION));
//        XSSFWorkbook workbook_submission = new XSSFWorkbook(excelFile_submission);
//
//        FileInputStream excelFile_solution = new FileInputStream(new File(SOLUTION));
//        XSSFWorkbook workbook_solution = new XSSFWorkbook(excelFile_solution);
//
//        List<XSSFWorkbook> xssfWorkbookList = CreateRandomInstruction.randomiseInstructionValues(workbook_instruction,workbook_submission,workbook_solution);
//
//        String feedback = CorrectDropDown.correctDropDown(xssfWorkbookList.get(1), xssfWorkbookList.get(2), xssfWorkbookList.get(1).getSheetAt(6), xssfWorkbookList.get(2).getSheetAt(6));
//
//        System.out.println(feedback);
//
//        boolean coorectCalculation = CorrectCalculations.isCorrectCalculated(xssfWorkbookList.get(1), xssfWorkbookList.get(2), xssfWorkbookList.get(1).getSheetAt(6), xssfWorkbookList.get(2).getSheetAt(6));
//
//        if(coorectCalculation){
//            System.out.println("Your calculated Values are correct !");
//        }else {
//            System.out.println("Your calculated Values are not correct !");
//        }
//
//        if (!ProtectionCheck.correctProtected(xssfWorkbookList.get(1).getSheetAt(6), xssfWorkbookList.get(2).getSheetAt(6))) {
//            System.out.println("Your Sheet is not correct protected");
//        }
//
//        if (!DataValidation.checkDataValidation(xssfWorkbookList.get(1).getSheetAt(6), xssfWorkbookList.get(2).getSheetAt(6))) {
//            System.out.println("Plaese check your Data Validation");
//        }
//
//        if (!CorrectCellFormat.isCorrectHidden(xssfWorkbookList.get(1).getSheetAt(6), xssfWorkbookList.get(2).getSheetAt(6))) {
//            System.out.println("Your Row / Colums are not correct hidden");
//        }
//
//        if (!CorrectCellFormat.isCorrectFormated(xssfWorkbookList.get(1).getSheetAt(6), xssfWorkbookList.get(2).getSheetAt(6))) {
//            System.out.println("Your Cells are not correct formated");
//        }

    }

    public static void runCorrection (String instruction, String solution, String submission) throws Exception {

        FileInputStream excelFile_instruction = new FileInputStream(new File(instruction));
        XSSFWorkbook workbook_instruction = new XSSFWorkbook(excelFile_instruction);
//        XSSFWorkbook workbook_instruction = CreateRandomInstruction.createRandomInstruction(workbook_ins, "testing.xlsx");

        FileInputStream excelFile_solution = new FileInputStream(new File(solution));
        XSSFWorkbook workbook_solution = new XSSFWorkbook(excelFile_solution);

        FileInputStream excelFile_submission = new FileInputStream(new File(submission));
        XSSFWorkbook workbook_submission = new XSSFWorkbook(excelFile_submission);



//        List<XSSFWorkbook> xssfWorkbookList = CreateRandomInstruction.overriteWorkbooks(workbook_instruction,workbook_solution,workbook_submission, "testing.xlsx");
        List<XSSFWorkbook> xssfWorkbookList = CreateRandomInstruction.overriteWorkbooks(workbook_instruction,workbook_solution,workbook_submission);


        // TODO: iterates through all sheets except the first sheet because it is the Hilftabelle most of the time
        int sheet_counter = 1;

        while (sheet_counter < workbook_solution.getNumberOfSheets()) {

            String correctDropDown = CorrectDropDown.correctDropDown(xssfWorkbookList.get(0), xssfWorkbookList.get(1), xssfWorkbookList.get(0).getSheetAt(sheet_counter), xssfWorkbookList.get(1).getSheetAt(sheet_counter));


            boolean correctCalculation = CorrectCalculations.isCorrectCalculated(xssfWorkbookList.get(0), xssfWorkbookList.get(1), xssfWorkbookList.get(0).getSheetAt(sheet_counter), xssfWorkbookList.get(1).getSheetAt(sheet_counter));
            boolean correctProtected = ProtectionCheck.correctProtected(xssfWorkbookList.get(0).getSheetAt(sheet_counter), xssfWorkbookList.get(1).getSheetAt(sheet_counter));
            boolean correctDataValidation = DataValidation.checkDataValidation(xssfWorkbookList.get(0).getSheetAt(sheet_counter), xssfWorkbookList.get(1).getSheetAt(sheet_counter));
            boolean correctHidden = CorrectCellFormat.isCorrectHidden(xssfWorkbookList.get(0).getSheetAt(sheet_counter), xssfWorkbookList.get(1).getSheetAt(sheet_counter));
            boolean correctFormated = CorrectCellFormat.isCorrectFormated(xssfWorkbookList.get(0).getSheetAt(sheet_counter), xssfWorkbookList.get(1).getSheetAt(sheet_counter));

            if (!Objects.equals(correctDropDown, "Your Dropdown and the Values are correct !")) {
                System.out.println(workbook_solution.getSheetName(sheet_counter) + ": " + correctDropDown);
            }

            if(!correctCalculation){
                System.out.println(workbook_solution.getSheetName(sheet_counter) + ": Your calculated Values are not correct !");
            }

            if (!correctProtected) {
                System.out.println(workbook_solution.getSheetName(sheet_counter) + ": Your Sheet is not correct protected");
            }

            if (!correctDataValidation) {
                System.out.println(workbook_solution.getSheetName(sheet_counter) + ": Plaese check your Data Validation");
            }

            if (!correctHidden) {
                System.out.println(workbook_solution.getSheetName(sheet_counter) + ": Your Row / Colums are not correct hidden");
            }

            if (!correctFormated) {
                System.out.println(workbook_solution.getSheetName(sheet_counter) + ": Your Cells are not correct formated");
            }

            if (Objects.equals(correctDropDown, "Your Dropdown and the Values are correct !") && correctCalculation && correctFormated && correctHidden && correctProtected && correctDataValidation ) {
                System.out.println(workbook_solution.getSheetName(sheet_counter) + " is correct !");
            }
            sheet_counter ++;

        }


    }
}
