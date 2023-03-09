package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConditionalFormattingOptions extends CorrectnessRule {

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return the Feedback regarding the correct conditional formatting options
     */
    @Override
    public Feedback checkCorrectness(XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        return correctConditionalFormattingOptions(solution, submission);
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return Feedback if the conditional formatting of solution and submission are the same
     * "Bedingte Formatierung"
     */
    public static Feedback correctConditionalFormattingOptions (XSSFWorkbook solution, XSSFWorkbook submission) {

        for (Sheet sheetSolution : solution) {
            SheetConditionalFormatting conditionalFormattingSheetSolution = sheetSolution.getSheetConditionalFormatting();
            Sheet sheetSubmission = submission.getSheetAt(solution.getSheetIndex(sheetSolution.getSheetName()));
            SheetConditionalFormatting conditionalFormattingSheetSubmission = sheetSubmission.getSheetConditionalFormatting();
            List<ConditionalFormatting> conditionalFormattingListSolution = new ArrayList<>();
            List<ConditionalFormatting> conditionalFormattingListSubmission = new ArrayList<>();

            for (int i = 0; i < conditionalFormattingSheetSolution.getNumConditionalFormattings(); i++) {
                conditionalFormattingListSolution.add(conditionalFormattingSheetSolution.getConditionalFormattingAt(i));
            }

            for (int i = 0; i < conditionalFormattingSheetSubmission.getNumConditionalFormattings(); i++) {
                conditionalFormattingListSubmission.add(conditionalFormattingSheetSubmission.getConditionalFormattingAt(i));
            }

            // check if the solution and the submission has the same amount of conditional formatting rules
            if (conditionalFormattingListSolution.size() != conditionalFormattingListSubmission.size()) {
                return new Feedback(false, "Your submission has not the correct number of conditional formatting rules!");
            }
            // assumption: the formatting rules of the submission are in the same order as the rules of the submission
            else {
                for (ConditionalFormatting conditionalFormattingSolution : conditionalFormattingListSolution) {
                    // get the rule of the submission with the same index as the rule of the solution
                    ConditionalFormatting conditionalFormattingSubmission = conditionalFormattingListSubmission.get(conditionalFormattingListSolution.indexOf(conditionalFormattingSolution));

                    // check for the correct place of the conditional formatting
                    if (!Arrays.toString(conditionalFormattingSolution.getFormattingRanges()).equals(Arrays.toString(conditionalFormattingSubmission.getFormattingRanges()))) {
                        return new Feedback(false, "Your conditional formatting cells are not on the correct place!");
                    }

                    List<ConditionalFormattingRule> conditionalFormattingRuleListSolution = new ArrayList<>();
                    List<ConditionalFormattingRule> conditionalFormattingRuleListSubmission = new ArrayList<>();

                    // get all the rules of the solution
                    for (int i = 0; i < conditionalFormattingSolution.getNumberOfRules(); i++) {
                        conditionalFormattingRuleListSolution.add(conditionalFormattingSolution.getRule(i));
                    }

                    // get all the rules of the submission
                    for (int i = 0; i < conditionalFormattingSubmission.getNumberOfRules(); i++) {
                        conditionalFormattingRuleListSubmission.add(conditionalFormattingSubmission.getRule(i));
                    }

                    for (ConditionalFormattingRule conditionalFormattingRuleSolution : conditionalFormattingRuleListSolution) {

                        // get the rule of the submission with the same index as the rule of the solution
                        ConditionalFormattingRule conditionalFormattingRuleSubmission = conditionalFormattingRuleListSubmission.get(conditionalFormattingRuleListSolution.indexOf(conditionalFormattingRuleSolution));

                        if (conditionalFormattingRuleSolution.getConditionType() != conditionalFormattingRuleSubmission.getConditionType()) {
                            return new Feedback(false, "Your conditional formatting's do not have the correct condition type!");
                        }

                        if (conditionalFormattingRuleSolution.getConditionType() == ConditionType.COLOR_SCALE) {

                            // check for the correct colors (just for the amount of the colors, because the code of the color does differ from the version of calc)
                            Color[] colorsSolution = conditionalFormattingRuleSolution.getColorScaleFormatting().getColors();
                            Color[] colorsSubmission = conditionalFormattingRuleSolution.getColorScaleFormatting().getColors();

                            if (colorsSolution.length != colorsSubmission.length) {
                                return new Feedback(false, "The colors of your conditional formatting's are not correct!");
                            }

                            // check for the correct number of control points
                            if (conditionalFormattingRuleSolution.getColorScaleFormatting().getNumControlPoints() != conditionalFormattingRuleSubmission.getColorScaleFormatting().getNumControlPoints()) {
                                return new Feedback(false, "The colors of your conditional formatting's are not correct!");
                            }


                            // check for the thresholds
                            ConditionalFormattingThreshold [] conditionalFormattingThresholdsSolution = conditionalFormattingRuleSolution.getColorScaleFormatting().getThresholds();
                            ConditionalFormattingThreshold [] conditionalFormattingThresholdsSubmission = conditionalFormattingRuleSubmission.getColorScaleFormatting().getThresholds();

                            for (int i = 0; i < conditionalFormattingThresholdsSolution.length; i ++) {
                                ConditionalFormattingThreshold conditionalFormattingThresholdSolution = conditionalFormattingThresholdsSolution[i];
                                ConditionalFormattingThreshold conditionalFormattingThresholdSubmission = conditionalFormattingThresholdsSubmission[i];

                                if (!Objects.equals(conditionalFormattingThresholdSolution.getFormula(), conditionalFormattingThresholdSubmission.getFormula())) {
                                    return new Feedback(false, "The formulas of your conditional formatting are not correct!");
                                }

                                if (!Objects.equals(conditionalFormattingThresholdSolution.getValue(), conditionalFormattingThresholdSubmission.getValue())) {
                                    return new Feedback(false, "The formulas of your conditional formatting are not correct!");
                                }

                                if (!conditionalFormattingThresholdSolution.getRangeType().toString().equals(conditionalFormattingThresholdSubmission.getRangeType().toString())) {
                                    return new Feedback(false, "The formulas of your conditional formatting are not correct!");
                                }
                            }
                        }

                        if (conditionalFormattingRuleSolution.getConditionType() == ConditionType.CELL_VALUE_IS) {

                            // check for the first formula
                            if (!Objects.equals(conditionalFormattingRuleSolution.getFormula1(), conditionalFormattingRuleSubmission.getFormula1())) {
                                return new Feedback(false, "The formulas of your conditional formatting are not correct!");
                            }

                            // check for the second formula
                            if (!Objects.equals(conditionalFormattingRuleSolution.getFormula2(), conditionalFormattingRuleSubmission.getFormula2())) {
                                return new Feedback(false, "The formulas of your conditional formatting are not correct!");
                            }

                            // check for the formatting

//                            if (!Objects.equals(conditionalFormattingRuleSolution.getPatternFormatting().getFillBackgroundColorColor(), conditionalFormattingRuleSubmission.getPatternFormatting().getFillBackgroundColorColor())) {
//                                return false;
//                            }
//
//                            if (!Objects.equals(conditionalFormattingRuleSolution.getPatternFormatting().getFillForegroundColorColor(), conditionalFormattingRuleSubmission.getPatternFormatting().getFillForegroundColorColor())) {
//                                return false;
//                            }

                            // just check if the colors are set and not if it is the same color
                            if (conditionalFormattingRuleSolution.getPatternFormatting().getFillForegroundColorColor() != null && conditionalFormattingRuleSubmission.getPatternFormatting().getFillForegroundColorColor() == null) {
                                return new Feedback(false, "The impacts of your conditional formatting's are not correct!");
                            }

                            if (conditionalFormattingRuleSolution.getPatternFormatting().getFillBackgroundColorColor() != null && conditionalFormattingRuleSubmission.getPatternFormatting().getFillBackgroundColorColor() == null) {
                                return new Feedback(false, "The impacts of your conditional formatting's are not correct!");
                            }

                            // TODO: many possible extensions
                        }

                        // TODO: many possible extensions

                    }



                }

            }

        }
        return new Feedback(true, null);
    }
}
