package Util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelExporter {

    /**
     * Export JTable to CSV file (opens save dialog)
     */
    public static void exportToCSV(JTable table, String defaultName, java.awt.Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(defaultName + "_" +
            new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".csv"));
        int result = fc.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), "UTF-8"))) {

            // BOM for Excel UTF-8
            pw.write('\uFEFF');

            DefaultTableModel model = (DefaultTableModel) table.getModel();

            // Header
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(model.getColumnName(i)).append("\"");
            }
            pw.println(sb);

            // Rows
            for (int r = 0; r < model.getRowCount(); r++) {
                sb = new StringBuilder();
                for (int c = 0; c < model.getColumnCount(); c++) {
                    if (c > 0) sb.append(",");
                    Object val = model.getValueAt(r, c);
                    String s = val == null ? "" : val.toString();
                    sb.append("\"").append(s.replace("\"", "\"\"")).append("\"");
                }
                pw.println(sb);
            }

            JOptionPane.showMessageDialog(parent,
                "Xuất file thành công!\n" + file.getAbsolutePath(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                "Lỗi xuất file: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}