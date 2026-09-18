package com.api.sisventas.routes.sale;

import com.api.sisventas.businessLogic.sale.GetSale;
import com.api.sisventas.businessLogic.sale.RenderSaleInvoice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Sales")
public class GetSaleInvoicePdfRoute {

    private final RenderSaleInvoice renderSaleInvoice;
    private final GetSale getSale;

    public GetSaleInvoicePdfRoute(RenderSaleInvoice renderSaleInvoice, GetSale getSale) {
        this.renderSaleInvoice = renderSaleInvoice;
        this.getSale = getSale;
    }

    @Operation(summary = "Invoice PDF",
            description = "Returns the sale as application/pdf: format=invoice (A4, default) or format=receipt (80 mm)")
    @GetMapping(value = "/api/v1/sales/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> handle(@PathVariable Long id,
                                         @RequestParam(defaultValue = "invoice") String format) {
        RenderSaleInvoice.Format kind = "receipt".equalsIgnoreCase(format)
                ? RenderSaleInvoice.Format.RECEIPT : RenderSaleInvoice.Format.INVOICE;
        String suffix = kind == RenderSaleInvoice.Format.RECEIPT ? "-receipt.pdf" : ".pdf";
        String fileName = getSale.execute(id).saleNumber() + suffix;
        byte[] pdf = renderSaleInvoice.execute(id, kind);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(fileName).build().toString())
                .body(pdf);
    }
}
