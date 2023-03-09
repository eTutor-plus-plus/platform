package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;

import java.util.ArrayList;
import java.util.List;

import static org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator.evaluateAllFormulaCells;

public class DropDownCorrection extends CorrectnessRule {


    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return the Feedback regarding the correct dropdowns
     */
    @Override
    public Feedback checkCorrectness(XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        if (correctDropDown(solution, submission).equals("Your Dropdown and the Values are correct !")) {
            return new Feedback(true, null);
        }
        return new Feedback(false, correctDropDown(solution, submission));
    }

    /**
     * @param sheet sheet where the function should be evaluated
     * @param cell cell where the function should be evaluated
     * @return true, when the cell in the sheet is a dropdown
     */
    public static boolean isDropdown(Sheet sheet, Cell cell) {
        if (cell != null) {
            List<String> dropdown_list = new ArrayList<>();
            List<String> dropdowns = new ArrayList<>();
            List<XSSFDataValidation> dropdown = (List<XSSFDataValidation>) sheet.getDataValidations();
            char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

            // Coordinates of the cell
            String cell_coordinates = Character.toString(alphabet[cell.getColumnIndex()]) + (cell.getRowIndex() + 1);

            // Get all dropdowns of the sheet
            for (XSSFDataValidation elem : dropdown) {
                CellRangeAddress[] list = elem.getRegions().getCellRangeAddresses();
                for (CellRangeAddress adr : list) {
                    dropdown_list.add(adr.formatAsString());
                }
            }

            // Split up every dropdown Area (sometimes the dropdowns are defined as A2:A8) so that it can be checked if the cell is a dropdown
            for (String elem : dropdown_list) {
                if (elem.contains(":")) {
                    String pattern = "(\\D)(\\d*):(\\D)(\\d*)";
                    char v2 = elem.replaceAll(pattern, "$3").charAt(0);
                    int n2 = Integer.parseInt(elem.replaceAll(pattern, "$4"));

                    for (char v1 = elem.replaceAll(pattern, "$1").charAt(0); v1 <= v2; v1++) {
                        for (int n1 = Integer.parseInt(elem.replaceAll(pattern, "$2")); n1 <= n2; n1++) {
                            dropdowns.add(v1 + Integer.toString(n1));
                        }
                    }
                } else {
                    dropdowns.add(elem);
                }
            }

            return dropdowns.contains(cell_coordinates);
        } else {
            return false;
        }
    }

    /**
     * @param workbook workbook where the function should be evaluated
     * @param sheet sheet where the function should be evaluated
     * @return a List of Strings with all Dropdown values of a sheet
     */
    public static List <String> getDropdownValues (XSSFWorkbook workbook, Sheet sheet) {
        List <Cell> dropdowns = getDropdownCells(workbook, sheet);
        List <String> dropdown_values = new ArrayList<>();
        for (Cell elem : dropdowns) {
            dropdown_values.add(elem.toString());
        }
        return dropdown_values;
    }


    /**
     * @param workbook workbook where the function should be evaluated
     * @param sheet sheet where the function should be evaluated
     * @param cell cell where the function should be evaluated
     * @return a List of Strings with all Dropdown values of a cell
     */
    public static List <String> getDropdownValues (XSSFWorkbook workbook, Sheet sheet, Cell cell) throws Exception {
        List <Cell> dropdowns = getDropdownCells(workbook, sheet, cell);
        List <String> dropdown_values = new ArrayList<>();
        for (Cell elem : dropdowns) {
            dropdown_values.add(elem.toString());
        }
        return dropdown_values;
    }

