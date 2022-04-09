package at.jku.dke.etutor.calc.functions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public class CorrectCalculations {


    // [255, 255, 128, 0] -> orange

    public static boolean isCorrectCalculated (XSSFWorkbook workbook_solution, XSSFWorkbook workbook_submission, Sheet sheet_solution, Sheet sheet_submission) throws Exception {

        FormulaEvaluator formulaEvaluator_submission = workbook_submission.getCreationHelper().createFormulaEvaluator();
        FormulaEvaluator formulaEvaluator_solution = workbook_solution.getCreationHelper().createFormulaEvaluator();

        if (FillColorHex.getCalculationHelpCells(sheet_solution).size() > 0) {
            List<Cell> calculationHelpCells = FillColorHex.getCalculationHelpCells(sheet_solution);
            for (Cell cell : calculationHelpCells) {
                Cell cell_submission = sheet_submission.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                cell.setCellValue(cell.getNumericCellValue()*Math.random());
                cell_submission.setCellValue(cell.getNumericCellValue());
            }
        }


        for (Row row : sheet_solution) {
            for (Cell cell : row) {
                if (FillColorHex.isCalculationCell(sheet_solution, cell)) {
                    Cell cell_submission = sheet_submission.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                    CorrectValues.checkFormular(formulaEvaluator_solution, cell);
                    CorrectValues.checkFormular(formulaEvaluator_submission, cell_submission);
                    if (!CorrectValues.compareCells(cell,cell_submission)){
                        System.out.println(cell);
                        System.out.println(cell.getNumericCellValue());
                        System.out.println(cell_submission);
                        System.out.println(cell_submission.getNumericCellValue());
                        return false;
                    }

                }

            }
        }


        return true;
    }
}
