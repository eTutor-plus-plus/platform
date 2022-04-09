package at.jku.dke.etutor.calc.functions;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CorrectDropDown {

    public static boolean isDropdown(Sheet sheet, Cell cell) {
        if (cell != null) {
            List<String> dropdown_list = new ArrayList<>();
            List<String> dropdowns = new ArrayList<>();
            List<XSSFDataValidation> dropdown = (List<XSSFDataValidation>) sheet.getDataValidations();
            char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            String cell_cordinates = Character.toString(alphabet[cell.getColumnIndex()]) + (cell.getRowIndex() + 1);

            for (XSSFDataValidation elem : dropdown) {
                CellRangeAddress[] list = elem.getRegions().getCellRangeAddresses();
                for (CellRangeAddress adr : list) {
                    dropdown_list.add(adr.formatAsString());
                }
            }

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

            return dropdowns.contains(cell_cordinates);
        } else {
            return false;
        }


    }

    public static List <String> getDropdownValues (XSSFWorkbook workbook, Sheet sheet) {
        List <Cell> dropdowns = getDropdownCells(workbook, sheet);
        List <String> dropdown_values = new ArrayList<>();
        for (Cell elem : dropdowns) {
            dropdown_values.add(elem.toString());
        }
        return dropdown_values;
    }

    public static List <String> getDropdownValues (XSSFWorkbook workbook, Sheet sheet, Cell cell) {
        List <Cell> dropdowns = getDropdownCells(workbook, sheet, cell);
        List <String> dropdown_values = new ArrayList<>();
        for (Cell elem : dropdowns) {
            dropdown_values.add(elem.toString());
        }
        return dropdown_values;
    }

    public static List<Cell> getDropdownCells (XSSFWorkbook workbook, Sheet sheet) {
        List<XSSFDataValidation> dropdown = (List<XSSFDataValidation>) sheet.getDataValidations();
//        String [] data = new String[0];
//        for (XSSFDataValidation elem : dropdown) {
//            data = elem.getValidationConstraint().getExplicitListValues();
//        }
        List <String> data = new ArrayList<>();
        for (XSSFDataValidation elem : dropdown) {
            data.add(elem.getValidationConstraint().getFormula1());
        }


        List <String> dropdowns = new ArrayList<>();
        List <Cell> cells = new ArrayList<>();


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

    public static List <Cell> getDropdownCells (XSSFWorkbook workbook, Sheet sheet, Cell cell) {
//        char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
//        String cell_position = alphabet[cell.getColumnIndex()] + Integer.toString(cell.getRowIndex()+1);
//
//        List<XSSFDataValidation> dropdown = (List<XSSFDataValidation>) sheet.getDataValidations();
//
//        int counter = 0;
//        int cell_position_list = 0;
//        List<String> dropdown_list = new ArrayList<>();
//        for (XSSFDataValidation elem : dropdown) {
//            CellRangeAddress[] list = elem.getRegions().getCellRangeAddresses();
//            for (CellRangeAddress adr : list) {
//                dropdown_list.add(adr.formatAsString());
//                if (Objects.equals(adr.formatAsString(), cell_position)) {
//                    cell_position_list = counter;
//                }
//                counter ++;
//            }
//
//        }
//
//
//        List <String []> data = new ArrayList<>();
//        for (XSSFDataValidation elem : dropdown) {
//            data.add(elem.getValidationConstraint().getExplicitListValues());
//        }
//
//        String cell_dropdown_values_location = data.get(cell_position_list)[0];
//
//
//        List <String> dropdowns = new ArrayList<>();
//        List <Cell> cells = new ArrayList<>();
//
//
//        if (cell_dropdown_values_location.contains(":")) {
//            String pattern = "([\\s\\S]+)!\\$(\\D)\\$(\\d*):\\$(\\D)\\$(\\d*)";
//            String sheet_name = cell_dropdown_values_location.replaceAll(pattern, "$1");
//            char v2 = cell_dropdown_values_location.replaceAll(pattern,"$4").charAt(0);
//            int n2 = Integer.parseInt(cell_dropdown_values_location.replaceAll(pattern,"$5"));
//
//            for (char v1 = cell_dropdown_values_location.replaceAll(pattern,"$2").charAt(0); v1 <= v2; v1++) {
//                for (int n1 = Integer.parseInt(cell_dropdown_values_location.replaceAll(pattern,"$3")); n1 <= n2; n1++) {
//                    dropdowns.add(sheet_name + "!" + "$" + v1 + "$" + Integer.toString(n1));
//                }
//            }
//        }
//        else if (cell_dropdown_values_location.contains(workbook.getSheetName(0))) {
//            dropdowns.add(cell_dropdown_values_location);
//        }
//
//
//
//        for (String elem : dropdowns) {
//            CellReference cf = new CellReference(elem);
//            Sheet s = workbook.getSheet(cf.getSheetName());
//            Row row = s.getRow(cf.getRow());
//            Cell cell_1 = row.getCell(cf.getCol());
//            cells.add(cell_1);
//        }
//        return cells;

        char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String cell_position = alphabet[cell.getColumnIndex()] + Integer.toString(cell.getRowIndex()+1);

        List<XSSFDataValidation> dropdown = (List<XSSFDataValidation>) sheet.getDataValidations();

        int counter = 0;
        int cell_position_list = 0;
        List<String> dropdown_list = new ArrayList<>();

        for (XSSFDataValidation elem : dropdown) {
            CellRangeAddress[] list = elem.getRegions().getCellRangeAddresses();
            for (CellRangeAddress adr : list) {
                dropdown_list.add(adr.formatAsString());
                if (Objects.equals(adr.formatAsString(), cell_position)) {
                    cell_position_list = counter;
                }
                counter ++;
            }

        }


        List <String> data = new ArrayList<>();
//        data.add(dropdown.get(1).getValidationConstraint().getFormula1());
//        for (XSSFDataValidation elem : dropdown) {
//            data.add(elem.getValidationConstraint().getFormula1());
//            System.out.println(elem.getValidationConstraint().getFormula1());
//        }

        for (XSSFDataValidation elem : dropdown) {
            if (elem.getValidationConstraint().getFormula1().contains("Hilfstabellen") && !(elem.getValidationConstraint().getFormula1()).contains("$A$3")) {
                data.add(elem.getValidationConstraint().getFormula1());
            }

        }

        String cell_dropdown_values_location = data.get(0);

//        System.out.println(cell_dropdown_values_location);


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
        else if (cell_dropdown_values_location.contains(workbook.getSheetName(0))) {
            dropdowns.add(cell_dropdown_values_location);
        }





        for (String elem : dropdowns) {
            CellReference cf = new CellReference(elem);
            String sn = cf.getSheetName();
            Sheet s = workbook.getSheetAt(0);
            Row row = s.getRow(cf.getRow());
            Cell cell_1 = row.getCell(cf.getCol());
            cells.add(cell_1);
        }
        return cells;
    }

    public static String correctDropDown (XSSFWorkbook workbook_submission, XSSFWorkbook workbook_solution, Sheet sheet_submission, Sheet sheet_solution) throws Exception {
//        try {
            List<Cell> dropdown_cells = FillColorHex.getDropdownCells(sheet_solution);
            List<Cell> value_cells = FillColorHex.getValueCells(sheet_solution);


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
                    boolean formula_check = true;

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
                                        CorrectValues.checkFormular(formulaEvaluator_submission,currentCell_submission);
                                    }
                                    if (FillColorHex.isDropdownCell(sheet_solution, currentCell) && (!(currentCell.getColumnIndex() == current_dropdown_cell_solution.getColumnIndex() && currentCell.getRowIndex() == current_dropdown_cell_solution.getRowIndex()))) {
                                        sheet_submission.getRow(currentCell.getRowIndex()).getCell(currentCell.getColumnIndex()).setCellValue(currentCell.getStringCellValue());
                                    }
                                    if (currentCell.getCellType() == Cell.CELL_TYPE_FORMULA && currentCell_submission.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                        CorrectValues.checkFormular(formulaEvaluator_solution, currentCell);
                                        CorrectValues.checkFormular(formulaEvaluator_submission, currentCell_submission);
                                    }
                                    if (FillColorHex.isValueCell(sheet_solution, currentCell)) {
                                        // all Cells have to be evaluated because otherwise the different Dropdowns will not be checked
                                        formulaEvaluator_solution.evaluateAll();
                                        formulaEvaluator_submission.evaluateAll();
                                        if (!(CorrectValues.compareCells(currentCell, currentCell_submission))) {
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
//        } catch (Exception e) {
//            return "Your submission has Syntax errors, please do not change the given Instruction, just change the values of the green and yellow Cells!";
//        }
    }
}
