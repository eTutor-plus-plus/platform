package at.jku.dke.etutor.calc.functions;

import org.apache.jena.base.Sys;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ProtectionCheck {

    public static boolean isSheetProtected (Sheet sheet) {
        return sheet.getProtect();
    }

    /**
     * @param solution sheet of the solution
     * @param submission sheet of the submission
     * @return true, when the protection of the sheet of the submission equals the protection of the sheet of the solution
     * the function also checks if some cells are able to change even if the sheet is protected
     */
    public static boolean correctProtected (Sheet solution, Sheet submission) {
        try {
            // Checks if the sheet is correct protected
            if (isSheetProtected(solution) != isSheetProtected(submission)) {
                return false;
            }
            // Checks if the correct cells of the sheet are unprotected
            for (Row row : solution) {
                for (Cell cell : row) {
                    Cell submission_cell = submission.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                    if (cell.getCellStyle().getLocked() != submission_cell.getCellStyle().getLocked()) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
        System.out.println("Your submission has Syntax errors, please do not change the given Instruction, just change the values of the green and yellow Cells!");
        return false;
    }

        return true;
    }



}
