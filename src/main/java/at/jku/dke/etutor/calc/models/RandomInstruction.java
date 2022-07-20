package at.jku.dke.etutor.calc.models;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class RandomInstruction {

    private XWPFDocument instructionWriter;

    private XSSFWorkbook instructionCalc;

    private XSSFWorkbook solutionCalc;

    public RandomInstruction(XWPFDocument instructionWriter, XSSFWorkbook instructionCalc, XSSFWorkbook solutionCalc) {
        this.instructionWriter = instructionWriter;
        this.instructionCalc = instructionCalc;
        this.solutionCalc = solutionCalc;
    }

    public XWPFDocument getInstructionWriter() {
        return instructionWriter;
    }

    public void setInstructionWriter(XWPFDocument instructionWriter) {
        this.instructionWriter = instructionWriter;
    }

    public XSSFWorkbook getInstructionCalc() {
        return instructionCalc;
    }

    public void setInstructionCalc(XSSFWorkbook instructionCalc) {
        this.instructionCalc = instructionCalc;
    }

    public XSSFWorkbook getSolutionCalc() {
        return solutionCalc;
    }

    public void setSolutionCalc(XSSFWorkbook solutionCalc) {
        this.solutionCalc = solutionCalc;
    }
}
