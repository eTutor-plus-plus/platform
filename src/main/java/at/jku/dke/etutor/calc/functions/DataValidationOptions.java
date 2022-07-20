package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DataValidationOptions extends CorrectnessRule {



    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return the Feedback regarding the correct data validation
     */
    @Override
    public Feedback checkCorrectness(XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        return correctDataValidationOptions(solution, submission);
    }


    /**
     * @param solution Workbook of the solution
     * @param submission Workbook of the submission
     * @return Feedback if the dataValidation (DataValidationConstraint, ErrorBox and PromptBox) of the solution and submission is the same
     */
    public static Feedback correctDataValidationOptions (XSSFWorkbook solution, XSSFWorkbook submission) {
        for (int i = 0; i < solution.getNumberOfSheets(); i++) {
            XSSFSheet sheetSolution = solution.getSheetAt(i);
            XSSFSheet sheetSubmission = submission.getSheetAt(i);

            List<XSSFDataValidation> dataValidationListSolution = sheetSolution.getDataValidations();
            List<XSSFDataValidation> dataValidationListSubmission = sheetSubmission.getDataValidations();

            // gets a list of the ranges (in which cells the validations are) of the data validations
            List<String> cellRangeListSolution = new ArrayList<>();
            List<String> cellRangeListSubmission = new ArrayList<>();

            for (XSSFDataValidation dataValidation : dataValidationListSolution) {
                cellRangeListSolution.add(Arrays.toString(dataValidation.getRegions().getCellRangeAddresses()));
            }

            for (XSSFDataValidation dataValidation : dataValidationListSubmission) {
                cellRangeListSubmission.add(Arrays.toString(dataValidation.getRegions().getCellRangeAddresses()));
            }

            if (cellRangeListSolution.size() != cellRangeListSubmission.size()) {
                return new Feedback(false, "Your submission does not contain the correct amount of data validations!");
            }
            else {
                for (String elem : cellRangeListSolution) {
                    if (!cellRangeListSubmission.contains(elem)){
                        return new Feedback(false, "Your data validations are not on the correct cells!");
                    }
                }
                for (XSSFDataValidation dataValidationSolution : dataValidationListSolution) {
                    // gets the index of the submission because the order of the data validations is sometimes different, even it is in the same cells
                    int indexOfSubmission = 0;
                    for (XSSFDataValidation dataValidationSubmission : dataValidationListSubmission) {
                        if (Objects.equals(Arrays.toString(dataValidationSolution.getRegions().getCellRangeAddresses()), Arrays.toString(dataValidationSubmission.getRegions().getCellRangeAddresses()))) {
                            indexOfSubmission = dataValidationListSubmission.indexOf(dataValidationSubmission);
                        }
                    }
                    XSSFDataValidation dataValidationSubmission = dataValidationListSubmission.get(indexOfSubmission);

                    // calls the functions which are defined below
                    if (!correctDataValidationConstraint(dataValidationSolution, dataValidationSubmission)) {
                        return new Feedback(false, "Your data validation constraints are not correct!");
                    }
                    if (!correctDataValidationErrorBox(dataValidationSolution, dataValidationSubmission)) {
                        return new Feedback(false, "The error box of your data validation does not contain the correct text!");
                    }
                    if (!correctDataValidationPromptBox(dataValidationSolution, dataValidationSubmission)) {
                        return new Feedback(false, "The prompt box of your data validation does not contain the correct text!");
                    }
                }
            }

        }
        return new Feedback(true, null);
    }

    /**
     * @param dataValidationSolution DataValidation of the solution
     * @param dataValidationSubmission DataValidation of the submission
     * @return true if the validationConstraint of the solution and submission is the same
     */
    public static boolean correctDataValidationConstraint (XSSFDataValidation dataValidationSolution, XSSFDataValidation dataValidationSubmission) {

        DataValidationConstraint dataValidationConstraintSolution = dataValidationSolution.getValidationConstraint();
        DataValidationConstraint dataValidationConstraintSubmission = dataValidationSubmission.getValidationConstraint();


        // checks the validation type
        if (dataValidationConstraintSolution.getValidationType() != dataValidationConstraintSubmission.getValidationType()) {
            return false;
        }

        // checks the formula1
        if (!Objects.equals(dataValidationConstraintSolution.getFormula1(), dataValidationConstraintSubmission.getFormula1())) {
            return false;
        }

        // checks the formula2
        if (!Objects.equals(dataValidationConstraintSolution.getFormula2(), dataValidationConstraintSubmission.getFormula2())) {
            return false;
        }

        return true;

    }

    /**
     * @param dataValidationSolution DataValidation of the Solution
     * @param dataValidationSubmission DataValidation of the Submission
     * @return true if the setting of the errorBox of solution and submission are the same
     */
    public static boolean correctDataValidationErrorBox (XSSFDataValidation dataValidationSolution, XSSFDataValidation dataValidationSubmission) {

        // checks if the error box should be shown
        if (dataValidationSolution.getShowErrorBox() != dataValidationSubmission.getShowErrorBox()) {
            return false;
        }

        // checks if the error box text is not null if the solution text of the solution is not null
        if (dataValidationSolution.getErrorBoxText() != null && dataValidationSubmission.getErrorBoxText() == null) {
            return false;
        }

        // checks if the error box title is not null if the solution text of the solution is not null
        if (dataValidationSolution.getErrorBoxTitle() != null && dataValidationSubmission.getErrorBoxTitle() == null) {
            return false;
        }

        return true;
    }

    /**
     * @param dataValidationSolution DataValidation of the Solution
     * @param dataValidationSubmission DataValidation of the Submission
     * @return true if the settings of the promptBox of solution and submission are the same
     */
    public static boolean correctDataValidationPromptBox (XSSFDataValidation dataValidationSolution, XSSFDataValidation dataValidationSubmission) {

        // checks if the prompt box should be shown
        if (dataValidationSolution.getShowPromptBox() != dataValidationSubmission.getShowPromptBox()) {
            return false;
        }

        // checks if the prompt box text is not null if the solution text of the solution is not null
        if (dataValidationSolution.getPromptBoxText() != null && dataValidationSubmission.getPromptBoxText() == null) {
            return false;
        }

        // checks if the prompt box title is not null if the solution text of the solution is not null
        if (dataValidationSolution.getPromptBoxTitle() != null && dataValidationSubmission.getPromptBoxTitle() == null) {
            return false;
        }

        return true;
    }


}
