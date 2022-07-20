package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

import java.util.ArrayList;
import java.util.List;

public class ChartOptions extends CorrectnessRule {



    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return the Feedback regarding the correct charts
     */
    @Override
    public Feedback checkCorrectness (XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        return correctChartOptions(solution, submission);
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return Feedback if the chart of the submission are the same as the charts of the solution regarding type, range, ....
     * for more information check the functions above
     */
    public static Feedback correctChartOptions (XSSFWorkbook solution, XSSFWorkbook submission) {
        if (!correctChartType(solution, submission)) {
            return new Feedback(false, "Your charts do not have the correct type!");
        }
        if (!correctChartRange(solution, submission)) {
            return new Feedback(false, "Your charts do not cover the correct range!");
        }
        if (!checkHasTitle(solution, submission)) {
            return new Feedback(false, "Your charts do not have a title!");
        }
        if (!checkNumberOfAxis(solution, submission)) {
            return new Feedback(false, "Your charts do not have the correct number of axis!");
        }
        return new Feedback(true, null);
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return true if the chart type of the solution is the same as the chart type of the submission
     */
    public static boolean correctChartType (XSSFWorkbook solution, XSSFWorkbook submission) {
        int numberOfSheets = solution.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            if (solution.getSheetAt(i).getDrawingPatriarch() != null) {
                if (submission.getSheetAt(i).getDrawingPatriarch() != null) {
                    List<XSSFChart> charts_solution = solution.getSheetAt(i).getDrawingPatriarch().getCharts();
                    List<XSSFChart> charts_submission = submission.getSheetAt(i).getDrawingPatriarch().getCharts();

                    if (charts_solution.size() == charts_submission.size()) {

                        for (XSSFChart chart_solution : charts_solution) {
                            // how to get the types of the charts
                            int chartSolutionIndex = charts_solution.indexOf(chart_solution);
                            CTPlotArea plotAreaSolution = chart_solution.getCTChart().getPlotArea();
                            CTPlotArea plotAreaSubmission = charts_submission.get(chartSolutionIndex).getCTChart().getPlotArea();
                            if (plotAreaSolution.getBarChartList().size() != plotAreaSubmission.getBarChartList().size()) {
                                return false;
                            } else if (plotAreaSolution.getDoughnutChartList().size() != plotAreaSubmission.getDoughnutChartList().size()) {
                                return false;
                            } else if (plotAreaSolution.getLineChartList().size() != plotAreaSubmission.getLineChartList().size()) {
                                return false;
                            } else if (plotAreaSolution.getPieChartList().size() != plotAreaSubmission.getPieChartList().size()) {
                                return false;
                            } else if (plotAreaSolution.getAreaChartList().size() != plotAreaSubmission.getAreaChartList().size()) {
                                return false;
                            } else if (plotAreaSolution.getRadarChartList().size() != plotAreaSubmission.getRadarChartList().size()) {
                                return false;
                            }

                        }
                    } else return false;
                }
                else return false;
            }
        }
        return true;
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return true if the range of the charts are the same
     */
    public static boolean correctChartRange (XSSFWorkbook solution, XSSFWorkbook submission) {
        int numberOfSheets = solution.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            if (solution.getSheetAt(i).getDrawingPatriarch() != null) {
                if (submission.getSheetAt(i).getDrawingPatriarch() != null) {
                    List<XSSFChart> charts_solution = solution.getSheetAt(i).getDrawingPatriarch().getCharts();
                    List<XSSFChart> charts_submission = submission.getSheetAt(i).getDrawingPatriarch().getCharts();

                    List<String> dataRangeReferenceListSolution = new ArrayList<>();
                    List<String> dataRangeReferenceListSubmission = new ArrayList<>();

                    for (XSSFChart chart_solution : charts_solution) {
                        // how to get the range of the chart
                        List<XDDFChartData> chartSeriesSolution = chart_solution.getChartSeries();
                        for (XDDFChartData chartData : chartSeriesSolution) {
                            for (int j = 0; j < chartData.getSeriesCount(); j++) {
                                XDDFChartData.Series series = chartData.getSeries(j);
                                dataRangeReferenceListSolution.add(series.getValuesData().getDataRangeReference());
                            }
                        }
                    }

                    for (XSSFChart chart_submission : charts_submission) {
                        // how to get the range of the chart
                        List<XDDFChartData> chartSeriesSubmission = chart_submission.getChartSeries();
                        for (XDDFChartData chartData : chartSeriesSubmission) {
                            for (int j = 0; j < chartData.getSeriesCount(); j++) {
                                XDDFChartData.Series series = chartData.getSeries(j);
                                dataRangeReferenceListSubmission.add(series.getValuesData().getDataRangeReference());
                            }
                        }
                    }

                    if (dataRangeReferenceListSolution.size() != dataRangeReferenceListSubmission.size()) {
                        return false;
                    } else {
                        for (String elem : dataRangeReferenceListSolution) {
                            if (!dataRangeReferenceListSubmission.contains(elem)) {
                                return false;
                            }
                        }
                        for (String elem : dataRangeReferenceListSubmission) {
                            if (!dataRangeReferenceListSolution.contains(elem)) {
                                return false;
                            }
                        }
                    }
                }
                else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return true if the charts of the submission have a title (of course just if the  charts of the solution have a title)
     */
    public static boolean checkHasTitle (XSSFWorkbook solution, XSSFWorkbook submission) {

        int numberOfSheets = solution.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            if (solution.getSheetAt(i).getDrawingPatriarch() != null) {
                if (submission.getSheetAt(i).getDrawingPatriarch() != null) {
                    List<XSSFChart> charts_solution = solution.getSheetAt(i).getDrawingPatriarch().getCharts();
                    List<XSSFChart> charts_submission = submission.getSheetAt(i).getDrawingPatriarch().getCharts();

                    List<Integer> solutionChartTitles = new ArrayList<>();
                    List<Integer> submissionChartTitles = new ArrayList<>();

                    for (XSSFChart chartSolution : charts_solution) {
                        if (chartSolution.getTitle() != null) {
                            solutionChartTitles.add(1);
                        }
                    }
                    for (XSSFChart chartSubmission : charts_submission) {
                        if (chartSubmission.getTitle() != null) {
                            submissionChartTitles.add(1);
                        }
                    }
                    if (solutionChartTitles.size() > submissionChartTitles.size()) {
                        return false;
                    }
                }
                else return false;
            }
        }
        return true;
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return true if the number of the axis of solution and submission are the same
     */
    public static boolean checkNumberOfAxis (XSSFWorkbook solution, XSSFWorkbook submission) {
        int numberOfSheets = solution.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            if (solution.getSheetAt(i).getDrawingPatriarch() != null) {
                if (submission.getSheetAt(i).getDrawingPatriarch() != null) {
                    List<XSSFChart> charts_solution = solution.getSheetAt(i).getDrawingPatriarch().getCharts();
                    List<XSSFChart> charts_submission = submission.getSheetAt(i).getDrawingPatriarch().getCharts();

                    List<Integer> numberOfAxisSolution = new ArrayList<>();
                    List<Integer> numberOfAxisSubmission = new ArrayList<>();

                    for (XSSFChart chart : charts_solution) {
                        numberOfAxisSolution.add(chart.getAxes().size());
                    }

                    for (XSSFChart chart : charts_submission) {
                        numberOfAxisSubmission.add(chart.getAxes().size());
                    }

                    if (numberOfAxisSolution.size() != numberOfAxisSubmission.size()) {
                        return false;
                    } else {
                        for (Integer elem : numberOfAxisSolution) {
                            if (!numberOfAxisSubmission.contains(elem)) {
                                return false;
                            }
                        }
                        for (Integer elem : numberOfAxisSubmission) {
                            if (!numberOfAxisSolution.contains(elem)) {
                                return false;
                            }
                        }
                    }
                }
                else return false;
            }
        }
        return true;
    }



}
