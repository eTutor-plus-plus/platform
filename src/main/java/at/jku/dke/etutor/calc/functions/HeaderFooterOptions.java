package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Objects;

public class HeaderFooterOptions extends CorrectnessRule {


    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return the Feedback regarding the correct header and footer
     */
    @Override
    public Feedback checkCorrectness (XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        return correctHeaderFooterOptions(solution, submission);
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return Feedback if the footer/header of the solution is not nothing but the footer/header of the submission is nothing
     */
    public static Feedback correctHeaderFooterOptions (XSSFWorkbook solution, XSSFWorkbook submission) {
        if (!correctHeader(solution, submission)) {
            return new Feedback(false, "The headers of your submission are not correct!");
        }
        if (!correctFooter(solution, submission)) {
            return new Feedback(false, "The footers of your submission are not correct!");
        }
        return new Feedback(true, null);
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return false if the header of the solution is not nothing but the header of the submission is nothing
     */
    public static boolean correctHeader (XSSFWorkbook solution, XSSFWorkbook submission) {

        for (int i = 0; i < solution.getNumberOfSheets(); i++) {

            XSSFSheet sheetSolution = solution.getSheetAt(i);
            XSSFSheet sheetSubmission = submission.getSheetAt(i);

            // return false if the header of the solution is not nothing ("") but the header of the submission is nothing

            if (!Objects.equals(sheetSolution.getHeader().getCenter(), "") && Objects.equals(sheetSubmission.getHeader().getCenter(), "")) {
                return false;
            }

            if (!Objects.equals(sheetSolution.getHeader().getLeft(), "") && Objects.equals(sheetSubmission.getHeader().getLeft(), "")) {
                return false;
            }

            if (!Objects.equals(sheetSolution.getHeader().getRight(), "") && Objects.equals(sheetSubmission.getHeader().getRight(), "")) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return false if the footer of the solution is not nothing but the footer of the submission is nothing
     */
    public static boolean correctFooter (XSSFWorkbook solution, XSSFWorkbook submission) {

        for (int i = 0; i < solution.getNumberOfSheets(); i++) {
            XSSFSheet sheetSolution = solution.getSheetAt(i);
            XSSFSheet sheetSubmission = submission.getSheetAt(i);

            // return false if the footer of the solution is not nothing ("") but the footer of the submission is nothing

            if (!Objects.equals(sheetSolution.getFooter().getCenter(), "") && Objects.equals(sheetSubmission.getFooter().getCenter(), "")) {
                return false;
            }

            if (!Objects.equals(sheetSolution.getFooter().getLeft(), "") && Objects.equals(sheetSubmission.getFooter().getLeft(), "")) {
                return false;
            }

            if (!Objects.equals(sheetSolution.getFooter().getRight(), "") && Objects.equals(sheetSubmission.getFooter().getRight(), "")) {
                return false;
            }
        }
        return true;
    }


}
