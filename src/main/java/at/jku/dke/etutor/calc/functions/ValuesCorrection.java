package at.jku.dke.etutor.calc.functions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import java.util.Objects;

/**
 * This class is a help class for evaluating formulas and comparing cells
 */
public class ValuesCorrection {

    /**
     * @param formulaEvaluator is needed to evaluate the formulas
     * @param cell cell where the formula should be checked
     * This function is overriding functions which are used in calc but are not usable in the POI library
     */
    public static void overrideUnknownFormulas (FormulaEvaluator formulaEvaluator, Cell cell) {
        try {
            if (cell.getCellType() == CellType.FORMULA) {
                // ORG.OPENOFFICE.YEARS-Function is not implemented -> implement new Function
                if (cell.getCellFormula().startsWith("ORG.OPENOFFICE.YEARS")) {
                    String formula_reference = cell.getCellFormula().replaceAll("ORG.OPENOFFICE.YEARS\\(", "").split(",")[0];
                    cell.setCellFormula("ROUNDDOWN((TODAY()-" + formula_reference + ")/365.24,0)");
                }
                // org.openoffice.years-Function is not implemented -> implement new Function
                if (cell.getCellFormula().startsWith("org.openoffice.years")) {
                    String formula_reference = cell.getCellFormula().replaceAll("org.openoffice.years\\(", "").split(",")[0];
                    cell.setCellFormula("ROUNDDOWN((TODAY()-" + formula_reference + ")/365.24,0)");
                }
                // Days()-Function is not implemented -> implement new Function
                if (cell.getCellFormula().contains("_xlfn.DAYS")) {
                    String days_pattern = "(_xlfn.DAYS\\()(\\w*),(\\w*\\))";
                    String formula_reference = cell.getCellFormula().replaceAll(days_pattern, "($2-$3");
                    cell.setCellFormula(formula_reference);
                }
                formulaEvaluator.evaluateFormulaCell(cell);
            }
        }catch (Exception ignored){}


    }

    /**
     * @param solution cell of the solution
     * @param submission cell of the submission
     * @return true when the values of the submission cell equals the value of the solution cell
     * the function tolerates 1% <>
     */
    public static boolean compareCells (Cell solution, Cell submission) {
        try {
            if (solution.getCellType() == CellType.NUMERIC && submission.getCellType() == CellType.NUMERIC) {
                return submission.getNumericCellValue() * 1.01 >= solution.getNumericCellValue() && submission.getNumericCellValue() * 0.99 <= solution.getNumericCellValue();
            }
            else if (solution.getCellType() == CellType.STRING && submission.getCellType() == CellType.STRING) {
                return Objects.equals(submission.getStringCellValue(), solution.getStringCellValue());
            }
            else if (solution.getCellType() == CellType.FORMULA && submission.getCellType() == CellType.FORMULA) {
                if (solution.getCachedFormulaResultType() == CellType.NUMERIC && submission.getCachedFormulaResultType() == CellType.NUMERIC) {
                    return Math.abs(submission.getNumericCellValue()) * 1.01 >= Math.abs(solution.getNumericCellValue()) && Math.abs(submission.getNumericCellValue()) * 0.99 <= Math.abs(solution.getNumericCellValue());
                }
                else if (solution.getCachedFormulaResultType() == CellType.STRING && submission.getCachedFormulaResultType() == CellType.STRING) {
                    return Objects.equals(submission.getStringCellValue(), solution.getStringCellValue());
                }
            }
            else if (solution.getCellType() == CellType.BLANK && solution.getCellType() == CellType.BLANK) {
                return true;
            }
        } catch (Exception e){
            return false;
        }
        return false;
    }
}
