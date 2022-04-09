package at.jku.dke.etutor.calc.functions;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FillColorHex {

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

    public static boolean isDropdownCell (Sheet sheet, Cell cell) throws Exception {
        List <Cell> dropdowns = getDropdownCells(sheet);
        return dropdowns.contains(cell);
    }

    public static boolean isValueCell (Sheet sheet, Cell cell) throws Exception {
        List <Cell> value_cells = getValueCells(sheet);
        return value_cells.contains(cell);
    }

    public static boolean isCalculationCell (Sheet sheet, Cell cell) throws Exception {
        List <Cell> calculation_cells = getCalculationCells(sheet);
        return calculation_cells.contains(cell);
    }

    public static boolean isCalculationHelpCell (Sheet sheet, Cell cell) throws Exception {
        List <Cell> calculation_help_cells = getCalculationHelpCells(sheet);
        return calculation_help_cells.contains(cell);
    }








}
