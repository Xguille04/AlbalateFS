package com.albalatefs.backend.service;

import com.albalatefs.backend.model.Socio;
import com.albalatefs.backend.payload.PedidoRequest;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EmailService {

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${mail.from.email:guilleayuda04@gmail.com}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private void sendEmail(String to, String subject, String html,
                           byte[] attachmentBytes, String attachmentFilename) {
        try {
            if (brevoApiKey == null || brevoApiKey.isBlank() || brevoApiKey.startsWith("YOUR_")) {
                System.err.println("[EMAIL] BREVO_API_KEY no configurada correctamente — email a " + to + " no enviado.");
                return;
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("sender", Map.of("name", "Albalate FS", "email", fromEmail));
            body.put("to", List.of(Map.of("email", to)));
            body.put("subject", subject);
            body.put("htmlContent", html);

            if (attachmentBytes != null && attachmentFilename != null) {
                body.put("attachment", List.of(Map.of(
                        "content", Base64.getEncoder().encodeToString(attachmentBytes),
                        "name", attachmentFilename
                )));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    BREVO_URL, new HttpEntity<>(body, headers), Map.class);

            System.out.println("[EMAIL] Enviado a " + to + " — status: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("[EMAIL ERROR] Error enviando email a " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarConfirmacionSocio(Socio socio) {
        try {
            byte[] pdfBytes = generarCarnetPdf(socio);

            String numeroSocio = String.format("ALB-%04d", socio.getId());
            String fechaAlta = socio.getFechaAlta() != null
                    ? new SimpleDateFormat("dd/MM/yyyy").format(socio.getFechaAlta())
                    : "-";

            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head><meta charset="UTF-8"></head>
                    <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                      <table width="100%%" bgcolor="#f4f4f4" cellpadding="0" cellspacing="0">
                        <tr><td align="center" style="padding:40px 20px;">
                          <table width="600" bgcolor="#ffffff" cellpadding="0" cellspacing="0" style="border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1);">
                            <tr><td bgcolor="#1e3a5f" style="padding:32px 40px;text-align:center;">
                              <h1 style="color:#f59e0b;margin:0;font-size:28px;letter-spacing:2px;">ALBALATE FS</h1>
                              <p style="color:#93c5fd;margin:8px 0 0;font-size:14px;letter-spacing:1px;">CLUB DE FÚTBOL SALA</p>
                            </td></tr>
                            <tr><td style="padding:40px;">
                              <h2 style="color:#1e3a5f;font-size:22px;margin:0 0 16px;">¡Bienvenido, %s! 🎉</h2>
                              <p style="color:#555;line-height:1.7;font-size:15px;">
                                Ya eres parte de la familia del <strong>Albalate FS</strong>. Tu solicitud de socio ha sido registrada y el pago procesado correctamente.
                              </p>
                              <table width="100%%" style="margin:24px 0;background:#1e3a5f;border-radius:12px;" cellpadding="0" cellspacing="0">
                                <tr><td style="padding:24px 28px;">
                                  <p style="color:#f59e0b;font-size:11px;letter-spacing:2px;margin:0 0 12px;font-weight:bold;">NÚMERO DE SOCIO</p>
                                  <p style="color:#ffffff;font-size:26px;font-weight:bold;margin:0 0 16px;letter-spacing:3px;">%s</p>
                                  <p style="color:#93c5fd;font-size:13px;margin:0;"><strong style="color:#fff;">Nombre:</strong> %s %s</p>
                                  <p style="color:#93c5fd;font-size:13px;margin:4px 0;"><strong style="color:#fff;">DNI:</strong> %s</p>
                                  <p style="color:#93c5fd;font-size:13px;margin:4px 0;"><strong style="color:#fff;">Alta:</strong> %s &nbsp;|&nbsp; <strong style="color:#fff;">Temporada:</strong> 2025/2026</p>
                                </td></tr>
                              </table>
                              <p style="color:#555;line-height:1.7;font-size:14px;">
                                Adjuntamos tu <strong>carnet de socio en PDF</strong> para que lo tengas siempre a mano.
                              </p>
                              <div style="margin-top:24px;padding:18px;border-radius:12px;background:#eff6ff;">
                                <p style="color:#1e3a5f;font-size:14px;font-weight:bold;margin:0 0 8px;">Tus datos de acceso a la aplicación:</p>
                                <p style="color:#475569;font-size:14px;margin:0;">Correo electrónico: <strong>%s</strong></p>
                                <p style="color:#475569;font-size:14px;margin:6px 0 0;">Contraseña inicial: <strong>%s</strong></p>
                                <p style="color:#475569;font-size:13px;margin:10px 0 0;">Puedes cambiar la contraseña más adelante desde tu perfil.</p>
                              </div>
                            </td></tr>
                            <tr><td bgcolor="#f8fafc" style="padding:20px 40px;text-align:center;border-top:1px solid #e2e8f0;">
                              <p style="color:#94a3b8;font-size:12px;margin:0;">Albalate del Arzobispo, Teruel &bull; albalatefs@gmail.com</p>
                            </td></tr>
                          </table>
                        </td></tr>
                      </table>
                    </body>
                    </html>
                    """.formatted(socio.getNombre(), numeroSocio,
                            socio.getNombre(), socio.getApellidos(),
                            socio.getDni(), fechaAlta,
                            socio.getEmail(), socio.getDni());

            sendEmail(socio.getEmail(),
                    "¡Bienvenido al Albalate FS! Tu carnet de socio",
                    html,
                    pdfBytes,
                    "carnet-socio-" + numeroSocio + ".pdf");
        } catch (Exception e) {
            System.err.println("[EMAIL ERROR] Error preparando email de socio " + socio.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private byte[] generarCarnetPdf(Socio socio) throws Exception {
        String numeroSocio = String.format("ALB-%04d", socio.getId());
        String fechaAlta = socio.getFechaAlta() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(socio.getFechaAlta())
                : "-";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Rectangle cardSize = new Rectangle(243, 153);
        Document doc = new Document(cardSize, 0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        doc.open();

        PdfContentByte cb = writer.getDirectContent();

        BaseFont bfBold   = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        BaseFont bfNormal = BaseFont.createFont(BaseFont.HELVETICA,      BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

        cb.setColorFill(new Color(30, 58, 95));
        cb.rectangle(0, 0, 243, 153);
        cb.fill();

        cb.setColorFill(new Color(245, 158, 11));
        cb.rectangle(0, 143, 243, 10);
        cb.fill();

        cb.setColorFill(new Color(245, 158, 11));
        cb.rectangle(0, 0, 243, 6);
        cb.fill();

        cb.setColorFill(new Color(29, 78, 216));
        cb.rectangle(215, 6, 28, 137);
        cb.fill();

        drawText(cb, bfBold,   11f,  new Color(245, 158, 11),  "ALBALATE FS",                                    10, 131);
        drawText(cb, bfNormal,  6f,  new Color(147, 197, 253), "CLUB DE FÚTBOL SALA",                            10, 124);
        drawText(cb, bfBold,    5f,  new Color(245, 158, 11),  "CARNET DE SOCIO · 2025/2026",                    10, 115);
        drawText(cb, bfBold,    5.5f,new Color(147, 197, 253), "NÚMERO DE SOCIO",                                10, 104);
        drawText(cb, bfBold,   13f,  new Color(245, 158, 11),  numeroSocio,                                      10,  93);
        drawText(cb, bfBold,    5f,  new Color(147, 197, 253), "NOMBRE",                                         10,  80);
        drawText(cb, bfNormal,  7f,  Color.WHITE,              socio.getNombre() + " " + socio.getApellidos(),   10,  71);
        drawText(cb, bfBold,    5f,  new Color(147, 197, 253), "DNI / NIE",                                      10,  59);
        drawText(cb, bfNormal,  7f,  Color.WHITE,              socio.getDni(),                                   10,  50);
        drawText(cb, bfBold,    5f,  new Color(147, 197, 253), "ALTA",                                          120,  59);
        drawText(cb, bfNormal,  7f,  Color.WHITE,              fechaAlta,                                       120,  50);
        drawText(cb, bfNormal,  5f,  new Color(147, 197, 253), socio.getEmail(),                                 10,  15);

        doc.close();
        return baos.toByteArray();
    }

    private void drawText(PdfContentByte cb, BaseFont bf, float size, Color color,
                          String text, float x, float y) {
        cb.beginText();
        cb.setFontAndSize(bf, size);
        cb.setColorFill(color);
        cb.setTextMatrix(x, y);
        cb.showText(text);
        cb.endText();
    }

    // ── CONFIRMACIÓN DE PEDIDO EN TIENDA ──────────────────────────────────────

    public void enviarConfirmacionPedido(String email, String nombre,
                                         java.util.List<PedidoRequest.ItemCarrito> items,
                                         double totalEur) {
        try {
            String numeroPedido = "PED-" + (System.currentTimeMillis() % 1_000_000L);
            String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());

            StringBuilder itemsHtml = new StringBuilder();
            for (PedidoRequest.ItemCarrito item : items) {
                itemsHtml.append(String.format(
                    "<tr><td style='padding:8px 14px;color:#374151;border-bottom:1px solid #e5e7eb;'>%s</td>" +
                    "<td style='padding:8px 14px;text-align:center;color:#374151;border-bottom:1px solid #e5e7eb;'>x%d</td>" +
                    "<td style='padding:8px 14px;text-align:right;color:#1e3a5f;font-weight:bold;border-bottom:1px solid #e5e7eb;'>%.2f€</td></tr>",
                    item.getNombre(), item.getCantidad(),
                    item.getPrecioUnitario() * item.getCantidad()
                ));
            }

            String html = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head><meta charset="UTF-8"></head>
                    <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                      <table width="100%%" bgcolor="#f4f4f4" cellpadding="0" cellspacing="0">
                        <tr><td align="center" style="padding:40px 20px;">
                          <table width="600" bgcolor="#ffffff" cellpadding="0" cellspacing="0"
                                 style="border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1);">
                            <tr><td bgcolor="#1e3a5f" style="padding:32px 40px;text-align:center;">
                              <h1 style="color:#f59e0b;margin:0;font-size:28px;letter-spacing:2px;">ALBALATE FS</h1>
                              <p style="color:#93c5fd;margin:8px 0 0;font-size:14px;letter-spacing:1px;">TIENDA OFICIAL</p>
                            </td></tr>
                            <tr><td style="padding:40px;">
                              <h2 style="color:#1e3a5f;font-size:22px;margin:0 0 8px;">¡Pedido confirmado, %s! ✅</h2>
                              <p style="color:#6b7280;font-size:14px;margin:0 0 4px;">N.º de pedido: <strong>%s</strong></p>
                              <p style="color:#6b7280;font-size:14px;margin:0 0 28px;">Fecha: %s</p>
                              <table width="100%%" cellpadding="0" cellspacing="0"
                                     style="border-collapse:collapse;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;">
                                <tr style="background:#1e3a5f;">
                                  <th style="padding:10px 14px;color:#f59e0b;text-align:left;font-size:12px;letter-spacing:1px;">PRODUCTO</th>
                                  <th style="padding:10px 14px;color:#f59e0b;text-align:center;font-size:12px;letter-spacing:1px;">UND.</th>
                                  <th style="padding:10px 14px;color:#f59e0b;text-align:right;font-size:12px;letter-spacing:1px;">SUBTOTAL</th>
                                </tr>
                                %s
                                <tr style="background:#f8fafc;">
                                  <td colspan="2" style="padding:12px 14px;font-weight:bold;color:#1e3a5f;font-size:15px;">TOTAL</td>
                                  <td style="padding:12px 14px;text-align:right;font-weight:bold;color:#1e3a5f;font-size:18px;">%.2f€</td>
                                </tr>
                              </table>
                              <p style="color:#555;font-size:14px;line-height:1.7;margin:24px 0 0;">
                                Gracias por tu compra en la tienda oficial del <strong>Albalate FS</strong>.<br>
                                Nos pondremos en contacto contigo para gestionar la entrega.
                              </p>
                            </td></tr>
                            <tr><td bgcolor="#f8fafc" style="padding:20px 40px;text-align:center;border-top:1px solid #e2e8f0;">
                              <p style="color:#94a3b8;font-size:12px;margin:0;">Albalate del Arzobispo, Teruel &bull; albalatefs@gmail.com</p>
                            </td></tr>
                          </table>
                        </td></tr>
                      </table>
                    </body>
                    </html>
                    """.formatted(nombre, numeroPedido, fecha, itemsHtml.toString(), totalEur);

            sendEmail(email, "¡Pedido confirmado! - Albalate FS Tienda", html, null, null);
        } catch (Exception e) {
            System.err.println("[EMAIL ERROR] Error preparando email de pedido a " + email + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}