    /**
     * @param workbook workbook where the function should be evaluated
     * @param sheet sheet where the function should be evaluated
     * @return a List of Cells with all Dropdown values of a sheet
     */
    public static List<Cell> getDropdownCells (XSSFWorkbook workbook, Sheet sheet) {
        List<XSSFDataValidation> dropdown = (List<XSSFDataValidation>) sheet.getDataValidations();
        List <String> data = new ArrayList<>();
        for (XSSFDataValidation elem : dropdown) {
            data.add(elem.getValidationConstraint().getFormula1());
        }


        List <String> dropdowns = new ArrayList<>();
        List <Cell> cells = new ArrayList<>();

        // Split up every dropdown Area (sometimes the dropdowns are defined as A2:A8) so that it can be checked if the cell is a dropdown
        for (String elem : data) {
            if (elem.contains(":")) {
                String pattern = "([\\s\\S]+)!\\$(\\D)\\$(\\d*):\\$(\\D)\\$(\\d*)";
                String sheet_name = elem.replaceAll(pattern, "$1");
                char v2 = elem.replaceAll(pattern,"$4").charAt(0);
                int n2 = Integer.parseInt(elem.replaceAll(pattern,"$5"));

                for (char v1 = elem.replaceAll(pattern,"$2").charAt(0); v1 <= v2; v1++) {
                    for (int n1 = Integer.parseInt(elem.replaceAll(pattern,"$3")); n1 <= n2; n1++) {
                        dropdowns.add(sheet_name + "!" + "$" + v1 + "$" + Integer.toString(n1));
                    }
                }
            }
            else if (elem.contains(workbook.getSheetName(0))) {
                dropdowns.add(elem);
            }
        }


        for (String elem : dropdowns) {
            CellReference cf = new CellReference(elem);
            Sheet s = workbook.getSheet(cf.getSheetName());
            Row row = s.getRow(cf.getRow());
            Cell cell = row.getCell(cf.getCol());
            cells.add(cell);
        }
        return cells;

    }



    /**
     * @param workbook workbook where the function should be evaluated
     * @param sheet sheet where the function should be evaluated
     * @param cell cell where the function should be evaluated
     * @return a List of Cells with all Dropdown values of a cell
     */
    public static List <Cell> getDropdownCells (XSSFWorkbook workbook, Sheet sheet, Cell cell) throws Exception {
        char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String cell_position = alphabet[cell.getColumnIndex()] + Integer.toString(cell.getRowIndex()+1);

        // gets all ranges of the dropdownCells
        List<XSSFDataValidation> dropdown = (List<XSSFDataValidation>) sheet.getDataValidations();

        List <String> data = new ArrayList<>();

        for (XSSFDataValidation elem : dropdown) {
            for (CellRangeAddress cellRangeAddress : elem.getRegions().getCellRangeAddresses()) {
                // just adds the current cells referring dropdown cells
                if (cellRangeAddress.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                    data.add(elem.getValidationConstraint().getFormula1());
                }
            }
        }

        String cell_dropdown_values_location = data.get(0);

        List <String> dropdowns = new ArrayList<>();
        List <Cell> cells = new ArrayList<>();

        if (cell_dropdown_values_location.contains(":")) {
            String pattern = "([\\s\\S]+)!\\$(\\D)\\$(\\d*):\\$(\\D)\\$(\\d*)";
            String sheet_name = cell_dropdown_values_location.replaceAll(pattern, "$1");
            char v2 = cell_dropdown_values_location.replaceAll(pattern,"$4").charAt(0);
            int n2 = Integer.parseInt(cell_dropdown_values_location.replaceAll(pattern,"$5"));

            for (char v1 = cell_dropdown_values_location.replaceAll(pattern,"$2").charAt(0); v1 <= v2; v1++) {
                for (int n1 = Integer.parseInt(cell_dropdown_values_location.replaceAll(pattern,"$3")); n1 <= n2; n1++) {
                    dropdowns.add(sheet_name + "!" + "$" + v1 + "$" + Integer.toString(n1));
                }
            }
        }
        else {
            dropdowns.add(cell_dropdown_values_location);
        }

        for (String elem : dropdowns) {
            CellReference cf = new CellReference(elem);
            Sheet s = workbook.getSheet(cf.getSheetName());
            Row row = s.getRow(cf.getRow());
            Cell cell_1 = row.getCell(cf.getCol());
            cells.add(cell_1);

        }
        return cells;
    }



