package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.CorrectnessRule;
import at.jku.dke.etutor.calc.models.Feedback;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.XSSFPivotCacheDefinition;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataField;

import java.util.ArrayList;
import java.util.List;

public class PivotTableOptions extends CorrectnessRule {


    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return the Feedback regarding the correct pivot tables
     */
    @Override
    public Feedback checkCorrectness(XSSFWorkbook solution, XSSFWorkbook submission) throws Exception {
        return correctPivotTableOptions(solution, submission);
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return Feedback if the solution has the same pivot table settings as the submission
     * for more information check the functions above
     */
    public static Feedback correctPivotTableOptions (XSSFWorkbook solution, XSSFWorkbook submission) {
        if (!correctPivotTableDataFieldNames(solution, submission)) {
            return new Feedback(false, "The field names of your submission are not correct!");
        }
        if (!correctPivotTableAreaReference(solution, submission)) {
            return new Feedback(false, "The areas which are referenced by your pivot tables are not correct!");
        }
        return new Feedback(true, null);
    }

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return ture if the table name of the pivot table is the same in solution and submission
     */
    public static boolean correctPivotTableName (XSSFWorkbook solution, XSSFWorkbook submission) {

        int numberOfSheets = solution.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            if (solution.getSheetAt(i).getPivotTables() != null) {
                if (submission.getSheetAt(i).getPivotTables() != null) {
                    List<XSSFPivotTable> pivotTablesSolution = solution.getSheetAt(i).getPivotTables();
                    List<XSSFPivotTable> pivotTablesSubmission = submission.getSheetAt(i).getPivotTables();


                    List<String> pivotTableNamesSolution = new ArrayList<>();
                    List<String> pivotTableNamesSubmission = new ArrayList<>();

                    for (XSSFPivotTable pivotTableSolution : pivotTablesSolution) {
                        // get the name
                        pivotTableNamesSolution.add(pivotTableSolution.getCTPivotTableDefinition().getName());
                    }

                    for (XSSFPivotTable pivotTableSubmission : pivotTablesSubmission) {
                        pivotTableNamesSubmission.add(pivotTableSubmission.getCTPivotTableDefinition().getName());
                    }

                    if (pivotTableNamesSolution.size() != pivotTableNamesSubmission.size()) {
                        return false;
                    } else {
                        for (String elem : pivotTableNamesSolution) {
                            if (!pivotTableNamesSubmission.contains(elem)) {
                                return false;
                            }
                        }
                        for (String elem : pivotTableNamesSubmission) {
                            if (!pivotTableNamesSolution.contains(elem)) {
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

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return true if the data field names of the solution and submission are the same
     */
    public static boolean correctPivotTableDataFieldNames (XSSFWorkbook solution, XSSFWorkbook submission) {

        int numberOfSheets = solution.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            if (solution.getSheetAt(i).getPivotTables() != null) {
                if (submission.getSheetAt(i).getPivotTables() != null) {
                    List<XSSFPivotTable> pivotTablesSolution = solution.getSheetAt(i).getPivotTables();
                    List<XSSFPivotTable> pivotTablesSubmission = submission.getSheetAt(i).getPivotTables();

                    List<String> pivotTableDataFieldNamesSolution = new ArrayList<>();
                    List<String> pivotTableDataFieldNamesSubmission = new ArrayList<>();

                    for (XSSFPivotTable pivotTable : pivotTablesSolution) {
                        List<CTDataField> dataFieldList = pivotTable.getCTPivotTableDefinition().getDataFields().getDataFieldList();
                        for (CTDataField ctDataField : dataFieldList) {
                            pivotTableDataFieldNamesSolution.add(ctDataField.getName());
                        }
                    }

                    for (XSSFPivotTable pivotTable : pivotTablesSubmission) {
                        List<CTDataField> dataFieldList = pivotTable.getCTPivotTableDefinition().getDataFields().getDataFieldList();
                        for (CTDataField ctDataField : dataFieldList) {
                            pivotTableDataFieldNamesSubmission.add(ctDataField.getName());
                        }
                    }

                    if (pivotTableDataFieldNamesSolution.size() != pivotTableDataFieldNamesSubmission.size()) {
                        return false;
                    } else {
                        for (String elem : pivotTableDataFieldNamesSolution) {
                            if (!pivotTableDataFieldNamesSubmission.contains(elem)) {
                                return false;
                            }
                        }
                        for (String elem : pivotTableDataFieldNamesSubmission) {
                            if (!pivotTableDataFieldNamesSolution.contains(elem)) {
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

    /**
     * @param solution workbook of the solution
     * @param submission workbook of the submission
     * @return true if the area which references the pivot table do not differ between solution and submission
     */
    public static boolean correctPivotTableAreaReference (XSSFWorkbook solution, XSSFWorkbook submission) {
        int numberOfSheets = solution.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            if (solution.getSheetAt(i).getPivotTables() != null) {
                if (submission.getSheetAt(i).getPivotTables() != null) {
                    List<XSSFPivotTable> pivotTablesSolution = solution.getSheetAt(i).getPivotTables();
                    List<XSSFPivotTable> pivotTablesSubmission = submission.getSheetAt(i).getPivotTables();

                    List<String> pivotTableAreaReferencesSolution = new ArrayList<>();
                    List<String> pivotTableAreaReferencesSubmission = new ArrayList<>();

                    for (XSSFPivotTable pivotTable : pivotTablesSolution) {
                        for (org.apache.poi.ooxml.POIXMLDocumentPart documentPart : pivotTable.getRelations()) {
                            if (documentPart instanceof XSSFPivotCacheDefinition pivotCacheDefinition) {
                                pivotTableAreaReferencesSolution.add(pivotCacheDefinition.getPivotArea(solution).formatAsString());
                            }
                        }
                    }

                    for (XSSFPivotTable pivotTable : pivotTablesSubmission) {
                        for (org.apache.poi.ooxml.POIXMLDocumentPart documentPart : pivotTable.getRelations()) {
                            if (documentPart instanceof XSSFPivotCacheDefinition pivotCacheDefinition) {
                                pivotTableAreaReferencesSubmission.add(pivotCacheDefinition.getPivotArea(submission).formatAsString());
                            }
                        }
                    }

                    if (pivotTableAreaReferencesSolution.size() != pivotTableAreaReferencesSubmission.size()) {
                        return false;
                    } else {
                        for (String elem : pivotTableAreaReferencesSolution) {
                            if (!pivotTableAreaReferencesSubmission.contains(elem)) {
                                return false;
                            }
                        }
                        for (String elem : pivotTableAreaReferencesSubmission) {
                            if (!pivotTableAreaReferencesSolution.contains(elem)) {
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
