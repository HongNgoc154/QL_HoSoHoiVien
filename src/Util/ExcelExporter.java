package Util;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelExporter {

    /**
     * Export JTable to CSV file (opens save dialog)
     */
    public static void exportToCSV(JTable table, String defaultName, java.awt.Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(defaultName + "_" +
            new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".xlsx"));
        int result = fc.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        try (Workbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            Sheet sheet = wb.createSheet(defaultName);
            // Header
            CellStyle headerStyle = wb.createCellStyle();
            Font hFont = wb.createFont();
            hFont.setBold(true);
            headerStyle.setFont(hFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < model.getColumnCount(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(model.getColumnName(i));
                cell.setCellStyle(headerStyle);
            }
//            pw.println(sb);

            // Rows
            for (int r = 0; r < model.getRowCount(); r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < model.getColumnCount(); c++) {
//                    if (c > 0) sb.append(",");
                    Object val = model.getValueAt(r, c);
                    row.createCell(c).setCellValue(val == null ? "" : val.toString());
                }
//                pw.println(sb);
            }
            
            for (int i = 0; i < model.getColumnCount(); i++) sheet.autoSizeColumn(i);
            wb.write(fos);

            JOptionPane.showMessageDialog(parent,
                "Xuất file Excel thành công!\n" + file.getAbsolutePath(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                "Lỗi xuất file Excel: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}