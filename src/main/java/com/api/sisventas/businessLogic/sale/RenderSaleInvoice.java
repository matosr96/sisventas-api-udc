package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.models.PaymentMethod;
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
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Dibuja la factura de una venta en PDF: tamaño carta (A4) o tirilla de 80 mm para la
 * impresora térmica de la caja. Pinta exactamente lo que la venta guardó (líneas con su
 * precio congelado, descuento, impuesto, total, forma de pago y cambio): no recalcula nada.
 */
@Service
public class RenderSaleInvoice {

    public enum Format { INVOICE, RECEIPT }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    /** 80 mm de ancho en puntos (72 por pulgada); el alto es generoso, la impresora corta el papel. */
    private static final float RECEIPT_WIDTH = 226.8f;
    private static final float RECEIPT_HEIGHT = 1200f;
    private static final float MARGIN = 36f;
    private static final float RECEIPT_MARGIN = 10f;
    private static final float TITLE_SIZE = 18f;
    private static final float RECEIPT_TITLE_SIZE = 12f;
    private static final float BODY_SIZE = 10f;
    private static final float RECEIPT_BODY_SIZE = 8f;
    private static final float SPACING = 12f;
    private static final float TOTAL_SIZE = 13.5f;
    private static final int FULL_WIDTH_PERCENT = 100;
    private static final float[] COLUMN_WIDTHS = {2f, 5f, 1.2f, 2f, 2f};
    private static final float[] RECEIPT_COLUMN_WIDTHS = {4f, 1f, 2f, 2f};
    private static final float[] TOTALS_WIDTHS = {3f, 2f};
    private static final Color HEADER_BACKGROUND = new Color(230, 230, 230);
    private static final Map<PaymentMethod, String> PAYMENT_LABELS = Map.of(
            PaymentMethod.CASH, "Efectivo", PaymentMethod.CARD, "Tarjeta", PaymentMethod.TRANSFER, "Transferencia");

    private final SaleRepository saleRepository;
    private final String businessName;
    private final String currency;
    private final ZoneId zone;

    public RenderSaleInvoice(SaleRepository saleRepository,
                             @Value("${app.invoice.business-name}") String businessName,
                             @Value("${app.invoice.currency}") String currency,
                             @Value("${app.business.time-zone}") String zone) {
        this.saleRepository = saleRepository;
        this.businessName = businessName;
        this.currency = currency;
        this.zone = ZoneId.of(zone);
    }

    @Transactional(readOnly = true)
    public byte[] execute(Long id, Format format) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new DomainError(ErrorCodes.SALE_NOT_FOUND));
        boolean receipt = format == Format.RECEIPT;
        Rectangle page = receipt ? new Rectangle(RECEIPT_WIDTH, RECEIPT_HEIGHT) : PageSize.A4;
        float margin = receipt ? RECEIPT_MARGIN : MARGIN;
        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, receipt ? RECEIPT_TITLE_SIZE : TITLE_SIZE);
        Font body = FontFactory.getFont(FontFactory.HELVETICA, receipt ? RECEIPT_BODY_SIZE : BODY_SIZE);
        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, receipt ? RECEIPT_BODY_SIZE : BODY_SIZE);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, receipt ? RECEIPT_TITLE_SIZE : TOTAL_SIZE);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(page, margin, margin, margin, margin);
            PdfWriter.getInstance(document, output);
            document.open();
            addHeader(document, sale, title, body);
            addItems(document, sale.getItems(), receipt, bold, body);
            addTotals(document, sale, body, bold, totalFont);
            document.close();
            return output.toByteArray();
        } catch (DocumentException | java.io.IOException error) {
            throw new IllegalStateException("No se pudo generar el PDF de la venta " + sale.getSaleNumber(), error);
        }
    }

    private void addHeader(Document document, Sale sale, Font title, Font body) throws DocumentException {
        Paragraph name = new Paragraph(businessName, title);
        name.setSpacingAfter(SPACING);
        document.add(name);
        document.add(new Paragraph("Factura " + sale.getSaleNumber(), body));
        document.add(new Paragraph("Fecha: " + DATE_FORMAT.format(sale.getSaleDate().atZone(zone)), body));
        document.add(new Paragraph(
                "Atendido por: " + sale.getUser().getFirstName() + " " + sale.getUser().getLastName(), body));
        if (sale.getCustomerName() != null) {
            document.add(new Paragraph("Cliente: " + sale.getCustomerName(), body));
        }
        Paragraph spacer = new Paragraph(" ", body);
        spacer.setSpacingAfter(SPACING);
        document.add(spacer);
    }

    private void addItems(Document document, List<SaleItem> items, boolean receipt, Font header, Font body)
            throws DocumentException {
        PdfPTable table = new PdfPTable(receipt ? RECEIPT_COLUMN_WIDTHS : COLUMN_WIDTHS);
        table.setWidthPercentage(FULL_WIDTH_PERCENT);
        String[] labels = receipt
                ? new String[] {"Producto", "Cant.", "Precio", "Subtotal"}
                : new String[] {"SKU", "Producto", "Cant.", "Precio unit.", "Subtotal"};
        for (String label : labels) {
            PdfPCell cell = new PdfPCell(new Phrase(label, header));
            cell.setBackgroundColor(HEADER_BACKGROUND);
            table.addCell(cell);
        }
        for (SaleItem item : items) {
            if (!receipt) {
                table.addCell(new Phrase(item.getProduct().getSku(), body));
            }
            table.addCell(new Phrase(item.getProduct().getName(), body));
            table.addCell(rightAligned(String.valueOf(item.getQuantity()), body));
            table.addCell(rightAligned(money(item.getUnitPrice()), body));
            table.addCell(rightAligned(money(item.getSubtotal()), body));
        }
        document.add(table);
    }

    private void addTotals(Document document, Sale sale, Font body, Font bold, Font totalFont)
            throws DocumentException {
        PdfPTable totals = new PdfPTable(TOTALS_WIDTHS);
        totals.setWidthPercentage(FULL_WIDTH_PERCENT);
        totals.setSpacingBefore(SPACING);
        totals.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        addTotalRow(totals, "Subtotal", money(sale.getSubtotal()), body);
        if (sale.getDiscount().signum() > 0) {
            addTotalRow(totals, "Descuento", "-" + money(sale.getDiscount()), body);
        }
        if (sale.getTaxRate().signum() > 0) {
            addTotalRow(totals, "Impuesto (" + sale.getTaxRate().stripTrailingZeros().toPlainString() + "%)",
                    money(sale.getTax()), body);
        }
        addTotalRow(totals, "Total", money(sale.getTotal()), totalFont);
        addTotalRow(totals, "Pago", PAYMENT_LABELS.getOrDefault(sale.getPaymentMethod(), "Efectivo"), bold);
        if (sale.getAmountPaid() != null) {
            addTotalRow(totals, "Recibido", money(sale.getAmountPaid()), body);
            addTotalRow(totals, "Cambio", money(sale.getChangeAmount()), body);
        }
        document.add(totals);
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell left = new PdfPCell(new Phrase(label, font));
        left.setBorder(Rectangle.NO_BORDER);
        left.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell right = new PdfPCell(new Phrase(value, font));
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(left);
        table.addCell(right);
    }

    private String money(BigDecimal value) {
        return value.toPlainString() + " " + currency;
    }

    private PdfPCell rightAligned(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
}
