package at.jku.dke.etutor.calc.service;

import at.jku.dke.etutor.calc.config.CorrectionConfig;
import at.jku.dke.etutor.calc.exception.CreateRandomInstructionFailedException;
import at.jku.dke.etutor.calc.functions.RandomInstructionImplementation;
import at.jku.dke.etutor.calc.models.Feedback;
import at.jku.dke.etutor.calc.models.RandomInstruction;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class CorrectionService {

    public static Feedback runCorrection (XWPFDocument instructionWriter, XSSFWorkbook solutionCalc, XSSFWorkbook submissionCalc) throws ClassNotFoundException {
        return CorrectionConfig.runCorrection(instructionWriter, solutionCalc, submissionCalc);
    }

    public static RandomInstruction createInstruction (XWPFDocument instructionWriter, XSSFWorkbook instructionCalc, XSSFWorkbook solutionCalc, String login) throws CreateRandomInstructionFailedException {
        System.out.println("asd");
        return  RandomInstructionImplementation.createRandomInstruction(instructionWriter, instructionCalc, solutionCalc, login);
    }
}
