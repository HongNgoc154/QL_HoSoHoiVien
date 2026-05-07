package Util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class HoiVienPdfExporter {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static class MemberProfileData {
        public String maHoiVien;
        public String tenHoiVien;
        public String gioiTinh;
        public String ngaySinh;
        public String sdt;
        public String email;
        public String diaChi;
        public String trangThai;
        public String ngayThamGia;
        public String hinhAnh;
    }

    public static void exportMemberProfile(MemberProfileData data, java.awt.Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("HoSo_HoiVien_" + safe(data.maHoiVien) + ".pdf"));
        if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try (OutputStream os = new FileOutputStream(file)) {
            Document doc = new Document(PageSize.A4, 36, 36, 30, 40);
            PdfWriter writer = PdfWriter.getInstance(doc, os);
            writer.setPageEvent(new WatermarkEvent());
            doc.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new BaseColor(25, 85, 166));
            Font sub = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.GRAY);
            Font label = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new BaseColor(52, 73, 94));
            Font val = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

            PdfPTable header = new PdfPTable(new float[]{4.6f, 1.4f});
            header.setWidthPercentage(100);
            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);
            left.setPaddingTop(8);
            Paragraph t = new Paragraph("HỒ SƠ HỘI VIÊN", title);
            t.setAlignment(Element.ALIGN_CENTER);
            Paragraph s = new Paragraph("Hệ thống quản lý hồ sơ hội viên", sub);
            s.setAlignment(Element.ALIGN_CENTER);
            left.addElement(t);
            left.addElement(s);

            PdfPCell right = new PdfPCell();
            right.setHorizontalAlignment(Element.ALIGN_CENTER);
            right.setVerticalAlignment(Element.ALIGN_MIDDLE);
            right.setPadding(6);
            right.setBorderColor(new BaseColor(170, 180, 195));
            Image img = loadMemberImage(data.hinhAnh);
            if (img != null) {
                img.scaleToFit(105, 150);
                img.setAlignment(Element.ALIGN_CENTER);
                right.addElement(img);
            } else {
                Paragraph p = new Paragraph("4x6", sub);
                p.setAlignment(Element.ALIGN_CENTER);
                right.addElement(new Paragraph(" "));
                right.addElement(p);
            }

            header.addCell(left);
            header.addCell(right);
            doc.add(header);
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(new float[]{1.7f, 3.8f});
            table.setWidthPercentage(100);
            table.setSpacingBefore(6);

            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("Mã hội viên", data.maHoiVien);
            fields.put("Tên hội viên", data.tenHoiVien);
            fields.put("Giới tính", data.gioiTinh);
            fields.put("Ngày sinh", data.ngaySinh);
            fields.put("Số điện thoại", data.sdt);
            fields.put("Email", data.email);
            fields.put("Địa chỉ", data.diaChi);
            fields.put("Trạng thái hội viên", data.trangThai);
            fields.put("Ngày tham gia", data.ngayThamGia);

            for (Map.Entry<String, String> e : fields.entrySet()) {
                PdfPCell c1 = new PdfPCell(new Phrase(e.getKey(), label));
                c1.setBackgroundColor(new BaseColor(245, 248, 252));
                c1.setPadding(8);
                c1.setBorderColor(new BaseColor(220, 228, 236));
                PdfPCell c2 = new PdfPCell(new Phrase(safe(e.getValue()), val));
                c2.setPadding(8);
                c2.setBorderColor(new BaseColor(220, 228, 236));
                table.addCell(c1);
                table.addCell(c2);
            }
            doc.add(table);

            Paragraph thanks = new Paragraph("Cảm ơn hội viên đã đồng hành cùng chúng tôi", sub);
            thanks.setSpacingBefore(18);
            thanks.setAlignment(Element.ALIGN_CENTER);
            doc.add(thanks);

            Paragraph footer = new Paragraph("Ngày xuất hồ sơ: " + LocalDate.now().format(DATE_FMT)
                + "\nNgười xuất hồ sơ: "
                + safe(
                    Session.getUser() != null
                    ? Session.getUser().getUsername()
                    : "Không xác định"
)
                + "\n\nNgười lập hồ sơ\n(Ký và ghi rõ họ tên)", val);
            footer.setSpacingBefore(24);
            footer.setAlignment(Element.ALIGN_RIGHT);
            doc.add(footer);

            doc.close();
            JOptionPane.showMessageDialog(parent, "Xuất hồ sơ PDF thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Lỗi xuất PDF: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String safe(String s) { return s == null || s.trim().isEmpty() ? "-" : s; }

    private static Image loadMemberImage(String path) {
        try {
            if (path == null || path.trim().isEmpty()) return null;
            if (path.startsWith("http://") || path.startsWith("https://")) return Image.getInstance(path);
            File f = new File(path);
            if (!f.exists()) return null;
            BufferedImage bi = ImageIO.read(f);
            if (bi == null) return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            return Image.getInstance(baos.toByteArray());
        } catch (Exception ex) {
            return null;
        }
    }

    private static class WatermarkEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContentUnder();
            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 52, new BaseColor(230, 235, 245));
            Phrase watermark = new Phrase("QUẢN LÝ HỘI VIÊN", font);
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, watermark,
                (document.right() + document.left()) / 2, (document.top() + document.bottom()) / 2, 35);
        }
    }
}
