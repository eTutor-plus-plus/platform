package at.jku.dke.etutor.calc.service;

import at.jku.dke.etutor.calc.config.CorrectionConfig;
import at.jku.dke.etutor.calc.exception.CreateRandomInstructionFailedException;
import at.jku.dke.etutor.calc.functions.RandomInstructionImplementation;
import at.jku.dke.etutor.calc.models.Feedback;
import at.jku.dke.etutor.calc.models.RandomInstruction;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class CorrectionService {

    /**
     * @param instructionWriter file of the instruction writer
     * @param solutionCalc file of the solution calc
     * @param submissionCalc file of the submission calc
     * @return Feedback of the submission
     */
    public static Feedback runCorrection (XWPFDocument instructionWriter, XSSFWorkbook solutionCalc, XSSFWorkbook submissionCalc) throws ClassNotFoundException {
        return CorrectionConfig.runCorrection(instructionWriter, solutionCalc, submissionCalc);
    }

    /**
     * @param instructionWriter file of the instruction writer
     * @param instructionCalc file of the instruction calc
     * @param solutionCalc file of the solution calc
     * @param login string of the currently logged in student
     * @return RandomInstruction with the randomised instruction for the student and the solution
     */
    public static RandomInstruction createInstruction (XWPFDocument instructionWriter, XSSFWorkbook instructionCalc, XSSFWorkbook solutionCalc, String login) throws CreateRandomInstructionFailedException {
        return  RandomInstructionImplementation.createRandomInstruction(instructionWriter, instructionCalc, solutionCalc, login);
    }
}
