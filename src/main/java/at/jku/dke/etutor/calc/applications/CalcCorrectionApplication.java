package at.jku.dke.etutor.calc.applications;

import at.jku.dke.etutor.calc.functions.RandomInstructionImplementation;
import at.jku.dke.etutor.calc.models.RandomInstruction;
import at.jku.dke.etutor.calc.service.CorrectionService;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class CalcCorrectionApplication {

    private static final String INSTRUCTION_WRITER = "src/main/resources/Prototype/AngabeVerkaufszahlen.docx";
    private static final String SOLUTION_CALC = "src/main/resources/Prototype/LösungVerkaufszahlen.xlsx";
    private static final String INSTRUCTION_CALC = "src/main/resources/Prototype/AngabeVerkaufszahlen.xlsx";

    private static final String ANGABE  = "src/main/resources/Prototype/AngabeVerkaufszahlen.docx";
////    private static final String INSTRUCTION  = "src/main/resources/calc/C1_AngabeGesamtSS21.xlsx";
////    private static final String SOLUTION  = "src/main/resources/calc/C1_MusterlösungSS21.xlsx";
//    private static final String SOLUTION  = "src/main/resources/calc/randomSolution.xlsx";
//    private static final String INSTRUCTION  = "src/main/resources/calc/randomInstruction.xlsx";

    private static final String SOLUTION  = "src/main/resources/Prototype/LösungVerkaufszahlen.xlsx";
    private static final String INSTRUCTION  = "src/main/resources/Prototype/AngabeVerkaufszahlen.xlsx";


    public static void main(String[] args) throws Exception {

        FileInputStream solutionCalc = new FileInputStream(new File(SOLUTION_CALC));
        XSSFWorkbook workbook_solutionCalc = new XSSFWorkbook(solutionCalc);

        FileInputStream submissionCalc = new FileInputStream(new File(INSTRUCTION_CALC));
        XSSFWorkbook workbook_submissionCalc = new XSSFWorkbook(submissionCalc);

        FileInputStream instructionWriter = new FileInputStream(new File(INSTRUCTION_WRITER));
        XWPFDocument document_instructionWriter = new XWPFDocument(instructionWriter);

        CorrectionService.createInstruction(document_instructionWriter, workbook_submissionCalc, workbook_solutionCalc, "asdf" );

//        FileInputStream solutionCalc = new FileInputStream(new File(SOLUTION));
//        XSSFWorkbook workbook_solutionCalc = new XSSFWorkbook(solutionCalc);
//
//        FileInputStream submissionCalc = new FileInputStream(new File(INSTRUCTION));
//        XSSFWorkbook workbook_submissionCalc = new XSSFWorkbook(submissionCalc);

//        System.out.println(CorrectionService.runCorrection(document_instructionWriter, workbook_solutionCalc, workbook_submissionCalc).getTextualFeedback());
////
////
////
        FileInputStream angabe = new FileInputStream(new File(ANGABE));
        XWPFDocument document_angabe = new XWPFDocument(angabe);
//
////        System.out.println(RandomInstructionImplementation.pickRandomOptions(RandomInstructionImplementation.readPossibleParametersOfInstructionWriter(document_angabe), RandomInstructionImplementation.pickRandomSheet(RandomInstructionImplementation.readPossibleSheetOptionsOfInstructionWriter(document_angabe))));
//
//        RandomInstruction randomInstruction = RandomInstructionImplementation.createRandomInstruction(document_angabe, workbook_instructionCalc, workbook_solutionCalc, "asdf");
//
//

//        System.out.println(CalcCorrection.runCorrection());

//        System.out.println(CorrectionConfig.runCorrection(workbook_solutionCalc, workbook_instructionCalc));
        //TODO: how to use the new randomisation

//        FileInputStream instruction = new FileInputStream(new File(INSTRUCTION));
//        XSSFWorkbook workbook_instruction = new XSSFWorkbook(instruction);
//
//
//
//        FileInputStream solution = new FileInputStream(new File(SOLUTION));
//        XSSFWorkbook workbook_solution1 = new XSSFWorkbook(solution);
//
//        RandomInstruction randomInstruction = RandomInstructionImplementation.createRandomInstruction(document_angabe, workbook_submissionCalc, workbook_solutionCalc, "K11827238");
//
////        System.out.println(CalcCorrection.correctTask(document_instructionWriter, workbook_solutionCalc, workbook_submissionCalc));
////        System.out.println(CalculationCorrection.correctFormulasUse(workbook_solutionCalc, workbook_submissionCalc));
//
//
//
//        FileOutputStream out = new FileOutputStream("src/main/resources/instructions/instructionWriterNew.docx");
//        randomInstruction.getInstructionWriter().write(out);
//        out.close();
//
//        FileOutputStream outputStreamInstruction = new FileOutputStream("src/main/resources/instructions/instructionCalcNew.xlsx");
//        randomInstruction.getInstructionCalc().write(outputStreamInstruction);
//        outputStreamInstruction.close();
//
//
//        FileOutputStream outputStreamSolution = new FileOutputStream("src/main/resources/instructions/solutionCalcNew.xlsx");
//        randomInstruction.getSolutionCalc().write(outputStreamSolution);
//        outputStreamSolution.close();


//        FileInputStream solutionCalc = new FileInputStream(new File(SOLUTION));
//        XSSFWorkbook workbook_solutionCalc = new XSSFWorkbook(solutionCalc);
//
//        FileInputStream submissionCalc = new FileInputStream(new File(SUBMISSION));
//        XSSFWorkbook workbook_submissionCalc = new XSSFWorkbook(submissionCalc);
//
//        System.out.println(CalculationCorrection.isCorrectCalculated(workbook_solutionCalc, workbook_submissionCalc, workbook_solutionCalc.getSheetAt(0), workbook_submissionCalc.getSheetAt(0)));
//
//
//
//

    }

}
