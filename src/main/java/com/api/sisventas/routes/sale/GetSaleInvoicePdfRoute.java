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

    @Operation(summary = "Factura en PDF", description = "Devuelve la factura de la venta como application/pdf")
    @GetMapping(value = "/api/v1/sales/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> handle(@PathVariable Long id) {
        String fileName = getSale.execute(id).saleNumber() + ".pdf";
        byte[] pdf = renderSaleInvoice.execute(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(fileName).build().toString())
                .body(pdf);
    }
}
