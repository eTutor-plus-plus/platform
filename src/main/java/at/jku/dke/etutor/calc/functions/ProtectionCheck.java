package at.jku.dke.etutor.calc.functions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ProtectionCheck {

    public static boolean isSheetProtected (Sheet sheet) {
        return sheet.getProtect();
    }

    public static boolean correctProtected (Sheet solution, Sheet submission) {

        // TODO: Checks if the sheet is correct protected
        if (isSheetProtected(solution) != isSheetProtected(submission)) {
            return false;
        }
        // TODO: Checks if the correct cells of the sheet are unprotected
        for (Row row : solution) {
            for (Cell cell : row) {
                Cell submission_cell = submission.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                if (cell.getCellStyle().getLocked() != submission_cell.getCellStyle().getLocked()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void protectSheet (Sheet sheet) {
        if (!isSheetProtected(sheet)) {
            sheet.protectSheet("asdf");
        }
    }


}
