package at.jku.dke.etutor.calc.functions;

import at.jku.dke.etutor.calc.models.RandomInstruction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomInstructionImplementation {

    /**
     * @param instructionWriter writer document of the instruction
     * @param instructionCalc calc document of the instruction
     * @param solutionCalc calc document of the solution
     * @param login id of the student
     * @return a RandomInstruction Object with the new writer instruction, new calc instruction and new calc solution
     */
    public static RandomInstruction createRandomInstruction (XWPFDocument instructionWriter, XSSFWorkbook instructionCalc, XSSFWorkbook solutionCalc, String login) throws Exception {

        List<String> sheetList = readPossibleSheetsOfInstructionWriter(instructionWriter);

        List<Map<String, List<String>>> mapList = readPossibleParametersOfInstructionWriter(instructionWriter);

        String pickedSheet = pickRandomSheet(sheetList);

        List <String> randomLocations = pickRandomOptions(mapList, pickedSheet).get(0);
        List <String> randomValues = pickRandomOptions(mapList, pickedSheet).get(1);

        String encryptedCode = createEncryptedCode(login);

        XWPFDocument newInstructionWriter = overrideInstructionWriterFooter(overrideInstructionWriterWithPickedOptions(instructionWriter, randomValues), login, encryptedCode);

        XSSFWorkbook newInstructionCalc = setEncryptedCodeCalcInstruction(randomiseValuesCalcInstruction(deleteNonPickedSheetsCalcInstruction(instructionCalc, pickedSheet)), encryptedCode);

        XSSFWorkbook newSolutionCalc =  overrideCalcSolutionWithPickedAndRandomisedValues(deleteNonPickedSheetsCalcSolution(solutionCalc, pickedSheet), newInstructionCalc, randomLocations, randomValues);


        return new RandomInstruction(newInstructionWriter, newInstructionCalc, newSolutionCalc);
    }



    /**
     * @param instructionWriter is the document where the instruction and all different parameters and sheets are defined
     * @return a List of Strings with all possible sheets defined in the instruction
     */
    public static List<String> readPossibleSheetsOfInstructionWriter (XWPFDocument instructionWriter) {
        List<XWPFParagraph> list = instructionWriter.getParagraphs();
        String paragraphWithSheets = "";
        for (XWPFParagraph paragraph : list) {
            if (paragraph.getText().contains("[") && paragraph.getText().contains("]") && paragraph.getText().contains(";") && !paragraph.getText().contains("{")) {
                paragraphWithSheets = paragraph.getText();
            }
        }

        // removing the brackets and the empty spaces
        paragraphWithSheets = paragraphWithSheets.replaceAll("\\[", "").replaceAll("]", "").replaceAll(" ", "");

        // array with all possible sheets

        return List.of(paragraphWithSheets.split(";"));
    }

    /**
     * @param instructionWriter is the document where the instruction and all different parameters and sheets are defined
     * @return a List of Strings with all possible sheets defined in the instruction
     */
    public static List<List<String>> readPossibleSheetOptionsOfInstructionWriter (XWPFDocument instructionWriter) {
        List<XWPFParagraph> list = instructionWriter.getParagraphs();
        String paragraphWithSheets = "";
        for (XWPFParagraph paragraph : list) {
            if (paragraph.getText().contains("{[") && paragraph.getText().contains("]}")) {
                paragraphWithSheets = paragraph.getText();
            }
        }

        final String regex = "\\[(.*?)]";

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(paragraphWithSheets);
        List<String> differentSheetCombinations = new ArrayList<>();

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                differentSheetCombinations.add(matcher.group(i));
            }
        }

        List<List<String>> listWithDifferentSheetCombinations = new ArrayList<>();

        for (String elem : differentSheetCombinations) {
            elem = elem.replaceAll(" ", "");
            listWithDifferentSheetCombinations.add(List.of(elem.split(";")));
        }

        return listWithDifferentSheetCombinations;

    }

    /**
     * @param instructionWriter is the document where the instruction and all different parameters are defined
     * @return a List of Maps with Strings and List of Strings with the location and the possible options defined in the instructionWriter
     */
    public static List<Map<String, List<String>>> readPossibleParametersOfInstructionWriter (XWPFDocument instructionWriter) {

        List<XWPFParagraph> list = instructionWriter.getParagraphs();
        List<String> paragraphsWithBrackets = new ArrayList<>();
        for (XWPFParagraph paragraph : list) {
            if (paragraph.getText().contains("[{") && paragraph.getText().contains("}]")) {
                paragraphsWithBrackets.add(paragraph.getText());
            }
        }


        // List of Maps with location and possible parameters
        List<Map<String, List<String>>> mapList = new ArrayList<>();


        for (String elem : paragraphsWithBrackets) {
            String regex = "(\\[\\{.*\\}])";

            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(elem);

            if (matcher.find()) {
                String allMatchesInParagraph = matcher.group(0);
                regex = "(\\[\\{.*?\\}])";

                pattern = Pattern.compile(regex, Pattern.MULTILINE);
                matcher = pattern.matcher(allMatchesInParagraph);

                List<String> singleMatchesList = new ArrayList<>();

                while (matcher.find()) {
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        singleMatchesList.add(matcher.group(i));
                    }
                }

                String location = "";
                String option = "";


                for (String singleMatch : singleMatchesList) {
                    regex = "\\{(.*?)\\{(.*?)\\}";

                    pattern = Pattern.compile(regex, Pattern.MULTILINE);
                    matcher = pattern.matcher(singleMatch);

                    List<String> options = new ArrayList<>();
                    Map<String, List<String>> map = new HashMap<>();
                    while (matcher.find()) {
                        for (int i = 1; i <= matcher.groupCount(); i++) {
                            if (i == 1) {
                                location = matcher.group(i).replaceAll(" ", "");
                            }
                            else {
                                option = matcher.group(i).replaceAll(" ","");
                                if (option.contains(";")) {
                                    String[] optionArray = option.split(";");
                                    options.addAll(Arrays.asList(optionArray));
                                }
                                else {
                                    options.add(option);
                                }
                            }

                        }
                        map.put(location, options);
                        options = new ArrayList<>();
                    }
                    mapList.add(map);
                }

            }
        }
        return mapList;
    }


    /**
     * @param sheetList randomly picks a sheet
     * @return a string with the picked sheet
     */
    public static String pickRandomSheet (List<String> sheetList) {
        int numberOfPossibleSheets = sheetList.size();

        // pick one sheet
        int randomNum = ThreadLocalRandom.current().nextInt(0, numberOfPossibleSheets);
        return sheetList.get(randomNum);
    }

    /**
     * @param mapList a list with all possible options of parameters and sheets
     * @param pickedSheet the picked sheet
     * @return a list of a list with strings
     * on the first place there are the random locations which are picked with the pickedSheet
     * on the second place there are the randomly picked options which are possible with the pickedSheet
     */
    public static List<List<String>> pickRandomOptions (List<Map<String, List<String>>> mapList, String pickedSheet) {
        List<List<String>> returningList  = new ArrayList<>();
        List <String> randomLocations = new ArrayList<>();
        List <String> randomValues = new ArrayList<>();


        for (Map<String, List<String>> map : mapList) {
            for (var entry : map.entrySet()) {
                String cellLocation = "";
                String cellValue = "";
                if (entry.getKey().contains(pickedSheet)) {
                    String regex = "!(.*)";

                    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(entry.getKey());

                    if (matcher.find()) {
                        if (matcher.groupCount() >= 1) {
                            cellLocation = matcher.group(1);
                        }
                    }

                    int numberOfOptions = entry.getValue().size();
                    int randomNumOptions = ThreadLocalRandom.current().nextInt(0, numberOfOptions);

                    cellValue = entry.getValue().get(randomNumOptions);
                    randomLocations.add(cellLocation);
                    randomValues.add(cellValue);
                }
            }
        }
        returningList.add(randomLocations);
        returningList.add(randomValues);

        return returningList;
    }

    /**
     * @param instructionWriter is the instruction writer document with all the possible options
     * @param randomValues are the picked random values
     * @return a writer document with the overwritten random values
     */
    public static XWPFDocument overrideInstructionWriterWithPickedOptions (XWPFDocument instructionWriter, List<String> randomValues) {
        int counterParagraph = 0;
        int counter = 0;
        int helpCounter = 0;

        for (XWPFParagraph paragraph : instructionWriter.getParagraphs()) {
            if (paragraph.getText().contains("[") && paragraph.getText().contains("]") && paragraph.getText().contains(";") && !paragraph.getText().contains("{")) {
                for (XWPFRun run : paragraph.getRuns()) {
                    run.setText("",0);
                }
            }
            else if (paragraph.getText().contains("[{") && paragraph.getText().contains("}]") && paragraph.getText().contains(";")) {
                String paragraphText = paragraph.getText();
                String regex = "\\[\\{.*?\\}]";
                final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                final Matcher matcher = pattern.matcher(paragraphText);
                counterParagraph = 0;
                while (matcher.find()) {
                    counterParagraph++;
                }

                while (helpCounter < counterParagraph) {
                    paragraphText = paragraphText.replaceFirst("\\[\\{.*?}]", randomValues.get(counter));
                    counter ++;
                    helpCounter ++;
                }
                helpCounter = 0;

                int help = 0;
                for (XWPFRun run : paragraph.getRuns()) {
                    if (help == 0) {
                        run.setText(paragraphText, 0);
                        help++;
                    }
                    else {
                        run.setText("", 0);
                    }
                }
            }
        }
        return instructionWriter;
    }


    /**
     * @param login is the student id
     * @return a code which is randomly generated and should identify the students instruction
     */
    public static String createEncryptedCode (String login) {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        SimpleDateFormat formatter2 = new SimpleDateFormat("yy-MM-dd");
        String currentDate = formatter2.format(date);
        int randomNumber = ThreadLocalRandom.current().nextInt(0, 100 + 1);
        return "#C" + randomNumber + "!" + login + "!" + currentDate;
    }

    /**
     * @param instructionWriter instruction writer document which should be overwritten
     * @param login id of the student
     * @param encryptedCode code identifies the student
     * @return a document which footer is overwritten
     */
    public static XWPFDocument overrideInstructionWriterFooter(XWPFDocument instructionWriter, String login, String encryptedCode) {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String currentDateTime = formatter.format(date);

        String footerText = login + "       " + currentDateTime + "       " + encryptedCode;

        // create header-footer
        XWPFHeaderFooterPolicy headerFooterPolicy = instructionWriter.getHeaderFooterPolicy();
        if (headerFooterPolicy == null) headerFooterPolicy = instructionWriter.createHeaderFooterPolicy();

        XWPFFooter footer = headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);

        XWPFParagraph paragraph = footer.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun run = paragraph.createRun();
        run.setText(footerText);


        CTSectPr sectPr = instructionWriter.getDocument().getBody().getSectPr();
        if (sectPr == null) sectPr = instructionWriter.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.getPgMar();
        if (pageMar == null) pageMar = sectPr.addNewPgMar();
        pageMar.setLeft(BigInteger.valueOf(720)); //720 TWentieths of an Inch Point (Twips) = 720/20 = 36 pt = 36/72 = 0.5"
        pageMar.setRight(BigInteger.valueOf(720));
        pageMar.setTop(BigInteger.valueOf(1440)); //1440 Twips = 1440/20 = 72 pt = 72/72 = 1"
        pageMar.setBottom(BigInteger.valueOf(1440));

        pageMar.setFooter(BigInteger.valueOf(568)); //28.4 pt * 20 = 568 = 28.4 pt footer from bottom

        return instructionWriter;
    }

    /**
     * @param instructionCalc the calc instruction
     * @param pickedSheet the name of the randomly picked sheet
     * @return the calc instruction with just the picked sheet, all other sheets got deleted
     */
    public static XSSFWorkbook deleteNonPickedSheetsCalcInstruction (XSSFWorkbook instructionCalc, String pickedSheet) {

        int numberOfSheets = instructionCalc.getNumberOfSheets();
        int sheetNumberStays = instructionCalc.getSheetIndex(instructionCalc.getSheet(pickedSheet));


        List<Integer> sheetNumbers = new ArrayList<>();
        int i = 0;
        while (i < numberOfSheets) {
            if (i != sheetNumberStays) {
                sheetNumbers.add(i);
            }
            i++;
        }

        for (int j : sheetNumbers) {
            instructionCalc.removeSheetAt(j);
        }

        return instructionCalc;
    }

    /**
     * @param instructionCalc the calc instruction
     * @return the calc instruction where all the yellow cells are randomised between 80 and 120 %
     */
    public static XSSFWorkbook randomiseValuesCalcInstruction (XSSFWorkbook instructionCalc) throws Exception {
        for (Row row : instructionCalc.getSheetAt(0)) {
            for (Cell cell : row) {
                if (FillColorHex.isCalculationHelpCell(instructionCalc.getSheetAt(0), cell)) {
                    if (cell.getCellType() == CellType.NUMERIC) {
                        Random r = new Random();
                        double rand =  0.8 + (1.2 - 0.8) * r.nextDouble();
                        cell.setCellValue(cell.getNumericCellValue() * Math.round(rand * 100.0) / 100.0);
                    }
                }
            }
        }
        return instructionCalc;
    }

    /**
     * @param instructionCalc the calc instruction
     * @param encryptedCode the encrypted code which identifies the student
     * @return the calc instruction which contains the encrypted code so in the correction it can be checked if it is the correct submission
     */
    public static XSSFWorkbook setEncryptedCodeCalcInstruction(XSSFWorkbook instructionCalc, String encryptedCode) {
        instructionCalc.getProperties().getCoreProperties().setKeywords(encryptedCode);
        return instructionCalc;
    }

    /**
     * @param solutionCalc the calc solution
     * @param pickedSheet the name of the picked sheet
     * @return the calc solution with just the picked sheet, all other sheets got deleted
     */
    public static XSSFWorkbook deleteNonPickedSheetsCalcSolution (XSSFWorkbook solutionCalc, String pickedSheet) {
        int numberOfSheets = solutionCalc.getNumberOfSheets();

        int sheetNumberStays = solutionCalc.getSheetIndex(solutionCalc.getSheet(pickedSheet));


        List<Integer> sheetNumbers = new ArrayList<>();
        int i = 0;
        while (i < numberOfSheets) {
            if (i != sheetNumberStays) {
                sheetNumbers.add(i);
            }
            i++;
        }

        for (int j : sheetNumbers) {
            solutionCalc.removeSheetAt(j);
        }
        return solutionCalc;
    }

    /**
     * @param solutionCalc the calc solution
     * @param instructionCalc the calc instruction
     * @param randomLocations the randomly picked locations
     * @param randomValues the randomly picked values
     * @return the calc solution with the overwritten random values of the calc instruction and the picked options of the wirter instruction
     */
    public static XSSFWorkbook overrideCalcSolutionWithPickedAndRandomisedValues (XSSFWorkbook solutionCalc, XSSFWorkbook instructionCalc, List<String> randomLocations, List<String> randomValues) throws Exception {

        // override the values cells
        for (Row row : instructionCalc.getSheetAt(0)) {
            for (Cell cell : row) {
                if (FillColorHex.isCalculationHelpCell(instructionCalc.getSheetAt(0), cell)) {
                    if (cell.getCellType() == CellType.NUMERIC) {
                        solutionCalc.getSheetAt(0).getRow(cell.getRowIndex()).getCell(cell.getColumnIndex()).setCellValue(cell.getNumericCellValue());
                    }
                }
            }
        }

        // override the values of the instruction writer

        for (String elem : randomLocations) {
            CellReference cellReference = new CellReference(elem);
            String value = randomValues.get(randomLocations.indexOf(elem));
            double doubleValue = 0.0;
            final String regex = "\\d";

            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                if (value.contains(",")) {
                    NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                    Number number = format.parse(value);
                    doubleValue = number.doubleValue();
                }
                else if (value.contains(".")) {
                    doubleValue = Double.parseDouble(value);
                }
            }
            if (doubleValue != 0.0) {
                solutionCalc.getSheetAt(0).getRow(cellReference.getRow()).getCell(cellReference.getCol()).setCellValue(doubleValue);
            }
            else {
                solutionCalc.getSheetAt(0).getRow(cellReference.getRow()).getCell(cellReference.getCol()).setCellValue(value);
            }
        }

        return solutionCalc;
    }



}
