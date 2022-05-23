package at.jku.dke.etutor.calc.functions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;

public class Testing {

    private static final String COLORS = "src/main/resources/calc/colors.xlsx";


    public static void main(String[] args) throws Exception {

        FileInputStream excelFile_instruction = new FileInputStream(new File(COLORS));
        XSSFWorkbook workbook_instruction = new XSSFWorkbook(excelFile_instruction);
        Sheet sheet = workbook_instruction.getSheetAt(0);

        for (Row row : sheet) {
            for (Cell cell : row) {
                System.out.println(cell.getCellStyle().getFillBackgroundColorColor());
            }
        }


    }
}
