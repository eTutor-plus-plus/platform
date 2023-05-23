package at.jku.dke.etutor.calc.config;

import at.jku.dke.etutor.calc.functions.AdditionalFunctions;
import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import at.jku.dke.etutor.config.ApplicationProperties;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Objects;

public class CorrectionConfig {


    private static final String FILE;
    static {
       FILE = Objects.requireNonNullElse(System.getenv("CALC_CORRECTION_FILE_PATH"),
           "src/main/java/at/jku/dke/etutor/calc/config/CorrectionConfig.txt");
    }

    public static Feedback runCorrection (XWPFDocument instructionWriter,  XSSFWorkbook solution, XSSFWorkbook submission) throws ClassNotFoundException {

        try {

            if (!AdditionalFunctions.checkCryptoCode(instructionWriter, submission)) {
                return new Feedback(false, "Please use the instruction which was generated for you to solve the task!");
            }

            try (BufferedReader br = new BufferedReader(new FileReader(FILE, Charset.defaultCharset()))) {
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
