package at.jku.dke.etutor.helper;

import at.jku.dke.etutor.service.dto.courseinstance.StudentImportDTO;
import at.jku.dke.etutor.service.exception.StudentCSVImportException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Helper class for handling CSV files.
 *
 * @author fne
 */
public class CSVHelper {

    public static final String CSV_FILE_FORMAT = "text/csv";

    public static final String COL_MATRICULATION_NUMBER = "MATRIKELNR";
    public static final String COL_FIRST_NAME = "VORNAME";
    public static final String COL_LAST_NAME = "NACHNAME";
    public static final String COL_EMAIL = "EMAIL";

    public static final String[] COLUMN_NAMES = new String[]{COL_MATRICULATION_NUMBER, COL_FIRST_NAME, COL_LAST_NAME, COL_EMAIL};

    /**
     * Checks whether the given multipart file has the correct
     * CSV content type or not.
     *
     * @param file the file to check
     * @return {@code true} if the file has the correct content type, otherwise {@code false}
     */
    public static boolean hasCSVFileFormat(MultipartFile file) {
        Objects.requireNonNull(file);

        return CSV_FILE_FORMAT.equals(file.getContentType());
    }

    /**
     * Loads the students from the given multipart csv file.
     *
     * @param file the csv file
     * @return list of students
     * @throws StudentCSVImportException if an import error occurs
     */
    public static List<StudentImportDTO> getStudentsFromCSVFile(MultipartFile file) throws StudentCSVImportException {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1));
            CSVParser csvParser = new CSVParser(
                reader,
                CSVFormat.DEFAULT
                    .withDelimiter(';')
                    .withNullString("")
                    .withAllowDuplicateHeaderNames(false)
                    .withTrim(true)
                    .withFirstRecordAsHeader()
                    .withSkipHeaderRecord(true)
            )
        ) {
            List<String> csvColumnHeaders = csvParser.getHeaderNames();

            if (!CollectionUtils.subtract(Arrays.asList(COLUMN_NAMES), csvColumnHeaders).isEmpty()) {
                throw new StudentCSVImportException();
            }

            List<StudentImportDTO> students = new ArrayList<>();
            for (CSVRecord csvRecord : csvParser.getRecords()) {
                String matriculationNumber = csvRecord.get(COL_MATRICULATION_NUMBER);
                String firstName = csvRecord.get(COL_FIRST_NAME);
                String lastName = csvRecord.get(COL_LAST_NAME);
                String email = csvRecord.get(COL_EMAIL);

                if (!matriculationNumber.toLowerCase().startsWith("k")) {
                    matriculationNumber = "k" + matriculationNumber;
                }

                students.add(new StudentImportDTO(firstName, lastName, matriculationNumber, email));
            }
            return students;
        } catch (final IOException ioException) {
            throw new StudentCSVImportException();
        }
    }

    /**
     * Private constructor.
     */
    private CSVHelper() {
        // No instantiation of this class is allowed.
    }
}
