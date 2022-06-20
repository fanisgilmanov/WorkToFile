package guru.qa;


import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class FileParseTest {
    ClassLoader classLoader = FileParseTest.class.getClassLoader();
    @Test
    @DisplayName("Чтение из Zip-архива")
    void zipTest() throws Exception {
        try (InputStream is = classLoader.getResourceAsStream("TestZipFile.zip")) {
            assert is != null;
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("csv")) {
                    CSVReader csvReader = new CSVReader(new InputStreamReader(zis, UTF_8));
                    List<String[]> csv = csvReader.readAll();
                    assertThat(csv).contains(
                            new String[]{"username;password"},
                            new String[]{"test1;123456"},
                            new String[]{"test2;12345"},
                            new String[]{"test3;123"}
                    );
                } else if (entry.getName().contains("xlsx")) {
                    XLS xls = new XLS(zis);
                    assertThat(
                            xls.excel.getSheetAt(0)
                                    .getRow(2)
                                    .getCell(1)
                                    .getStringCellValue()
                    ).contains("Инга");
                } else if (entry.getName().contains("pdf")) {
                    PDF pdf = new PDF(zis);
                    assertThat(pdf.text).contains("документация!");
                }
            }
        }
    }

    @Test
    @DisplayName("Чтение из Json-файла")
    void jsonTestFile() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try(InputStream is = classLoader.getResourceAsStream("TestFile.json")){
            JsonNode jsonNode = objectMapper.readTree(new InputStreamReader(is, UTF_8));
            assertThat(jsonNode.withArray("CarOwners").get(2).findValue("name").asText())
                    .isEqualTo("Иван");
            assertThat(jsonNode.withArray("CarOwners").get(2).findValue("age").asText())
                    .isEqualTo("44");
            assertThat(jsonNode.withArray("CarOwners").get(2).findValue("onCredit").asBoolean())
                    .isEqualTo(false);
            assertThat(jsonNode.withArray("CarOwners").get(2).withArray("cars").get(1)
                    .findValue("name").asText()).isEqualTo("Lexus");

        }
    }

}

