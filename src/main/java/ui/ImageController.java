package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vision.ImageParser;
import vision.dto.Item;
import vision.exception.ChainNotDefinedException;
import vision.exception.ChainNotSupportedException;
import vision.exception.FailedToExtractImageTextException;
import vision.exception.FailedToInitException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ImageController implements Initializable {

    private ImageParser imageParser;

    private final AtomicInteger atomicInteger = new AtomicInteger();

    @FXML
    private TextArea textArea;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imageParser = new ImageParser();
    }

    @FXML
    protected void locateFile() throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        File file = chooser.showOpenDialog(new Stage());

        if (file != null && file.exists()) {

            Map<String, String> itemPerPrice;
            try {
                itemPerPrice = imageParser.extractTextFromPhoto(file);
            } catch (FailedToInitException e) {
                textArea.setText("Failed to init");
                return;
            } catch (FailedToExtractImageTextException | ChainNotDefinedException | ChainNotSupportedException e) {
                textArea.setText(e.getMessage());
                return;
            }

            String result = itemPerPrice.entrySet().stream()
                    .map(entry -> entry.getKey() + " = " + entry.getValue() + " ")
                    .collect(Collectors.joining("\n"));

            textArea.setText(result);
        }
    }

    @FXML
    protected void loadToExcel() throws Exception {
        if (textArea.getText().isEmpty()) {
            return;
        }

        List<Item> items = Arrays.stream(textArea.getText().split("\n"))
                .map(this::parseItem)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        String[] COLUMNs = {"Найменування", "Ціна"};

        Workbook workbook = new XSSFWorkbook();

        CreationHelper createHelper = workbook.getCreationHelper();

        Sheet sheet = workbook.createSheet("Список покупок");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int col = 0; col < COLUMNs.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(COLUMNs[col]);
            cell.setCellStyle(headerCellStyle);
        }

        CellStyle ageCellStyle = workbook.createCellStyle();
        ageCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#"));

        int rowIdx = 1;
        for (Item item : items) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(item.getName());
            row.createCell(1).setCellValue(item.getPrice());
        }

        FileOutputStream fileOut = new FileOutputStream("мої покупки-" + atomicInteger.getAndIncrement() + ".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }

    private Item parseItem(String item) {
        String[] split = item.split("=");

        if (split.length < 2) {
            return null;
        }

        return mapToItem(split[0], split[1]);
    }

    private Item mapToItem(String name, String price) {
        Item item = new Item();

        item.setName(name);
        item.setPrice(price);

        return item;
    }

    @FXML
    protected void clear() {
        textArea.clear();
    }
}
