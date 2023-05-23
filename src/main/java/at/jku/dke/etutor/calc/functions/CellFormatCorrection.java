package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CellFormatCorrection extends CorrectnessRule {


    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return the Feedback regarding the correct format of the cell
     */
    @Override
    public Feedback checkCorrectness(XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        return checkCorrectCellFormat(solution, submission);
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return feedback if the solution has the same formatted cells as the submission (format, hidden, locked, usw.)
     * for more information check the functions above
     */
    public static Feedback checkCorrectCellFormat (XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {

        for (int i = 0; i < solution.getNumberOfSheets(); i++) {
            XSSFSheet sheetSolution = solution.getSheetAt(i);
            XSSFSheet sheetSubmission = submission.getSheetAt(i);

            for (Row row : sheetSolution) {
                for (Cell cell : row) {
                    // is just checking the cell format if the cell is a value, calculation, dropdown or checkCellFormatCell
                    if (FillColorHex.isCheckCellFormatCell(sheetSolution, cell) || FillColorHex.isCalculationCell(sheetSolution, cell) || FillColorHex.isValueCell(sheetSolution, cell) || FillColorHex.isDropdownCell(sheetSolution, cell)) {
                        XSSFCell cellSolution = sheetSolution.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                        XSSFCell cellSubmission = sheetSubmission.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                        if (!checkDataFormat(cellSolution, cellSubmission)) {
                            return new Feedback(false, "Your calculated cells are not in the correct data format!");
                        }
                        if (!checkExactlyMatchingCellFormat(cellSolution, cellSubmission)) {
                            return new Feedback(false, "Your cells are not in the correct format. Check the size, font and the alignment of your cells!");
                        }
                        if (!checkHidden(cellSolution, cellSubmission)) {
                            return new Feedback(false, "Your cells are not correctly hidden!");
                        }
                        if (!checkLocked(cellSolution, cellSubmission)){
                          return new Feedback( false, "Your cells are not correct locked!");
                        }
                    }
                }
            }
        }
        return new Feedback(true, null);
    }

    /**
     * @param solution cell of the solution
     * @param submission cell of the submission
     * @return true if the format of the submission is the same as of the solution (currency, general, percent, date)
     */
    public static boolean checkDataFormat (Cell solution, Cell submission) {


        // check the standard types of the cell
        if (solution.getCellType() != submission.getCellType()) {
            return false;
        }

        if (Objects.equals(solution.getCellStyle().getDataFormatString(), "General") && !Objects.equals(submission.getCellStyle().getDataFormatString(), "General")) {
            return false;
        }

        // check the currency
        final String regex = "\\[(\\$.)";
        final String currencySolution = solution.getCellStyle().getDataFormatString();
        final String currencySubmission = submission.getCellStyle().getDataFormatString();


        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcherSolution = pattern.matcher(currencySolution);


        if (matcherSolution.find()){
                if (matcherSolution.groupCount() >= 1) {
                        if (!currencySubmission.contains(matcherSolution.group(1))) {
                            return false;
                        }
                }

        }


        // check the %
        if (solution.getCellStyle().getDataFormatString().contains("%") && !submission.getCellStyle().getDataFormatString().contains("%")) {
            return false;
        }

        // check date format
        if (solution.getCellStyle().getDataFormatString().contains("d") && solution.getCellStyle().getDataFormatString().contains("m")) {
            if (!submission.getCellStyle().getDataFormatString().contains("d") || !submission.getCellStyle().getDataFormatString().contains("m")) {
                return false;
            }
        }


        return true;
    }

    /**
     * @param solution cell of the solution
     * @param submission cell of the submission
     * @return true if the cell of the submission is same locked (true or false) as the solution
     */
    public static boolean checkLocked (Cell solution, Cell submission) {
        return solution.getCellStyle().getLocked() == submission.getCellStyle().getLocked();
    }

    /**
     * @param solution cell of the solution
     * @param submission cell of the submission
     * @return true if the cell of the submission is same hidden (true or false) as the solution
     */
    public static boolean checkHidden (Cell solution, Cell submission) {
        return solution.getCellStyle().getHidden() == submission.getCellStyle().getHidden();
    }

    /**
     * @param solution Cell of the solution
     * @param submission Cell of the submission
     * @return true if the cell of the solution has the exactly same format as the submission
     * this just gets checked if the cell of the submission is marked with grey background color
     */
    public static boolean checkExactlyMatchingCellFormat (XSSFCell solution, XSSFCell submission) throws Exception {
        if (FillColorHex.isCheckCellFormatCell(solution.getSheet(), solution)) {
            if (solution.getCellStyle().getAlignment() != submission.getCellStyle().getAlignment()) {
                return false;
            }
            if (solution.getCellStyle().getFont().getFontHeight() != submission.getCellStyle().getFont().getFontHeight()) {
                return false;
            }
            if (!Objects.equals(solution.getCellStyle().getFont().getFontName(), submission.getCellStyle().getFont().getFontName())) {
                return false;
            }
        }
        return true;
    }


}
