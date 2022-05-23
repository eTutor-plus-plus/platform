package at.jku.dke.etutor.calc.functions;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateRandomInstruction {

    /**
     * @param workbook_instruction workbook of the instruction (which contains a randomised sheet)
     * @param workbook_solution workbook of the solution
     * @param workbook_submission workbook of the submsion
     * @return a List of the workbooks solution and submission which got be overwritten by the source of instruction
     * The function also overrides the functions of the solution because the references to the source are not correct anymore
     * and the function overrides the dates of the submission because in Example 5 & 6 it is possible to insert random dates
     */
    public static List<XSSFWorkbook> overriteWorkbooks (XSSFWorkbook workbook_instruction,  XSSFWorkbook workbook_solution, XSSFWorkbook workbook_submission) throws IOException {

        Sheet source_solution_old = workbook_solution.getSheetAt(0);
        int row_old= 0;
        int column_old = 0;

        for (Row row : source_solution_old) {
            for (Cell cell : row) {
                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && (row_old == 0 && column_old == 0)) {
                    row_old = cell.getRowIndex();
                    column_old = cell.getColumnIndex();
                    break;
                }
            }
        }

        Sheet instruction_sheet = workbook_instruction.getSheetAt(0);


        XSSFWorkbook solution = changeFirstSheet(instruction_sheet, workbook_solution);
        XSSFWorkbook submission = changeFirstSheet(instruction_sheet, workbook_submission);

        // override the formulas of the solution because the source was changed

        Sheet source_solution_new = solution.getSheetAt(0);

        int row_new = 0;
        int column_new = 0;

        // get the first numeric cell
        for (Row row : source_solution_new) {
            for (Cell cell : row) {
                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && (row_new == 0 && column_new == 0)) {
                    row_new = cell.getRowIndex();
                    column_new = cell.getColumnIndex();
                    break;
                }
            }
        }

        int row_difference = row_new - row_old ;
        int column_difference = column_new - column_old ;


        // This block is overriding every date of the submission workbook because in Example 5 it is possible to insert ony value (Date)

        int sheet_counter = 1;

        while (sheet_counter < workbook_solution.getNumberOfSheets()) {
            Sheet solution_sheet = workbook_solution.getSheetAt(sheet_counter);
            Sheet submission_sheet = workbook_submission.getSheetAt(sheet_counter);
            for (Row row : solution_sheet) {
                for (Cell cell : row) {
                    try {
                        if (DateUtil.isCellDateFormatted(cell)) {
                            Cell submission_cell = submission_sheet.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                            submission_cell.setCellValue(cell.getDateCellValue());
                        }
                    }
                    catch (Exception ignored){}
                }
            }
            sheet_counter ++;

        }

        //  This block is overriding every Formula in the workbook which contains the name of the source tab because of the random source

        sheet_counter = 1;
        FormulaEvaluator formulaEvaluator_solution = workbook_solution.getCreationHelper().createFormulaEvaluator();
        char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        List<Character> alphabet_list = new ArrayList<>();
        for (char elem : alphabet) {
            alphabet_list.add(elem);
        }

        while (sheet_counter < workbook_solution.getNumberOfSheets()) {
            Sheet solution_sheet = workbook_solution.getSheetAt(sheet_counter);
            for (Row row : solution_sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                        CorrectValues.checkFormula(formulaEvaluator_solution, cell);

                        String pattern1 = workbook_instruction.getSheetName(0) + "!(\\D)(\\d*):(\\D)(\\d*)";
                        String pattern2 = workbook_instruction.getSheetName(0) + "!(\\D)(\\d*)\\)";
                        if (cell.getCellFormula().matches(".*" + pattern1 + ".*")) {
                            Pattern pattern3 = Pattern.compile(pattern1);
                            Matcher matcher = pattern3.matcher(cell.getCellFormula());
                            StringBuffer sb = new StringBuffer();
                            while (matcher.find()) {
                                char v1 = matcher.group().replaceAll(pattern1, "$1").charAt(0);
                                v1 = alphabet_list.get(alphabet_list.indexOf(v1) + column_difference);
                                int n1 = Integer.parseInt(matcher.group().replaceAll(pattern1, "$2")) + row_difference;
                                char v2 = matcher.group().replaceAll(pattern1, "$3").charAt(0);
                                v2 = alphabet_list.get(alphabet_list.indexOf(v2) + column_difference);
                                int n2 = Integer.parseInt(matcher.group().replaceAll(pattern1, "$4")) + row_difference;
                                matcher.appendReplacement(sb, workbook_instruction.getSheetName(0) + "!" + v1 + n1 + ":" + v2 + n2);
                            }
                            matcher.appendTail(sb);
                            cell.setCellFormula(String.valueOf(sb));

                        } else if (cell.getCellFormula().matches(".*" + pattern2 + ".*")) {
                            Pattern pattern4 = Pattern.compile(pattern2);
                            Matcher matcher = pattern4.matcher(cell.getCellFormula());
                            StringBuffer sb = new StringBuffer();
                            while (matcher.find()) {
                                char v1 = matcher.group().replaceAll(pattern2, "$1").charAt(0);
                                v1 = alphabet_list.get(alphabet_list.indexOf(v1) + column_difference);
                                int n1 = Integer.parseInt(matcher.group().replaceAll(pattern2, "$2")) + row_difference;
                                matcher.appendReplacement(sb, workbook_instruction.getSheetName(0) + "!" + v1 + n1 + ")");
                            }
                            cell.setCellFormula(String.valueOf(sb));
                        }
                    }
                }
            }
            sheet_counter ++;

        }


        // This block is overriding every Dropdown in the workbook which contains the name of the source tab because of the random source

        sheet_counter = 1;

        while (sheet_counter < workbook_solution.getNumberOfSheets()) {
            Sheet solution_sheet = workbook_solution.getSheetAt(sheet_counter);
            List<XSSFDataValidation> dropdown = (List<XSSFDataValidation>) solution_sheet.getDataValidations();
            for (XSSFDataValidation elem : dropdown) {
                if (elem.getValidationConstraint().getFormula1().contains(workbook_instruction.getSheetName(0))) {
                    String pattern = workbook_instruction.getSheetName(0) + "!\\$(\\D)\\$(\\d*):\\$(\\D)\\$(\\d*)";
                    String formula = elem.getValidationConstraint().getFormula1();
                    char v1 = formula.replaceAll(pattern, "$1").charAt(0);
                    v1 = alphabet_list.get(alphabet_list.indexOf(v1) + column_difference);
                    int n1 = Integer.parseInt(formula.replaceAll(pattern, "$2"))+row_difference;
                    char v2 = formula.replaceAll(pattern, "$3").charAt(0);
                    v2 = alphabet_list.get(alphabet_list.indexOf(v2) + column_difference);
                    int n2 = Integer.parseInt(formula.replaceAll(pattern, "$4"))+ row_difference;
                    elem.getValidationConstraint().setFormula1(workbook_instruction.getSheetName(0) + "!$" + v1 + "$" + n1 + ":$" + v2 + "$" + n2 );

                    DataValidation xssfDataValidation = solution_sheet.getDataValidationHelper().createValidation(elem.getValidationConstraint(),elem.getRegions());
                    solution_sheet.addValidationData(xssfDataValidation);
                }
            }
            sheet_counter++;
        }

        List <XSSFWorkbook> workbooks = new ArrayList<>();
        workbooks.add(solution);
        workbooks.add(submission);

        return workbooks;
    }



    /**
     * @param instruction  is the base instruction which should be randomised
     * @param path is the path where the new instruction should be saved
     * @return the new instruction where all the yellow values are randomised
     */


    public static XSSFWorkbook createRandomInstruction (XSSFWorkbook instruction, String path) throws Exception {

        // Randomise a new Instruction
        Sheet datatypeSheet_instruction = instruction.getSheetAt(0);

        try {

            for (Row row : datatypeSheet_instruction) {
                for (Cell cell : row) {
                    if (FillColorHex.isValueCell(datatypeSheet_instruction, cell)) {
                        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            Random r = new Random();
                            double rand =  0.8 + (1.2 - 0.8) * r.nextDouble();
                            cell.setCellValue(cell.getNumericCellValue() * Math.round(rand * 100.0) / 100.0);
                        }
                    }
                }
            }


            instruction = createRandomLocation(instruction);

            FileOutputStream out = new FileOutputStream(path);
            instruction.write(out);
            out.close();

            return instruction;
        } catch (Exception e) {
            return instruction;
        }
    }

    /**
     * @param instruction  is the base instruction which should be randomised
     * @return the new instruction
     */

    public static XSSFWorkbook createRandomInstruction (XSSFWorkbook instruction) throws IOException {

        // TODO: Randomise a new Instruction
        Sheet datatypeSheet_instruction = instruction.getSheetAt(0);

        // Unterkunft pro Tag (zw. 20 - 200) (Row 2-4, Cell 3)
        datatypeSheet_instruction.getRow(2).getCell(3).setCellValue(Math.round(Math.random()*(200 - 20) + 20));
        datatypeSheet_instruction.getRow(3).getCell(3).setCellValue(Math.round(Math.random()*(200 - 20) + 20));
        datatypeSheet_instruction.getRow(4).getCell(3).setCellValue(Math.round(Math.random()*(200 - 20) + 20));

        // Kosten pro Person (zw. 5 - 50) (Row 2-4, Cell 4)
        datatypeSheet_instruction.getRow(2).getCell(4).setCellValue(Math.round(Math.random()*(50 - 5) + 5));
        datatypeSheet_instruction.getRow(3).getCell(4).setCellValue(Math.round(Math.random()*(50 - 5) + 5));
        datatypeSheet_instruction.getRow(4).getCell(4).setCellValue(Math.round(Math.random()*(50 - 5) + 5));

        // Rabattklasse (zw. 5 - 50) (Row 9, Cell 1-3)
        datatypeSheet_instruction.getRow(9).getCell(1).setCellValue(Math.round((Math.random() / 10.0)*100.00)/100.00);
        datatypeSheet_instruction.getRow(9).getCell(2).setCellValue(Math.round((Math.random() / 10.0)*100.00)/100.00);
        datatypeSheet_instruction.getRow(9).getCell(3).setCellValue(Math.round((Math.random() / 10.0)*100.00)/100.00);

        return instruction;
    }

    /**
     * @param workbook workboook where the Location of the cells should be randomised
     * @return a workbook where the Location of the cells is randomised
     */
    public static XSSFWorkbook createRandomLocation (XSSFWorkbook workbook) {
        XSSFWorkbook xssfWorkbook = workbook;
        xssfWorkbook.createSheet("Hilfstabellen1");

        Sheet sheet = xssfWorkbook.getSheetAt(xssfWorkbook.getNumberOfSheets()-1);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        int row_c = (int) Math.round(Math.random()*(10 - 1) + 1);
        int column_c = (int) Math.round(Math.random()*(10 - 1) + 1);

        for (Row row : datatypeSheet) {
            for (Cell cell : row) {

                if (sheet.getRow(cell.getRowIndex() + row_c) == null) {
                    sheet.createRow(cell.getRowIndex() + row_c);
                    sheet.getRow(cell.getRowIndex() + row_c).setHeight(datatypeSheet.getRow(cell.getRowIndex()).getHeight());

                }
                if (sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c) == null) {
                    sheet.getRow(cell.getRowIndex() + row_c).createCell(cell.getColumnIndex() + column_c);
                }
                if (sheet.getColumnWidth(cell.getColumnIndex() + column_c) != datatypeSheet.getColumnWidth(cell.getColumnIndex())) {
                    sheet.setColumnWidth(cell.getColumnIndex() + column_c, datatypeSheet.getColumnWidth(cell.getColumnIndex()));
                }

                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellValue(cell.getNumericCellValue());
                    CellStyle newStyle = xssfWorkbook.createCellStyle();
                    newStyle.cloneStyleFrom(cell.getCellStyle());
                    newStyle.setBorderBottom(cell.getCellStyle().getBorderBottom());
                    newStyle.setBorderLeft(cell.getCellStyle().getBorderLeft());
                    newStyle.setBorderRight(cell.getCellStyle().getBorderRight());
                    newStyle.setBorderTop(cell.getCellStyle().getBorderTop());
                    newStyle.setAlignment(cell.getCellStyle().getAlignment());
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellStyle(newStyle);
                }
                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellValue(cell.getStringCellValue());
                    CellStyle newStyle = xssfWorkbook.createCellStyle();
                    newStyle.cloneStyleFrom(cell.getCellStyle());
                    newStyle.setBorderBottom(cell.getCellStyle().getBorderBottom());
                    newStyle.setBorderLeft(cell.getCellStyle().getBorderLeft());
                    newStyle.setBorderRight(cell.getCellStyle().getBorderRight());
                    newStyle.setBorderTop(cell.getCellStyle().getBorderTop());
                    newStyle.setAlignment(cell.getCellStyle().getAlignment());
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellStyle(newStyle);
                }
                if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellValue("");
                    CellStyle newStyle = xssfWorkbook.createCellStyle();
                    newStyle.cloneStyleFrom(cell.getCellStyle());
                    newStyle.setBorderBottom(cell.getCellStyle().getBorderBottom());
                    newStyle.setBorderLeft(cell.getCellStyle().getBorderLeft());
                    newStyle.setBorderRight(cell.getCellStyle().getBorderRight());
                    newStyle.setBorderTop(cell.getCellStyle().getBorderTop());
                    newStyle.setAlignment(cell.getCellStyle().getAlignment());
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellStyle(newStyle);
                }
            }
        }
        for (int i=0; i < datatypeSheet.getNumMergedRegions(); i++) {
            CellRangeAddress copy = datatypeSheet.getMergedRegion(i);
            copy.setFirstColumn(datatypeSheet.getMergedRegion(i).getFirstColumn() + column_c);
            copy.setLastColumn(datatypeSheet.getMergedRegion(i).getLastColumn() + column_c);
            copy.setFirstRow(datatypeSheet.getMergedRegion(i).getFirstRow() + row_c);
            copy.setLastRow(datatypeSheet.getMergedRegion(i).getLastRow() + row_c);
            sheet.addMergedRegion(copy);
        }
        xssfWorkbook.removeSheetAt(0);
        xssfWorkbook.setSheetName(xssfWorkbook.getNumberOfSheets()-1, "Hilfstabellen");
        xssfWorkbook.getSheetAt(xssfWorkbook.getNumberOfSheets()-1).protectSheet("");
        xssfWorkbook.setSheetOrder("Hilfstabellen", 0);

        return xssfWorkbook;
    }

    /**
     * @param xssfWorkbook workbook where the values of the cells should be randomised
     * @param new_instruction_path path where the new workbook should be saved
     * @return a new workbook where the first sheet is randomised
     */
    public static Sheet randomiseSheet (XSSFWorkbook xssfWorkbook, String new_instruction_path) throws IOException {

        Sheet datatypeSheet = xssfWorkbook.getSheetAt(0);

        // Unterkunft pro Tag (zw. 20 - 200) (Row 2-4, Cell 3)
        datatypeSheet.getRow(2).getCell(3).setCellValue(Math.round(Math.random()*(200 - 20) + 20));
        datatypeSheet.getRow(3).getCell(3).setCellValue(Math.round(Math.random()*(200 - 20) + 20));
        datatypeSheet.getRow(4).getCell(3).setCellValue(Math.round(Math.random()*(200 - 20) + 20));

        // Kosten pro Person (zw. 5 - 50) (Row 2-4, Cell 4)
        datatypeSheet.getRow(2).getCell(4).setCellValue(Math.round(Math.random()*(50 - 5) + 5));
        datatypeSheet.getRow(3).getCell(4).setCellValue(Math.round(Math.random()*(50 - 5) + 5));
        datatypeSheet.getRow(4).getCell(4).setCellValue(Math.round(Math.random()*(50 - 5) + 5));

        // Rabattklasse (zw. 5 - 50) (Row 9, Cell 1-3)
        datatypeSheet.getRow(9).getCell(1).setCellValue(Math.round((Math.random() / 10.0)*100.00)/100.00);
        datatypeSheet.getRow(9).getCell(2).setCellValue(Math.round((Math.random() / 10.0)*100.00)/100.00);
        datatypeSheet.getRow(9).getCell(3).setCellValue(Math.round((Math.random() / 10.0)*100.00)/100.00);


        XSSFWorkbook workbook = xssfWorkbook;
        workbook.createSheet(datatypeSheet.getSheetName() + "1");
        Sheet sheet = workbook.getSheetAt(workbook.getNumberOfSheets() -1);

        int row_c = (int) Math.round(Math.random()*(10 - 1) + 1);
        int column_c = (int) Math.round(Math.random()*(10 - 1) + 1);

        for (Row row : datatypeSheet) {
            for (Cell cell : row) {

                if (sheet.getRow(cell.getRowIndex() + row_c) == null) {
                    sheet.createRow(cell.getRowIndex() + row_c);
                    sheet.getRow(cell.getRowIndex() + row_c).setHeight(datatypeSheet.getRow(cell.getRowIndex()).getHeight());

                }
                if (sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c) == null) {
                    sheet.getRow(cell.getRowIndex() + row_c).createCell(cell.getColumnIndex() + column_c);
                }
                if (sheet.getColumnWidth(cell.getColumnIndex() + column_c) != datatypeSheet.getColumnWidth(cell.getColumnIndex())) {
                    sheet.setColumnWidth(cell.getColumnIndex() + column_c, datatypeSheet.getColumnWidth(cell.getColumnIndex()));
                }

                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellValue(cell.getNumericCellValue());
                    CellStyle newStyle = workbook.createCellStyle();
                    newStyle.cloneStyleFrom(cell.getCellStyle());
                    newStyle.setBorderBottom(cell.getCellStyle().getBorderBottom());
                    newStyle.setBorderLeft(cell.getCellStyle().getBorderLeft());
                    newStyle.setBorderRight(cell.getCellStyle().getBorderRight());
                    newStyle.setBorderTop(cell.getCellStyle().getBorderTop());
                    newStyle.setAlignment(cell.getCellStyle().getAlignment());
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellStyle(newStyle);
                }
                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellValue(cell.getStringCellValue());
                    CellStyle newStyle = workbook.createCellStyle();
                    newStyle.cloneStyleFrom(cell.getCellStyle());
                    newStyle.setBorderBottom(cell.getCellStyle().getBorderBottom());
                    newStyle.setBorderLeft(cell.getCellStyle().getBorderLeft());
                    newStyle.setBorderRight(cell.getCellStyle().getBorderRight());
                    newStyle.setBorderTop(cell.getCellStyle().getBorderTop());
                    newStyle.setAlignment(cell.getCellStyle().getAlignment());
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellStyle(newStyle);
                }
                if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellValue("");
                    CellStyle newStyle = workbook.createCellStyle();
                    newStyle.cloneStyleFrom(cell.getCellStyle());
                    newStyle.setBorderBottom(cell.getCellStyle().getBorderBottom());
                    newStyle.setBorderLeft(cell.getCellStyle().getBorderLeft());
                    newStyle.setBorderRight(cell.getCellStyle().getBorderRight());
                    newStyle.setBorderTop(cell.getCellStyle().getBorderTop());
                    newStyle.setAlignment(cell.getCellStyle().getAlignment());
                    sheet.getRow(cell.getRowIndex() + row_c).getCell(cell.getColumnIndex() + column_c).setCellStyle(newStyle);
                }
            }
        }
        for (int i=0; i < datatypeSheet.getNumMergedRegions(); i++) {
            CellRangeAddress copy = datatypeSheet.getMergedRegion(i);
            copy.setFirstColumn(datatypeSheet.getMergedRegion(i).getFirstColumn() + column_c);
            copy.setLastColumn(datatypeSheet.getMergedRegion(i).getLastColumn() + column_c);
            copy.setFirstRow(datatypeSheet.getMergedRegion(i).getFirstRow() + row_c);
            copy.setLastRow(datatypeSheet.getMergedRegion(i).getLastRow() + row_c);
            sheet.addMergedRegion(copy);
        }

        workbook.removeSheetAt(0);
        workbook.setSheetName(xssfWorkbook.getNumberOfSheets()-1, "Hilfstabellen");
        workbook.getSheetAt(xssfWorkbook.getNumberOfSheets()-1).protectSheet("");
        workbook.setSheetOrder("Hilfstabellen", 0);

        FileOutputStream out = new FileOutputStream(new_instruction_path);
        workbook.write(out);
        out.close();

        return sheet;
    }


    /**
     * @param datatypeSheet new sheet which should be changed
     * @param workbook workbook which gets the new sheet
     * @return a new workbook with a new source sheet
     */
    public static XSSFWorkbook changeFirstSheet (Sheet datatypeSheet, XSSFWorkbook workbook) {

        // It is necessary to override the formulas because when it comes to changing the sheet name the formulas are evaluated
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                        CorrectValues.checkFormula(formulaEvaluator, cell);
                    }
                }
            }
        }

        String sheet_name = datatypeSheet.getSheetName();
        String new_name = sheet_name + "1";
        workbook.createSheet(new_name);
        Sheet sheet = workbook.getSheetAt(workbook.getNumberOfSheets()-1);

        for (Row row : datatypeSheet) {
            for (Cell cell : row) {

                if (sheet.getRow(cell.getRowIndex() ) == null) {
                    sheet.createRow(cell.getRowIndex() );
                    sheet.getRow(cell.getRowIndex() ).setHeight(datatypeSheet.getRow(cell.getRowIndex()).getHeight());

                }
                if (sheet.getRow(cell.getRowIndex() ).getCell(cell.getColumnIndex() ) == null) {
                    sheet.getRow(cell.getRowIndex() ).createCell(cell.getColumnIndex() );
                }
                if (sheet.getColumnWidth(cell.getColumnIndex() ) != datatypeSheet.getColumnWidth(cell.getColumnIndex())) {
                    sheet.setColumnWidth(cell.getColumnIndex() , datatypeSheet.getColumnWidth(cell.getColumnIndex()));
                }

                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    sheet.getRow(cell.getRowIndex() ).getCell(cell.getColumnIndex() ).setCellValue(cell.getNumericCellValue());
                }
                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    sheet.getRow(cell.getRowIndex() ).getCell(cell.getColumnIndex() ).setCellValue(cell.getStringCellValue());
                }
                if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                    sheet.getRow(cell.getRowIndex() ).getCell(cell.getColumnIndex() ).setCellValue("");
                }
            }
        }
        for (int i=0; i < datatypeSheet.getNumMergedRegions(); i++) {
            CellRangeAddress copy = datatypeSheet.getMergedRegion(i);
            copy.setFirstColumn(datatypeSheet.getMergedRegion(i).getFirstColumn() );
            copy.setLastColumn(datatypeSheet.getMergedRegion(i).getLastColumn() );
            copy.setFirstRow(datatypeSheet.getMergedRegion(i).getFirstRow() );
            copy.setLastRow(datatypeSheet.getMergedRegion(i).getLastRow() );
            sheet.addMergedRegion(copy);
        }

        String sheetName = workbook.getSheetName(0);
        workbook.removeSheetAt(0);
        workbook.setSheetOrder(new_name,0);
        workbook.setSheetName(0, sheetName);
        return workbook;
    }


}