    /**
     * @param submission workbook of the submission
     * @param solution workbook of the solution
     * @return a String which gives feedback about the dropdowns of the sheet of the submission, by comparing the dropdowns with the dropdowns of the solution
     * Catches every Exception and prints feedback that the submission has syntax errors
     */
    public static String correctDropDown (XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        try {

            for (Sheet sheetSolution : solution) {

                Sheet sheetSubmission = submission.getSheetAt(solution.getSheetIndex(sheetSolution.getSheetName()));

                // List of cell with all dropdown cells (all cells with a green background color) of the solution
                List<Cell> dropdownCellsSolution = FillColorHex.getDropdownCells(sheetSolution);

                FormulaEvaluator formulaEvaluatorSubmission = submission.getCreationHelper().createFormulaEvaluator();
                FormulaEvaluator formulaEvaluatorSolution = solution.getCreationHelper().createFormulaEvaluator();


                // iterates over all dropdown cells (green cells)
                for (Cell currentDropdownCellSolution : dropdownCellsSolution) {

                    // gets the cell of the submission which is on the same place as the solution cell
                    Cell currentDropdownCellSubmission = sheetSubmission.getRow(currentDropdownCellSolution.getRowIndex()).getCell(currentDropdownCellSolution.getColumnIndex());

                    if (isDropdown(sheetSolution, currentDropdownCellSolution)) {
                        // checks if the submission cell is a dropdown
                        if (isDropdown(sheetSubmission, currentDropdownCellSubmission)) {
                            List<String> dropdownValuesSubmission = getDropdownValues(submission, sheetSubmission, currentDropdownCellSubmission);
                            List<String> dropdownValuesSolution = getDropdownValues(solution, sheetSolution, currentDropdownCellSolution);

                            boolean correct_dropdown = true;
                            boolean correct_dropdown_values = true;

                            for (String elem : dropdownValuesSolution) {
                                if ((!(dropdownValuesSubmission.contains(elem))) || dropdownValuesSubmission.size() != dropdownValuesSolution.size()) {
                                    correct_dropdown = false;
                                    break;
                                }
                            }

                            if (correct_dropdown) {
                                for (String elem : dropdownValuesSolution) {
                                    sheetSolution.getRow(currentDropdownCellSolution.getRowIndex()).getCell(currentDropdownCellSolution.getColumnIndex()).setCellValue(elem);
                                    sheetSubmission.getRow(currentDropdownCellSubmission.getRowIndex()).getCell(currentDropdownCellSubmission.getColumnIndex()).setCellValue(elem);
                                    for (Row currentRow : sheetSolution) {
                                        for (Cell currentCell_solution : currentRow) {
                                            Cell currentCell_submission = sheetSubmission.getRow(currentCell_solution.getRowIndex()).getCell(currentCell_solution.getColumnIndex());
                                            if (currentCell_submission.getCellType() == CellType.FORMULA) {
                                                ValuesCorrection.overrideUnknownFormulas(formulaEvaluatorSubmission, currentCell_submission);
                                            }
                                            if (FillColorHex.isDropdownCell(sheetSolution, currentCell_solution) && (!(currentCell_solution.getColumnIndex() == currentDropdownCellSolution.getColumnIndex() && currentCell_solution.getRowIndex() == currentDropdownCellSolution.getRowIndex()))) {
                                                sheetSubmission.getRow(currentCell_solution.getRowIndex()).getCell(currentCell_solution.getColumnIndex()).setCellValue(currentCell_solution.getStringCellValue());
                                            }
                                            if (currentCell_solution.getCellType() == CellType.FORMULA && currentCell_submission.getCellType() == CellType.FORMULA) {
                                                ValuesCorrection.overrideUnknownFormulas(formulaEvaluatorSolution, currentCell_solution);
                                                ValuesCorrection.overrideUnknownFormulas(formulaEvaluatorSubmission, currentCell_submission);
                                            }
                                            if (FillColorHex.isValueCell(sheetSolution, currentCell_solution)) {
                                                if (currentCell_solution.getCellType() == CellType.FORMULA) {
                                                    formulaEvaluatorSolution.evaluateFormulaCell(currentCell_solution);
                                                }
                                                if (!(ValuesCorrection.compareCells(currentCell_solution, currentCell_submission)) || currentCell_solution.getCellType() != currentCell_submission.getCellType()) {
                                                    correct_dropdown_values = false;
                                                }

                                            }
                                        }

                                    }
                                }

                                if (!correct_dropdown_values) {
                                    return "The Values which are referring to your Dropdown are not correct !";
                                }
                            } else {
                                return "Your Dropdown does not refer to the correct cells !";
                            }

                        } else {
                            return "You dont have a Dropdown in the right cell !";
                        }
                    }
                }
            }
            return "Your Dropdown and the Values are correct !";
        } catch (Exception e) {
            return "Your submission has either Syntax errors or does not include the correct dropdown cells.";
        }
    }



}


