package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdditionalFunctions {

    public static boolean correctCommentOptions (XSSFWorkbook solution, XSSFWorkbook submission) {

        for (int i = 0; i < solution.getNumberOfSheets(); i++) {
            XSSFSheet sheetSolution = solution.getSheetAt(i);
            XSSFSheet sheetSubmission = submission.getSheetAt(i);

            Map<CellAddress, XSSFComment> xssfCommentMapSolution =  sheetSolution.getCellComments();
            Map<CellAddress, XSSFComment> xssfCommentMapSubmission =  sheetSubmission.getCellComments();

            // just checks if the submission has a comment on the same cell as the solution
            for (var entry : xssfCommentMapSolution.entrySet()) {
                if (!xssfCommentMapSubmission.containsKey(entry.getKey())) {
                    return false;
                }
                // how to get the text of the comment
                entry.getValue().getString().getString();
            }
        }
        return true;
    }

    public static boolean correctSheetName (XSSFWorkbook solution, XSSFWorkbook submission) {

        for (int i = 0; i < solution.getNumberOfSheets(); i++) {
            XSSFSheet sheetSolution = solution.getSheetAt(i);
            XSSFSheet sheetSubmission = submission.getSheetAt(i);

            // returns false if the sheetName of the solution differs the sheetName of the submission
            if (!Objects.equals(sheetSolution.getSheetName(), sheetSubmission.getSheetName())) {
                return false;
            }
        }
        return true;
    }

    public static boolean correctHiddenSheet (XSSFWorkbook solution, XSSFWorkbook submission) {

        for (int i = 0; i < solution.getNumberOfSheets(); i++) {
            XSSFSheet sheetSolution = solution.getSheetAt(i);
            XSSFSheet sheetSubmission = submission.getSheetAt(i);

            // returns false if the hidden sheets of the submission differs from the sheets of the solution
            if (solution.isSheetHidden(i) != submission.isSheetHidden(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param instructionWriter instruction writer document
     * @param submissionCalc submission calc document
     * @return true if the crypto code of the instruction and the submission is the same
     */
    public static boolean checkCryptoCode (XWPFDocument instructionWriter, XSSFWorkbook submissionCalc) {

        String cryptoCode = "";

        final String regex = "(#.*)";
        final String string = instructionWriter.getHeaderFooterPolicy().getFooter(0).getText();

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(string);

        if (matcher.find()) {
            cryptoCode = matcher.group(0);
        }
        if (submissionCalc.getProperties().getCoreProperties().getKeywords() == null) {
            return false;
        }
        return submissionCalc.getProperties().getCoreProperties().getKeywords().equals(cryptoCode);

    }
}
