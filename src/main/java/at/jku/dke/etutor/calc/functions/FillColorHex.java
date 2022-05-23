package at.jku.dke.etutor.calc.functions;

import org.apache.jena.base.Sys;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FillColorHex {

    /**
     * @param cell cell of which the color should be returned
     * @return String which expresses the color of the cell
     * This function got copied by the Internet
     */
    public static String getFillColorHex(Cell cell) throws Exception {
        String fillColorString = "none";
        if (cell != null) {
            CellStyle cellStyle = cell.getCellStyle();
            Color color =  cellStyle.getFillForegroundColorColor();
            if (color instanceof XSSFColor) {
                XSSFColor xssfColor = (XSSFColor)color;
                byte[] argb = xssfColor.getARgb();
                fillColorString = "[" + (argb[0]&0xFF) + ", " + (argb[1]&0xFF) + ", " + (argb[2]&0xFF) + ", " + (argb[3]&0xFF) + "]";
                if (xssfColor.getTint() != 0) {
                    fillColorString += " * " + xssfColor.getTint();
                    byte[] rgb = xssfColor.getRgbWithTint();
                    fillColorString += " = [" + (argb[0]&0xFF) + ", " + (rgb[0]&0xFF) + ", " + (rgb[1]&0xFF) + ", " + (rgb[2]&0xFF) + "]" ;
                }
            } else if (color instanceof HSSFColor) {
                HSSFColor hssfColor = (HSSFColor)color;
                short[] rgb = hssfColor.getTriplet();
                fillColorString = "[" + rgb[0] + ", " + rgb[1] + ", "  + rgb[2] + "]";
            }
        }
        return fillColorString;
    }

    /**
     * @return List of Cells of a sheet with green background-color
     */
    public static List<Cell> getDropdownCells (Sheet sheet) throws Exception {
        // [255, 0, 169, 51] -> green
        List <Cell> dropdowns = new ArrayList<>();
        for (Row current_row : sheet) {
            for (Cell current_cell : current_row) {
                if (Objects.equals(getFillColorHex(current_cell), "[255, 0, 169, 51]")) {
                    dropdowns.add(current_cell);
                }
            }
        }
        return dropdowns;
    }

    /**
     * @return List of Cells of a sheet with yellow background-color
     */
    public static List<Cell> getValueCells (Sheet sheet) throws Exception {
        // [255, 255, 255, 0] -> yellow
        List <Cell> value_cells = new ArrayList<>();
        for (Row current_row : sheet) {
            for (Cell current_cell : current_row) {
                if (Objects.equals(getFillColorHex(current_cell), "[255, 255, 255, 0]")) {
                    value_cells.add(current_cell);
                }
            }
        }
        return value_cells;
    }

    /**
     * @return List of Cells of a sheet with orange background-color
     */
    public static List<Cell> getCalculationCells (Sheet sheet) throws Exception {
        // [255, 255, 128, 0] -> orange
        List <Cell> value_cells = new ArrayList<>();
        for (Row current_row : sheet) {
            for (Cell current_cell : current_row) {
                if (Objects.equals(getFillColorHex(current_cell), "[255, 255, 128, 0]")) {
                    value_cells.add(current_cell);
                }
            }
        }
        return value_cells;
    }

    /**
     * @return List of Cells of a sheet with red background-color
     */
    public static List<Cell> getCalculationHelpCells (Sheet sheet) throws Exception {
        // [255, 255, 0, 0] -> red
        List <Cell> value_cells = new ArrayList<>();
        for (Row current_row : sheet) {
            for (Cell current_cell : current_row) {
                if (Objects.equals(getFillColorHex(current_cell), "[255, 255, 0, 0]")) {
                    value_cells.add(current_cell);
                }
            }
        }
        return value_cells;
    }

    /**
     * @return true, when the cell is a dropDownCell
     */
    public static boolean isDropdownCell (Sheet sheet, Cell cell) throws Exception {
        List <Cell> dropdowns = getDropdownCells(sheet);
        return dropdowns.contains(cell);
    }

    /**
     * @return true, when the cell is a valueCell
     */
    public static boolean isValueCell (Sheet sheet, Cell cell) throws Exception {
        List <Cell> value_cells = getValueCells(sheet);
        return value_cells.contains(cell);
    }

    /**
     * @return true, when the cell is a calculationCell
     */
    public static boolean isCalculationCell (Sheet sheet, Cell cell) throws Exception {
        List <Cell> calculation_cells = getCalculationCells(sheet);
        return calculation_cells.contains(cell);
    }

    /**
     * @return true, when the cell is a calculationHelpCell
     */
    public static boolean isCalculationHelpCell (Sheet sheet, Cell cell) throws Exception {
        List <Cell> calculation_help_cells = getCalculationHelpCells(sheet);
        return calculation_help_cells.contains(cell);
    }

    public static boolean isSheetUnchanged (Sheet solution, Sheet submission) throws Exception {
        for (Row current_row_solution : solution) {
            for (Cell current_cell_solution : current_row_solution) {
                if (isDropdownCell(solution, current_cell_solution) != isDropdownCell(submission, submission.getRow(current_cell_solution.getRowIndex()).getCell(current_cell_solution.getColumnIndex()))) {
                    return false;
                }
                if (isCalculationCell(solution, current_cell_solution) != isCalculationCell(submission, submission.getRow(current_cell_solution.getRowIndex()).getCell(current_cell_solution.getColumnIndex()))) {
                    return false;
                }
                if (isValueCell(solution, current_cell_solution) != isValueCell(submission, submission.getRow(current_cell_solution.getRowIndex()).getCell(current_cell_solution.getColumnIndex()))) {
                    return false;
                }
                if (isCalculationHelpCell(solution, current_cell_solution) != isCalculationHelpCell(submission, submission.getRow(current_cell_solution.getRowIndex()).getCell(current_cell_solution.getColumnIndex()))) {
                    return false;
                }
            }
        }
        return true;
    }








}
