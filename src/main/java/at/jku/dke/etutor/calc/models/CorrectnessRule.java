package at.jku.dke.etutor.calc.models;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class CorrectnessRule {

    public abstract Feedback checkCorrectness(XSSFWorkbook solution, XSSFWorkbook submission) throws Exception;
}
