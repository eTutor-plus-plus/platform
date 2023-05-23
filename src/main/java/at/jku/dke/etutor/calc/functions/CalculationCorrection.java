package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculationCorrection extends CorrectnessRule {


    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return the Feedback regarding the correct calculations
     */
    @Override
    public Feedback checkCorrectness(XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        if (!isCorrectCalculated(solution, submission)) {
            return new Feedback(false, "Your calculated Values are not correct !");
        }
        if (!correctFormulasUse(solution, submission)) {
            return new Feedback(false,"Your use of one of the following functions is not correct: VLOOKUP, HLOOKUP, LOOKUP!");
        }

        return new Feedback(true, null);
    }



    /**
     * @param solution workbook of the solution (is necessary because the formulas have to be evaluated)
     * @param submission workbook of the submission (is necessary because the formulas have to be evaluated)
     * @return true, when cells of the submission which should be calculated (evalutated with the function of the Class FillColorHex) are the same as the ones of the solution
     */
    public static boolean isCorrectCalculated (XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {

        for (Sheet sheetSolution : solution) {

            Sheet sheetSubmission = submission.getSheetAt(solution.getSheetIndex(sheetSolution.getSheetName()));

            FormulaEvaluator formulaEvaluatorSubmission = submission.getCreationHelper().createFormulaEvaluator();
            FormulaEvaluator formulaEvaluatorSolution = solution.getCreationHelper().createFormulaEvaluator();


            // overrides the calculationHelpCells with random values when the cells are numeric
            // overrides the calculationHelpCells with another string value in the workbook when the cells are strings
            if (FillColorHex.getCalculationHelpCells(sheetSolution).size() > 0) {
                List<Cell> calculationHelpCells = FillColorHex.getCalculationHelpCells(sheetSolution);
                List<Cell> stringCalculationHelpCells = new ArrayList<>();
                int counter = 0;
                for (Cell cell : calculationHelpCells) {
                    if (cell.getCellType() == CellType.NUMERIC) {
                        Cell cell_submission = sheetSubmission.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                        cell.setCellValue(cell.getNumericCellValue() * Math.random());
                        cell_submission.setCellValue(cell.getNumericCellValue());
                    } else if (cell.getCellType() == CellType.STRING) {
                        stringCalculationHelpCells.add(cell);
                    }
                }
                Collections.shuffle(stringCalculationHelpCells);
                for (Cell cell : calculationHelpCells) {
                    if (cell.getCellType() == CellType.STRING) {
                        Cell cell_submission = sheetSubmission.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                        cell.setCellValue(stringCalculationHelpCells.get(counter).getStringCellValue());
                        cell_submission.setCellValue(stringCalculationHelpCells.get(counter).getStringCellValue());
                        counter++;
                    }
                }
            }


            // checks the formulas and compares the cells of the solution and submission
            for (Row row : sheetSolution) {
                for (Cell cell : row) {
                    if (FillColorHex.isCalculationCell(sheetSolution, cell)) {
                        Cell cell_submission = sheetSubmission.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                        ValuesCorrection.overrideUnknownFormulas(formulaEvaluatorSolution, cell);
                        ValuesCorrection.overrideUnknownFormulas(formulaEvaluatorSubmission, cell_submission);
                        if (!ValuesCorrection.compareCells(cell, cell_submission)) {
                            return false;
                        }

                    }

                }
            }
        }

        return true;
    }

    /**
     * @param workbook_solution workbook of the solution
     * @param workbook_submission workbook of the submission
     * @return true if VLOOKUP, HLOOKUP and LOOKUP are used correctly
     */
    public static boolean correctFormulasUse(XSSFWorkbook workbook_solution, XSSFWorkbook workbook_submission) {

        for (Sheet sheet : workbook_solution) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.FORMULA) {
                        if (cell.getCellFormula().contains("LOOKUP")) {

                            List<String> formulaParametersSolution = getFormulaParameters(cell.getCellFormula());

                            // gets the submission cell which is on the same place as the solution cell
                            Cell cell_submission = workbook_submission.getSheet(cell.getSheet().getSheetName()).getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());

                            if (cell_submission.getCellType() == CellType.FORMULA) {

                                List<String> formulaParametersSubmission = getFormulaParameters(cell_submission.getCellFormula());


                                // check for the VLOOKUP formula
                                if (cell.getCellFormula().contains("VLOOKUP") || cell.getCellFormula().contains("HLOOKUP")) {
                                    if (!checkFormulaVLookupHLookup(cell, cell_submission, formulaParametersSolution, formulaParametersSubmission)) {
                                        return false;
                                    }
                                } else if (cell.getCellFormula().contains("LOOKUP")) {
                                    if (!checkFormulaLookup(cell, cell_submission, formulaParametersSolution, formulaParametersSubmission)) {
                                        return false;
                                    }
                                }
                            } else return false;
                        }
                    }
                }
            }
        }
        return true;

    }

    /**
     * @param formula String of the formula
     * @return the parameters of a formula
     * this function helps the function above
     */
    public static List<String> getLookupParametersOfFormula (String formula) {
        List<String> formulaParameters = new ArrayList<>();
        int parameterCount = 0;
        int bracketsCounter = 0;
        StringBuilder parameter = new StringBuilder();
        for (char elem : formula.toCharArray()) {
            if (elem == ',' && bracketsCounter == 0) {
                parameterCount++;
                formulaParameters.add(parameter.toString());
                parameter = new StringBuilder();

            }
            else if (elem == ')' && bracketsCounter == 0) {
                formulaParameters.add(parameter.toString());
                break;
            }
            else if (elem == '(') {
                bracketsCounter++;
            }
            else if (elem == ')') {
                bracketsCounter = 0;
                formulaParameters.add(parameter.toString());
                parameter = new StringBuilder();
            }
            else {
                parameter.append(elem);
            }
        }
        formulaParameters.remove("");
        return formulaParameters;
    }

    /**
     * @param formula String of the formula
     * @return a List of Strings with the parameter of the (S/H)LOOKUP function (even though it is inside a formula)
     */
    public static List<String> getFormulaParameters (String formula) {
        String wholeFormula = "";
        String regex = "[VH]LOOKUP\\((.*\\))";

        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(formula);

        List<String> formulaParameters = new ArrayList<>();

        if (matcher.find()) {
            if (matcher.groupCount() >= 1) {
                wholeFormula = matcher.group(1);
                formulaParameters = getLookupParametersOfFormula(wholeFormula);
            }



        }
        else {
            regex = "LOOKUP\\((.*\\))";
            pattern = Pattern.compile(regex, Pattern.MULTILINE);
            matcher = pattern.matcher(formula);

            if (matcher.find()) {
                if (matcher.groupCount() >= 1) {
                    wholeFormula = matcher.group(1);
                    formulaParameters = getLookupParametersOfFormula(wholeFormula);
                }
            }
        }
        return formulaParameters;
    }

    /**
     * @param cell_solution cell of the solution
     * @param cell_submission cell of the submission
     * @param formula_parameters_solution parameters of the LOOKUP Function of the solution
     * @param formula_parameters_submission parameters of the LOOKUP Function of the submission
     * @return true if the V or H LOOKUP function is used correct
     */
    public static boolean checkFormulaVLookupHLookup(Cell cell_solution, Cell cell_submission, List <String> formula_parameters_solution, List <String> formula_parameters_submission) {

        // if solution cell is VLOOKUP with the last parameter 0 the submission cell must use VLOOKUP with the last parameter 0
        // if solution cell is VLOOKUP with the last parameter 1 the submission cell must use VLOOKUP with the last parameter 1 or LOOKUP
        // VLOOKUP can be used with only3 parameters then the 4. parameter is default 0
        // for HLOOKUP the same rules

        if (formula_parameters_solution.size() == 3) {
            if (cell_submission.getCellFormula().contains("VLOOKUP") || cell_submission.getCellFormula().contains("HLOOKUP")) {
                if (formula_parameters_submission.size() == 3 || formula_parameters_submission.size() == 4) {
                    if (formula_parameters_submission.size() == 4) {
                        if (!Objects.equals(formula_parameters_submission.get(3), "0")) {
                            return false;
                        }
                    }
                }
                else return false;
            }
            else return false;
        }

        // check for the VLOOKUP/HLOOKUP with last parameter = 0
        if (formula_parameters_solution.size() == 4) {
            if (Objects.equals(formula_parameters_solution.get(3), "0")) {
                if (cell_submission.getCellFormula().contains("VLOOKUP") || cell_submission.getCellFormula().contains("HLOOKUP")) {
                    if (formula_parameters_submission.size() == 3 || formula_parameters_submission.size() == 4) {
                        if (formula_parameters_submission.size() == 4) {
                            if (!Objects.equals(formula_parameters_submission.get(3), "0")) {
                                return false;
                            }
                        }
                    }
                    else return false;
                }
                else return false;
            }
        }

        //check for the VLOOKUP/HLOOKUP with last parameter = 1 (VLOOKUP/HLOOKUP with 1 or LOOKUP is allowed)
        if (formula_parameters_solution.size() == 4) {
            if (Objects.equals(formula_parameters_solution.get(3), "1")) {
                if (cell_submission.getCellFormula().contains("VLOOKUP") || cell_submission.getCellFormula().contains("HLOOKUP") || cell_submission.getCellFormula().contains("LOOKUP")) {
                    if (cell_submission.getCellFormula().contains("VLOOKUP") || cell_submission.getCellFormula().contains("HLOOKUP")) {
                        if (formula_parameters_submission.size() == 4) {
                            return Objects.equals(formula_parameters_submission.get(3), "1");
                        }
                        else return false;
                    }
                }
                else return false;
            }
        }
        return true;

    }

    /**
     * @param cell_solution cell of the solution
     * @param cell_submission cell of the submission
     * @param formula_parameters_solution parameters of the LOOKUP Function of the solution
     * @param formula_parameters_submission parameters of the LOOKUP Function of the submission
     * @return true if the LOOKUP function is used correct
     */
    public static boolean checkFormulaLookup (Cell cell_solution, Cell cell_submission, List<String> formula_parameters_solution, List<String> formula_parameters_submission) {
        // solution uses the function LOOKUP the submssion can use LOOKUP, VLOOKUP with last parameter 1 or HLOOKUP with last parameter 1
        if (cell_submission.getCellFormula().contains("LOOKUP") || cell_submission.getCellFormula().contains("VLOOKUP") || cell_submission.getCellFormula().contains("HLOOKUP")) {
            if (cell_submission.getCellFormula().contains("VLOOKUP") || cell_submission.getCellFormula().contains("HLOOKUP")) {
                if (formula_parameters_submission.size() == 4) {
                    return Objects.equals(formula_parameters_submission.get(3), "1");
                }
                else return false;
            }
        }
        else return false;

        return true;
    }



}
