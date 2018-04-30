package Module.core;

import Module.config.Properties;
import Module.entities.NetworkXML;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class XLSXReader {

    public void readFromCSV(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);
            Iterator<Row> rowIterator = mySheet.iterator();
            flag:
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                NetworkXML net = new NetworkXML();
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING:
                            if (cell.getStringCellValue().matches("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b")) {
                                net.setNetwork(cell.getStringCellValue().substring(0,
                                        cell.getStringCellValue().lastIndexOf(".")));
                                net.setRange(cell.getStringCellValue().substring(
                                        cell.getStringCellValue().lastIndexOf(".") + 1, cell.getStringCellValue().length()));
                            } else if (cell.getColumnIndex() == 0) {
                                continue flag;
                            } else {
                                net.setCommunity(cell.getStringCellValue());
                            }
                            break;
                        default:
                            continue flag;
                    }
                }
                if (net.getCommunity() == null || net.getCommunity().isEmpty()) {
                    net.setCommunity("public");
                }

                net.setPrinter(0);

                if (net.getNetwork() != null || net.getNetwork().isEmpty()) {
                    for (NetworkXML n : Properties.getNetworks()) {
                        if (n.getNetwork().equals(net.getNetwork()) && net.getRange().equals(n.getRange())) {
                            continue flag;
                        }
                    }
                    Properties.getNetworks().add(net);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
