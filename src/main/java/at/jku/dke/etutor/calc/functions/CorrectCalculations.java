package at.jku.dke.etutor.calc.functions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public class CorrectCalculations {


    /**
     * @param workbook_solution workbook of the solution (is necessary because the formulas have to be evaluated)
     * @param workbook_submission workbook of the submission (is necessary because the formulas have to be evaluated)
     * @param sheet_solution sheet of the solution
     * @param sheet_submission sheet of the submission
     * @return true, when cells of the submission which should be calculated (evalutated with the function of the Class FillColorHex) are the same as the ones of the solution
     */
    public static boolean isCorrectCalculated (XSSFWorkbook workbook_solution, XSSFWorkbook workbook_submission, Sheet sheet_solution, Sheet sheet_submission) throws Exception {

        FormulaEvaluator formulaEvaluator_submission = workbook_submission.getCreationHelper().createFormulaEvaluator();
        FormulaEvaluator formulaEvaluator_solution = workbook_solution.getCreationHelper().createFormulaEvaluator();


        // overrides the calculationHelpCells with random values because otherwise the calculations could be done without a formula
        if (FillColorHex.getCalculationHelpCells(sheet_solution).size() > 0) {
            List<Cell> calculationHelpCells = FillColorHex.getCalculationHelpCells(sheet_solution);
            for (Cell cell : calculationHelpCells) {
                Cell cell_submission = sheet_submission.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                cell.setCellValue(cell.getNumericCellValue()*Math.random());
                cell_submission.setCellValue(cell.getNumericCellValue());
            }
        }


        // checks the formulas and compares the cells of the solution and submission
        for (Row row : sheet_solution) {
            for (Cell cell : row) {
                if (FillColorHex.isCalculationCell(sheet_solution, cell)) {
                    Cell cell_submission = sheet_submission.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                    CorrectValues.checkFormula(formulaEvaluator_solution, cell);
                    CorrectValues.checkFormula(formulaEvaluator_submission, cell_submission);
                    if (!CorrectValues.compareCells(cell,cell_submission)){
                        return false;
                    }

                }

            }
        }

        return true;
    }
}
