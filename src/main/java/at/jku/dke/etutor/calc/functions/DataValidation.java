package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.List;

public class DataValidation extends CorrectnessRule {


    /**
     * OLD FEATURE THE NEW CLASS IS DataValidationOptions
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return the Feedback regarding the correct data validation
     */
    @Override
    public Feedback checkCorrectness(XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        if (checkDataValidation(solution.getSheetAt(1), submission.getSheetAt(1))) {
            return new Feedback(true, null);
        }
        return new Feedback(false, "Please check your Data Validation !");
    }


    /**
     * @param solution sheet of the solution
     * @param submission sheet of the submission
     * @return true, when the dataValidation of the submission equals the dataValidation of the solution
     * this function is for the Examples 5 & 6 where it should only be possible to type in specific dates
     * this function is just checking the dataValidation for dates, because otherwise it fails (numeric does not equal integer and problems which the overwritten formulas in the CreateRandomInstruction.overrideWorkbooks)
     */
    public static boolean checkDataValidation (Sheet solution, Sheet submission) {
        List<XSSFDataValidation> solution_validation = (List<XSSFDataValidation>) solution.getDataValidations();
        List<XSSFDataValidation> submission_validation = (List<XSSFDataValidation>) submission.getDataValidations();

        List <Integer> solution_validation_types = new ArrayList<>();
        List <String> solution_validation_formulas = new ArrayList<>();

        List <Integer> submission_validation_types = new ArrayList<>();
        List <String> submission_validation_formulas = new ArrayList<>();

        for (XSSFDataValidation elem : solution_validation) {
            if (elem.getValidationConstraint().getValidationType() == DataValidationConstraint.ValidationType.DATE) {
                solution_validation_types.add(elem.getValidationConstraint().getValidationType());
                solution_validation_formulas.add(elem.getValidationConstraint().getFormula1());
                solution_validation_formulas.add(elem.getValidationConstraint().getFormula2());

            }
        }

        for (XSSFDataValidation elem : submission_validation) {

            if (elem.getValidationConstraint().getValidationType() == DataValidationConstraint.ValidationType.DATE) {
                submission_validation_types.add(elem.getValidationConstraint().getValidationType());
                submission_validation_formulas.add(elem.getValidationConstraint().getFormula1());
                submission_validation_formulas.add(elem.getValidationConstraint().getFormula2());
            }
        }

        if (solution_validation_types.size() != submission_validation_types.size() || solution_validation_formulas.size() != submission_validation_formulas.size()) {
            return false;
        }
        else {
            for (int elem : solution_validation_types) {
                if (! submission_validation_types.contains(elem)) {
                    return false;
                }
            }
            for (String elem : solution_validation_formulas) {
                if (!submission_validation_formulas.contains(elem)) {
                    return false;
                }
            }
        }
        return true;
    }
}
