package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.models.Sale;
import com.api.sisventas.models.SaleItem;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dibuja la factura de una venta en PDF. Pinta exactamente lo que la venta guardó
 * (líneas con su precio congelado y el total calculado): no recalcula nada.
 */
@Service
public class RenderSaleInvoice {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'");
    private static final float MARGIN = 36f;
    private static final float TITLE_SIZE = 18f;
    private static final float BODY_SIZE = 10f;
    private static final float SPACING = 12f;
    private static final float TOTAL_SIZE = 13.5f;
    private static final int FULL_WIDTH_PERCENT = 100;
    private static final float[] COLUMN_WIDTHS = {2f, 5f, 1.2f, 2f, 2f};
    private static final Color HEADER_BACKGROUND = new Color(230, 230, 230);

    private final SaleRepository saleRepository;
    private final String businessName;

    public RenderSaleInvoice(SaleRepository saleRepository,
                             @Value("${app.invoice.business-name}") String businessName) {
        this.saleRepository = saleRepository;
        this.businessName = businessName;
    }

    @Transactional(readOnly = true)
    public byte[] execute(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.SALE_NOT_FOUND));
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, MARGIN, MARGIN, MARGIN, MARGIN);
            PdfWriter.getInstance(document, output);
            document.open();
            addHeader(document, sale);
            addItems(document, sale.getItems());
            addTotal(document, sale.getTotal());
            document.close();
            return output.toByteArray();
        } catch (DocumentException | java.io.IOException error) {
            throw new IllegalStateException("No se pudo generar el PDF de la venta " + sale.getSaleNumber(), error);
        }
    }

    private void addHeader(Document document, Sale sale) throws DocumentException {
        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, TITLE_SIZE);
        Font body = FontFactory.getFont(FontFactory.HELVETICA, BODY_SIZE);

        Paragraph name = new Paragraph(businessName, title);
        name.setSpacingAfter(SPACING);
        document.add(name);

        document.add(new Paragraph("Factura " + sale.getSaleNumber(), body));
        document.add(new Paragraph("Fecha: " + DATE_FORMAT.format(sale.getSaleDate().atZone(ZoneOffset.UTC)), body));
        if (sale.getUser() != null) {
            document.add(new Paragraph(
                    "Atendido por: " + sale.getUser().getFirstName() + " " + sale.getUser().getLastName(), body));
        }
        Paragraph spacer = new Paragraph(" ", body);
        spacer.setSpacingAfter(SPACING);
        document.add(spacer);
    }

    private void addItems(Document document, List<SaleItem> items) throws DocumentException {
        Font header = FontFactory.getFont(FontFactory.HELVETICA_BOLD, BODY_SIZE);
        Font body = FontFactory.getFont(FontFactory.HELVETICA, BODY_SIZE);

        PdfPTable table = new PdfPTable(COLUMN_WIDTHS);
        table.setWidthPercentage(FULL_WIDTH_PERCENT);
        for (String label : new String[] {"SKU", "Producto", "Cant.", "Precio unit.", "Subtotal"}) {
            PdfPCell cell = new PdfPCell(new Phrase(label, header));
            cell.setBackgroundColor(HEADER_BACKGROUND);
            table.addCell(cell);
        }
        for (SaleItem item : items) {
            table.addCell(new Phrase(item.getProduct().getSku(), body));
            table.addCell(new Phrase(item.getProduct().getName(), body));
            table.addCell(rightAligned(String.valueOf(item.getQuantity()), body));
            table.addCell(rightAligned(item.getUnitPrice().toPlainString(), body));
            table.addCell(rightAligned(item.getSubtotal().toPlainString(), body));
        }
        document.add(table);
    }

    private void addTotal(Document document, BigDecimal total) throws DocumentException {
        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, TOTAL_SIZE);
        Paragraph paragraph = new Paragraph("Total: " + total.toPlainString(), bold);
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        paragraph.setSpacingBefore(SPACING);
        document.add(paragraph);
    }

    private PdfPCell rightAligned(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
}
