import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import model.Items;
import model.Structure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.WorkWtithFiles;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;


public class FilesParsingTests {

    WorkWtithFiles workWithFiles = new WorkWtithFiles();

    @DisplayName("Проверка что zip архив содержит необходимые файлы")
    @Test
    void zipParsingTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(
                getClass().getResourceAsStream("/" + workWithFiles.getZipName())
        )) {
            ZipEntry entry;
            List<String> expectedFiles = List.of("selenide.pdf", "catalogloader.csv", "График отпусков_2017.xlsx");
            List<String> actualFiles = new ArrayList<>();

            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (!entryName.startsWith("__MACOSX/") && !entryName.endsWith("/")) {
                    actualFiles.add(entryName.replace("testobject/", ""));
                }
            }
            assertThat(actualFiles).containsExactlyInAnyOrderElementsOf(expectedFiles);
        }
    }

    @DisplayName("Проверка содержимого pdf файла")
    @Test
    void pdfParsingTest() throws Exception {
        try (InputStream pdfFile = workWithFiles.getFileFromZip("selenide.pdf")) {
            PDF pdf = new PDF(pdfFile);
            assertThat(pdf.producer).isEqualTo("Microsoft® Word 2016");
            assertThat(pdf.text.contains("Глава 1 Структура HTML-документа"));
        }
    }

    @DisplayName("Проверка содержимого xlsx файла")
    @Test
    void xlsxParsingTest() throws Exception {
        try (InputStream xlsxFile = workWithFiles.getFileFromZip("График отпусков_2017.xlsx")) {
            XLS xlsFile = new XLS(xlsxFile);
            String actualTitle1 = xlsFile.excel.getSheetAt(0).getRow(2).getCell(0).getStringCellValue();
            assertThat(actualTitle1).isEqualTo("График отпусков на 2017 г. Отдела ….");
        }
    }

    @DisplayName("Проверка содержимого csv файла")
    @Test
    void csvParsingTest() throws Exception {
        try (InputStream csvFile = workWithFiles.getFileFromZip("catalogloader.csv")) {
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(';')
                    .build();
            CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(csvFile))
                    .withCSVParser(parser)
                    .build();
            List<String[]> data = csvReader.readAll();
            assertThat(data.size()).isEqualTo(66);
            assertThat(data.get(0)[0]).isEqualTo("Категория");
        }
    }


    @DisplayName("Проверка содержимого json файла")
    @Test
    void jsonParsingTest() throws Exception {
        File jsonFile = new File("src/test/resources/MyJson.json");
        ObjectMapper objectMapper = new ObjectMapper();

        Structure structure = objectMapper.readValue(jsonFile, Structure.class);

        List<Items> itemsList = structure.getItems();
        assertThat(itemsList.size()).isEqualTo(2);

        Items firstItem = itemsList.get(0);
        assertThat(firstItem.getId()).isEqualTo("1");
        assertThat(firstItem.getLabel()).isEqualTo("Item 1");

        Items secondItem = itemsList.get(1);
        assertThat(secondItem.getId()).isEqualTo("2");
        assertThat(secondItem.getLabel()).isEqualTo("Item 2");
    }

}