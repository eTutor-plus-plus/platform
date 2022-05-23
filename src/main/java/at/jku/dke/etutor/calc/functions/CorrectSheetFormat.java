package at.jku.dke.etutor.calc.functions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import java.util.Objects;

public class CorrectSheetFormat {

    /**
     * @param solution sheet of the solution
     * @param submission sheet of the submission
     * @return true, when the rows and columns of the submission are hidden like in the solution
     */
    public static boolean isCorrectHidden (Sheet solution, Sheet submission) {

        for (Row row : solution) {
            Row submission_row = submission.getRow(row.getRowNum());
            // Checks if the correct rows are hidden
            if (row.getZeroHeight() != submission_row.getZeroHeight()) {
                return false;
            }
            for (Cell cell : row) {
                // Checks if the correct columns are hidden
                if (solution.isColumnHidden(cell.getColumnIndex()) != submission.isColumnHidden(cell.getColumnIndex())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param solution sheet of the solution
     * @param submission sheet of the submission
     * @return true, when the rows and columns of the submission are formatted (Datatype and rounded) like in the solution
     */
    public static boolean isCorrectFormatted (Sheet solution, Sheet submission) {
        for (Row row : solution) {
            for (Cell cell : row) {
                Cell submission_cell = submission.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                // Checks if the Data Format of the Cell is correct
                if (!Objects.equals(cell.getCellStyle().getDataFormatString(), submission_cell.getCellStyle().getDataFormatString())) {
                    //System.out.println(submission_cell.getCellStyle().getDataFormatString());
                    //System.out.println(cell.getCellStyle().getDataFormatString());
                    return false;
                }
                // Checks if the length after the Comma is correct
                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    if (Double.toString(cell.getNumericCellValue()).length() != Double.toString(submission_cell.getNumericCellValue()).length()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
