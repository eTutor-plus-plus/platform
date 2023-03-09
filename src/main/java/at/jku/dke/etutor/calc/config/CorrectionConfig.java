package at.jku.dke.etutor.calc.config;

import at.jku.dke.etutor.calc.functions.AdditionalFunctions;
import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class CorrectionConfig {


    private static final String FILE  = "src/main/java/at/jku/dke/etutor/calc/config/CorrectionConfig.txt";

    public static Feedback runCorrection (XWPFDocument instructionWriter,  XSSFWorkbook solution, XSSFWorkbook submission) throws ClassNotFoundException {

        try {

            if (!AdditionalFunctions.checkCryptoCode(instructionWriter, submission)) {
                return new Feedback(false, "Please use the instruction which was generated for you to solve the task!");
            }

            try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    CorrectnessRule correctnessRule = (CorrectnessRule) Class.forName(line).getDeclaredConstructor().newInstance();
                    if (!correctnessRule.checkCorrectness(solution, submission).isCorrect()) {
                        return correctnessRule.checkCorrectness(solution, submission);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new Feedback(false, "Your submission has Syntax Errors, please Contact the Admin of your Program!");
            }
        } catch (Exception e) {
            return new Feedback(false, "Your submission has Syntax Errors, please Contact the Admin of your Program!");
        }
        return new Feedback(true, "Congratulation! Your Submission is correct");
    }
}
