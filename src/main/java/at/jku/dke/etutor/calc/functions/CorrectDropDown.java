package at.jku.dke.etutor.calc.functions;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.List;

public class CorrectDropDown {

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
    public static List <String> getDropdownValues (XSSFWorkbook workbook, Sheet sheet, Cell cell) {
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
    public static List <Cell> getDropdownCells (XSSFWorkbook workbook, Sheet sheet, Cell cell) {
        char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String cell_position = alphabet[cell.getColumnIndex()] + Integer.toString(cell.getRowIndex()+1);

        List<XSSFDataValidation> dropdown = (List<XSSFDataValidation>) sheet.getDataValidations();

        List <String> data = new ArrayList<>();

        for (XSSFDataValidation elem : dropdown) {
            if (elem.getValidationConstraint().getFormula1().contains("Hilfstabellen") && !(elem.getValidationConstraint().getFormula1()).contains("$A$3")) {
                data.add(elem.getValidationConstraint().getFormula1());
            }
        }

        String cell_dropdown_values_location = data.get(0);

        List <String> dropdowns = new ArrayList<>();
        List <Cell> cells = new ArrayList<>();

        // Split up every dropdown Area (sometimes the dropdowns are defined as A2:A8) so that it can be checked if the cell is a dropdown
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
        else if (cell_dropdown_values_location.contains(workbook.getSheetName(0))) {
            dropdowns.add(cell_dropdown_values_location);
        }

        for (String elem : dropdowns) {
            CellReference cf = new CellReference(elem);
            Sheet s = workbook.getSheetAt(0);
            Row row = s.getRow(cf.getRow());
            Cell cell_1 = row.getCell(cf.getCol());
            cells.add(cell_1);
        }
        return cells;
    }

    /**
     * @param workbook_submission workbook of the submission
     * @param workbook_solution workbook of the solution
     * @param sheet_submission sheet of the submission
     * @param sheet_solution sheet of the solution
     * @return a String which gives feedback about the dropdowns of the sheet of the submission, by comparing the dropdowns with the dropdowns of the solution
     * Catches every Exception and prints feedback that the submission has syntax errors
     */
    public static String correctDropDown (XSSFWorkbook workbook_submission, XSSFWorkbook workbook_solution, Sheet sheet_submission, Sheet sheet_solution) throws Exception {
        try {

            List<Cell> dropdown_cells = FillColorHex.getDropdownCells(sheet_solution);

            FormulaEvaluator formulaEvaluator_submission = workbook_submission.getCreationHelper().createFormulaEvaluator();
            FormulaEvaluator formulaEvaluator_solution = workbook_solution.getCreationHelper().createFormulaEvaluator();

            String feedback = "Your Dropdown and the Values are correct !";

            for (Cell current_dropdown_cell_solution : dropdown_cells) {

                Cell current_dropdown_cell_submission = sheet_submission.getRow(current_dropdown_cell_solution.getRowIndex()).getCell(current_dropdown_cell_solution.getColumnIndex());
                if (isDropdown(sheet_submission, current_dropdown_cell_submission)) {
                    List<String> dropdownCells_submission = getDropdownValues(workbook_submission, sheet_submission, current_dropdown_cell_submission);
                    List<String> dropdownCells_solution = getDropdownValues(workbook_solution, sheet_solution, current_dropdown_cell_solution);


                    boolean correct_dropdown = true;
                    boolean correct_dropdown_values = true;

                    for (String elem : dropdownCells_solution) {
                        if ((!(dropdownCells_submission.contains(elem))) || dropdownCells_submission.size() != dropdownCells_solution.size()) {
                            correct_dropdown = false;
                            break;
                        }
                    }

                    if (correct_dropdown) {
                        for (String elem : dropdownCells_solution) {
                            sheet_solution.getRow(current_dropdown_cell_solution.getRowIndex()).getCell(current_dropdown_cell_solution.getColumnIndex()).setCellValue(elem);
                            sheet_submission.getRow(current_dropdown_cell_solution.getRowIndex()).getCell(current_dropdown_cell_solution.getColumnIndex()).setCellValue(elem);
                            for (Row currentRow : sheet_solution) {
                                for (Cell currentCell : currentRow) {
                                    Cell currentCell_submission = sheet_submission.getRow(currentCell.getRowIndex()).getCell(currentCell.getColumnIndex());
                                    if (currentCell_submission.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                        CorrectValues.checkFormula(formulaEvaluator_submission,currentCell_submission);
                                    }
                                    if (FillColorHex.isDropdownCell(sheet_solution, currentCell) && (!(currentCell.getColumnIndex() == current_dropdown_cell_solution.getColumnIndex() && currentCell.getRowIndex() == current_dropdown_cell_solution.getRowIndex()))) {
                                        sheet_submission.getRow(currentCell.getRowIndex()).getCell(currentCell.getColumnIndex()).setCellValue(currentCell.getStringCellValue());
                                    }
                                    if (currentCell.getCellType() == Cell.CELL_TYPE_FORMULA && currentCell_submission.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                        CorrectValues.checkFormula(formulaEvaluator_solution, currentCell);
                                        CorrectValues.checkFormula(formulaEvaluator_submission, currentCell_submission);
                                    }
                                    if (FillColorHex.isValueCell(sheet_solution, currentCell)) {
                                        // all Cells have to be evaluated because otherwise the different Dropdowns will not be checked
                                        formulaEvaluator_solution.evaluateAll();
                                        formulaEvaluator_submission.evaluateAll();
                                        if (!(CorrectValues.compareCells(currentCell, currentCell_submission)) || currentCell.getCellType() != currentCell_submission.getCellType()) {
                                                correct_dropdown_values = false;
                                        }

                                    }
                                }

                            }
                        }

                        if (!correct_dropdown_values) {
                            feedback = "The Values which are referring to your Dropdown are not correct !";
                        }
                    } else {
                        feedback = "Your Dropdown does not refer to the correct cells !";
                    }

                } else {
                    feedback = "You dont have a Dropdown in the right cell !";
                }
            }
            return feedback;
        } catch (Exception e) {
            return "Your submission has Syntax errors, please do not change the given Instruction, just change the values of the green and yellow Cells!";
        }
    }
}
