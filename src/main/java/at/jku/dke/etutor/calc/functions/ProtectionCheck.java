package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.jena.base.Sys;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ProtectionCheck extends CorrectnessRule {


    /**
     * This function is not concluded in the config file
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return the Feedback regarding the correct sheet protection
     */
    @Override
    public Feedback checkCorrectness(XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        if (correctProtected(solution.getSheetAt(1), submission.getSheetAt(1))) {
            return new Feedback(true, null);
        }
        return new Feedback(false, "Your submission is not correct protected !");
    }

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
