package at.jku.dke.etutor.calc.functions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import java.util.Objects;

public class CorrectValues {

    public static void checkFormular (FormulaEvaluator formulaEvaluator, Cell cell) {
        try {
            if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                if (cell.getCellFormula().startsWith("ORG.OPENOFFICE.YEARS")) {
                    String formula_reference = cell.getCellFormula().replaceAll("ORG.OPENOFFICE.YEARS\\(", "").split(",")[0];
                    // is not working because datedif is not implemented
                    //                        currentCell.setCellFormula("DATEDIF(A1,TODAY(),\"y\")");
                    cell.setCellFormula("ROUNDDOWN((TODAY()-" + formula_reference + ")/365.24,0)");
                }
                if (cell.getCellFormula().startsWith("org.openoffice.years")) {
                    String formula_reference = cell.getCellFormula().replaceAll("org.openoffice.years\\(", "").split(",")[0];
                    // is not working because datedif is not implemented
                    //                        currentCell.setCellFormula("DATEDIF(A1,TODAY(),\"y\")");
                    cell.setCellFormula("ROUNDDOWN((TODAY()-" + formula_reference + ")/365.24,0)");
                }
                // TODO: Days()-Function is not implemented -> implement new Function
                if (cell.getCellFormula().contains("_xlfn.DAYS")) {
                    String days_pattern = "(_xlfn.DAYS\\()(\\w*),(\\w*\\))";
                    String formula_reference = cell.getCellFormula().replaceAll(days_pattern, "($2-$3");
                    cell.setCellFormula(formula_reference);
                }
                formulaEvaluator.evaluateFormulaCell(cell);
            }
        }catch (Exception ignored){}


    }

    public static boolean compareCells (Cell solution, Cell submission) {
        try {
            if (solution.getCellType() == Cell.CELL_TYPE_NUMERIC && submission.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                return submission.getNumericCellValue() * 1.01 >= solution.getNumericCellValue() && submission.getNumericCellValue() * 0.99 <= solution.getNumericCellValue();
            }
            else if (solution.getCellType() == Cell.CELL_TYPE_STRING && submission.getCellType() == Cell.CELL_TYPE_STRING) {
                return Objects.equals(submission.getStringCellValue(), solution.getStringCellValue());
            }
            else if (solution.getCellType() == Cell.CELL_TYPE_FORMULA && submission.getCellType() == Cell.CELL_TYPE_FORMULA) {
                if (solution.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC && submission.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) {
                    return Math.abs(submission.getNumericCellValue()) * 1.01 >= Math.abs(solution.getNumericCellValue()) && Math.abs(submission.getNumericCellValue()) * 0.99 <= Math.abs(solution.getNumericCellValue());
                }
                else if (solution.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING && submission.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING) {
                    return Objects.equals(submission.getStringCellValue(), solution.getStringCellValue());
                }
            }
            else if (solution.getCellType() == Cell.CELL_TYPE_BLANK && solution.getCellType() == Cell.CELL_TYPE_BLANK) {
                return true;
            }
        } catch (Exception e){
            return false;
        }
        return false;
    }
}
