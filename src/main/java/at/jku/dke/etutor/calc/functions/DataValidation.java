package at.jku.dke.etutor.calc.functions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;

import java.util.ArrayList;
import java.util.List;

public class DataValidation {

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
