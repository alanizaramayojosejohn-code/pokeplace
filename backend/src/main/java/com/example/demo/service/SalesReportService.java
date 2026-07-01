package com.example.demo.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.example.demo.dto.*;
import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 300_000; // 5 min

    private static class CacheEntry {
        final Object data;
        final long timestamp;
        CacheEntry(Object data) { this.data = data; this.timestamp = System.currentTimeMillis(); }
        boolean isExpired() { return System.currentTimeMillis() - timestamp > CACHE_TTL_MS; }
    }

    @PostConstruct
    public void init() {
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60_000);
                    cache.entrySet().removeIf(e -> e.getValue().isExpired());
                } catch (InterruptedException ignored) { break; }
            }
        }, "cache-cleaner");
        cleaner.setDaemon(true);
        cleaner.start();
    }

    @SuppressWarnings("unchecked")
    private <T> T getCached(String key, java.util.function.Supplier<T> loader) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) return (T) entry.data;
        T data = loader.get();
        cache.put(key, new CacheEntry(data));
        return data;
    }

    private static final String[] MONTH_NAMES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    public SalesReportSummaryDTO getSummary(LocalDate start, LocalDate end) {
        String key = "summary|" + start + "|" + end;
        return getCached(key, () -> buildSummary(start, end));
    }

    private SalesReportSummaryDTO buildSummary(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);

        List<DailySalesReportDTO> dailySales = getDailySales(startDateTime, endDateTime);
        List<ProductReportDTO> bestSelling = getBestSellingProductsByDateRange(startDateTime, endDateTime);
        List<PaymentMethodReportDTO> byPayment = getSalesByPaymentMethod(startDateTime, endDateTime);

        Double periodTotal = dailySales.stream()
            .mapToDouble(DailySalesReportDTO::getTotalSales)
            .sum();

        Long totalOrders = dailySales.stream()
            .mapToLong(DailySalesReportDTO::getOrderCount)
            .sum();

        return new SalesReportSummaryDTO(periodTotal, totalOrders, dailySales, bestSelling, byPayment);
    }

    public List<MonthlyReportDTO> getMonthlyReport(int year) {
        String key = "monthly|" + year;
        return getCached(key, () -> fetchMonthlyReport(year));
    }

    private List<MonthlyReportDTO> fetchMonthlyReport(int year) {
        return orderRepository.findMonthlyReport(year)
            .stream()
            .map(row -> new MonthlyReportDTO(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).doubleValue(),
                ((Number) row[3]).doubleValue()
            ))
            .collect(Collectors.toList());
    }

    public byte[] exportExcel(int year) {
        List<MonthlyReportDTO> data = getMonthlyReport(year);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Cierre Gestion " + year);

            CellStyle headerStyle = workbook.createCellStyle();
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            Row header = sheet.createRow(0);
            String[] columns = {"Mes", "Pedidos", "Ventas", "Ganancia"};
            for (int i = 0; i < columns.length; i++) {
                var cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            double totalSales = 0;
            double totalProfit = 0;
            long totalOrders = 0;

            int rowNum = 1;
            for (MonthlyReportDTO dto : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(getMonthName(dto.getMonth()));
                row.createCell(1).setCellValue(dto.getOrderCount());

                var salesCell = row.createCell(2);
                salesCell.setCellValue(dto.getTotalSales());
                salesCell.setCellStyle(numberStyle);

                var profitCell = row.createCell(3);
                profitCell.setCellValue(dto.getTotalProfit());
                profitCell.setCellStyle(numberStyle);

                totalSales += dto.getTotalSales();
                totalProfit += dto.getTotalProfit();
                totalOrders += dto.getOrderCount();
            }

            Row totalRow = sheet.createRow(rowNum);
            totalRow.createCell(0).setCellValue("TOTAL");
            totalRow.createCell(1).setCellValue(totalOrders);
            var totalSalesCell = totalRow.createCell(2);
            totalSalesCell.setCellValue(totalSales);
            totalSalesCell.setCellStyle(numberStyle);
            var totalProfitCell = totalRow.createCell(3);
            totalProfitCell.setCellValue(totalProfit);
            totalProfitCell.setCellStyle(numberStyle);

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }

    public byte[] exportPdf(int year) {
        List<MonthlyReportDTO> data = getMonthlyReport(year);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Cierre de Gesti\u00f3n " + year, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 3, 3});

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            String[] headers = {"Mes", "Pedidos", "Ventas ($)", "Ganancia ($)"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            double totalSales = 0;
            double totalProfit = 0;
            long totalOrders = 0;

            for (MonthlyReportDTO dto : data) {
                table.addCell(new Phrase(getMonthName(dto.getMonth()), cellFont));
                table.addCell(new Phrase(String.valueOf(dto.getOrderCount()), cellFont));
                table.addCell(new Phrase(String.format("$ %.2f", dto.getTotalSales()), cellFont));
                table.addCell(new Phrase(String.format("$ %.2f", dto.getTotalProfit()), cellFont));
                totalSales += dto.getTotalSales();
                totalProfit += dto.getTotalProfit();
                totalOrders += dto.getOrderCount();
            }

            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            table.addCell(new Phrase("TOTAL", totalFont));
            table.addCell(new Phrase(String.valueOf(totalOrders), totalFont));
            table.addCell(new Phrase(String.format("$ %.2f", totalSales), totalFont));
            table.addCell(new Phrase(String.format("$ %.2f", totalProfit), totalFont));

            document.add(table);

            Paragraph footer = new Paragraph(
                "\nGenerado: " + LocalDate.now(),
                FontFactory.getFont(FontFactory.HELVETICA, 9)
            );
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    public byte[] exportSummaryExcel(LocalDate start, LocalDate end) {
        SalesReportSummaryDTO summary = getSummary(start, end);
        List<UserSalesReportDTO> users = getSalesByUser(start, end);
        List<ClientSalesReportDTO> clients = getSalesByClient(start, end);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Reporte " + start + " - " + end);

            CellStyle headerStyle = wb.createCellStyle();
            var hFont = wb.createFont();
            hFont.setBold(true);
            hFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(hFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle boldStyle = wb.createCellStyle();
            var bFont = wb.createFont();
            bFont.setBold(true);
            boldStyle.setFont(bFont);

            CellStyle numStyle = wb.createCellStyle();
            numStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));

            int r = 0;

            // Title
            Row titleRow = sheet.createRow(r++);
            titleRow.createCell(0).setCellValue("Reporte de Ventas (" + start + " - " + end + ")");
            titleRow.getCell(0).setCellStyle(boldStyle);
            r++;

            // Summary
            Row stat1 = sheet.createRow(r++);
            stat1.createCell(0).setCellValue("Total Ventas");
            var tv = stat1.createCell(1);
            tv.setCellValue(summary.getPeriodTotal());
            tv.setCellStyle(numStyle);

            Row stat2 = sheet.createRow(r++);
            stat2.createCell(0).setCellValue("Total Pedidos");
            stat2.createCell(1).setCellValue(summary.getTotalOrders());

            Row stat3 = sheet.createRow(r++);
            stat3.createCell(0).setCellValue("Promedio por Pedido");
            double avg = summary.getTotalOrders() > 0 ? summary.getPeriodTotal() / summary.getTotalOrders() : 0;
            var av = stat3.createCell(1);
            av.setCellValue(avg);
            av.setCellStyle(numStyle);
            r++;

            // Products
            Row prodHeader = sheet.createRow(r++);
            prodHeader.createCell(0).setCellValue("Producto");
            prodHeader.createCell(1).setCellValue("Cantidad");
            prodHeader.createCell(2).setCellValue("Ingreso");
            for (int i = 0; i < 3; i++) prodHeader.getCell(i).setCellStyle(headerStyle);

            for (ProductReportDTO p : summary.getBestSellingProducts()) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(p.getProductName());
                row.createCell(1).setCellValue(p.getQuantitySold());
                var c = row.createCell(2);
                c.setCellValue(p.getRevenue());
                c.setCellStyle(numStyle);
            }
            r++;

            // Payment methods
            Row payHeader = sheet.createRow(r++);
            payHeader.createCell(0).setCellValue("Metodo de Pago");
            payHeader.createCell(1).setCellValue("Pedidos");
            payHeader.createCell(2).setCellValue("Total");
            for (int i = 0; i < 3; i++) payHeader.getCell(i).setCellStyle(headerStyle);

            for (PaymentMethodReportDTO pm : summary.getSalesByPaymentMethod()) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(pm.getPaymentMethod());
                row.createCell(1).setCellValue(pm.getOrderCount());
                var c = row.createCell(2);
                c.setCellValue(pm.getTotal());
                c.setCellStyle(numStyle);
            }
            r++;

            // Users
            Row userHeader = sheet.createRow(r++);
            userHeader.createCell(0).setCellValue("Usuario");
            userHeader.createCell(1).setCellValue("Pedidos");
            userHeader.createCell(2).setCellValue("Total");
            for (int i = 0; i < 3; i++) userHeader.getCell(i).setCellStyle(headerStyle);

            for (UserSalesReportDTO u : users) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(u.getUserName());
                row.createCell(1).setCellValue(u.getOrderCount());
                var c = row.createCell(2);
                c.setCellValue(u.getTotalSales());
                c.setCellStyle(numStyle);
            }
            r++;

            // Clients
            Row clientHeader = sheet.createRow(r++);
            clientHeader.createCell(0).setCellValue("Cliente");
            clientHeader.createCell(1).setCellValue("Pedidos");
            clientHeader.createCell(2).setCellValue("Total");
            for (int i = 0; i < 3; i++) clientHeader.getCell(i).setCellStyle(headerStyle);

            for (ClientSalesReportDTO c : clients) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(c.getClientName());
                row.createCell(1).setCellValue(c.getOrderCount());
                var cv = row.createCell(2);
                cv.setCellValue(c.getTotalSales());
                cv.setCellStyle(numStyle);
            }

            for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating summary Excel", e);
        }
    }

    public byte[] exportSummaryPdf(LocalDate start, LocalDate end) {
        SalesReportSummaryDTO summary = getSummary(start, end);
        List<UserSalesReportDTO> users = getSalesByUser(start, end);
        List<ClientSalesReportDTO> clients = getSalesByClient(start, end);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Reporte de Ventas", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            doc.add(title);

            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Paragraph range = new Paragraph(start + " - " + end, subFont);
            range.setAlignment(Element.ALIGN_CENTER);
            range.setSpacingAfter(20);
            doc.add(range);

            // Summary stats
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            double avg = summary.getTotalOrders() > 0 ? summary.getPeriodTotal() / summary.getTotalOrders() : 0;
            doc.add(new Paragraph("Total Ventas: Bs " + String.format("%.2f", summary.getPeriodTotal()), boldFont));
            doc.add(new Paragraph("Total Pedidos: " + summary.getTotalOrders(), boldFont));
            doc.add(new Paragraph("Promedio por Pedido: Bs " + String.format("%.2f", avg), boldFont));
            doc.add(new Paragraph(" "));

            // Products
            doc.add(new Paragraph("Productos Vendidos", boldFont));
            PdfPTable prodTable = new PdfPTable(3);
            prodTable.setWidthPercentage(100);
            prodTable.setWidths(new float[]{4, 2, 3});
            Font hf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            for (String h : new String[]{"Producto", "Cantidad", "Ingreso"}) {
                prodTable.addCell(new Phrase(h, hf));
            }
            Font cf = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (ProductReportDTO p : summary.getBestSellingProducts()) {
                prodTable.addCell(new Phrase(p.getProductName(), cf));
                prodTable.addCell(new Phrase(String.valueOf(p.getQuantitySold()), cf));
                prodTable.addCell(new Phrase("Bs " + String.format("%.2f", p.getRevenue()), cf));
            }
            doc.add(prodTable);
            doc.add(new Paragraph(" "));

            // Payment methods
            doc.add(new Paragraph("Metodos de Pago", boldFont));
            PdfPTable payTable = new PdfPTable(3);
            payTable.setWidthPercentage(100);
            for (String h : new String[]{"Metodo", "Pedidos", "Total"}) {
                payTable.addCell(new Phrase(h, hf));
            }
            for (PaymentMethodReportDTO pm : summary.getSalesByPaymentMethod()) {
                payTable.addCell(new Phrase(pm.getPaymentMethod(), cf));
                payTable.addCell(new Phrase(String.valueOf(pm.getOrderCount()), cf));
                payTable.addCell(new Phrase("Bs " + String.format("%.2f", pm.getTotal()), cf));
            }
            doc.add(payTable);
            doc.add(new Paragraph(" "));

            // Users
            doc.add(new Paragraph("Ventas por Usuario", boldFont));
            PdfPTable userTable = new PdfPTable(3);
            userTable.setWidthPercentage(100);
            for (String h : new String[]{"Usuario", "Pedidos", "Total"}) {
                userTable.addCell(new Phrase(h, hf));
            }
            for (UserSalesReportDTO u : users) {
                userTable.addCell(new Phrase(u.getUserName(), cf));
                userTable.addCell(new Phrase(String.valueOf(u.getOrderCount()), cf));
                userTable.addCell(new Phrase("Bs " + String.format("%.2f", u.getTotalSales()), cf));
            }
            doc.add(userTable);
            doc.add(new Paragraph(" "));

            // Clients
            doc.add(new Paragraph("Ventas por Cliente", boldFont));
            PdfPTable clientTable = new PdfPTable(3);
            clientTable.setWidthPercentage(100);
            for (String h : new String[]{"Cliente", "Pedidos", "Total"}) {
                clientTable.addCell(new Phrase(h, hf));
            }
            for (ClientSalesReportDTO c : clients) {
                clientTable.addCell(new Phrase(c.getClientName(), cf));
                clientTable.addCell(new Phrase(String.valueOf(c.getOrderCount()), cf));
                clientTable.addCell(new Phrase("Bs " + String.format("%.2f", c.getTotalSales()), cf));
            }
            doc.add(clientTable);

            Paragraph footer = new Paragraph("\nGenerado: " + LocalDate.now(),
                FontFactory.getFont(FontFactory.HELVETICA, 9));
            footer.setAlignment(Element.ALIGN_RIGHT);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating summary PDF", e);
        }
    }

    private String getMonthName(int month) {
        if (month < 1 || month > 12) return "Mes " + month;
        return MONTH_NAMES[month - 1];
    }

    public List<UserSalesReportDTO> getSalesByUser(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        return orderRepository.findSalesByUser(startDateTime, endDateTime)
            .stream()
            .map(row -> new UserSalesReportDTO(
                (Long) row[0],
                (String) row[1] + " " + (String) row[2],
                (Long) row[3],
                (Double) row[4]
            ))
            .collect(Collectors.toList());
    }

    public List<ClientSalesReportDTO> getSalesByClient(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        return orderRepository.findSalesByClient(startDateTime, endDateTime)
            .stream()
            .map(row -> new ClientSalesReportDTO(
                (Long) row[0],
                (String) row[1],
                (Long) row[2],
                (Double) row[3]
            ))
            .collect(Collectors.toList());
    }

    public DashboardReportDTO getDashboard() {
        return getCached("dashboard", () -> buildDashboard());
    }

    private DashboardReportDTO buildDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate weekStart = today.minusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atTime(23, 59, 59);
        LocalDateTime weekStartDT = weekStart.atStartOfDay();
        LocalDateTime monthStartDT = monthStart.atStartOfDay();
        LocalDateTime now = today.atTime(23, 59, 59);

        double todaySales = 0, yesterdaySales = 0, weekSales = 0, monthSales = 0;
        long todayOrders = 0, weekOrders = 0, monthOrders = 0;

        for (Object[] row : orderRepository.findSalesTotalAndCountBetween(todayStart, todayEnd)) {
            todaySales = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
            todayOrders = row[1] != null ? ((Number) row[1]).longValue() : 0;
        }
        for (Object[] row : orderRepository.findSalesTotalAndCountBetween(yesterdayStart, yesterdayEnd)) {
            yesterdaySales = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
        }
        for (Object[] row : orderRepository.findSalesTotalAndCountBetween(weekStartDT, now)) {
            weekSales = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
            weekOrders = row[1] != null ? ((Number) row[1]).longValue() : 0;
        }
        for (Object[] row : orderRepository.findSalesTotalAndCountBetween(monthStartDT, now)) {
            monthSales = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
            monthOrders = row[1] != null ? ((Number) row[1]).longValue() : 0;
        }

        double salesGrowthPercent = yesterdaySales > 0
            ? ((todaySales - yesterdaySales) / yesterdaySales) * 100
            : (todaySales > 0 ? 100 : 0);

        double monthProfit = 0;
        int currentMonth = today.getMonthValue();
        List<Object[]> monthProfitData = orderRepository.findMonthlyReportForMonth(today.getYear(), currentMonth);
        if (!monthProfitData.isEmpty() && monthProfitData.get(0)[3] != null) {
            monthProfit = ((Number) monthProfitData.get(0)[3]).doubleValue();
        }

        List<ProductReportDTO> topProducts = getBestSellingProductsByDateRange(todayStart, now)
            .stream().limit(5).collect(Collectors.toList());

        List<DailySalesReportDTO> weeklyTrend = getDailySales(weekStartDT, now);

        List<PaymentMethodReportDTO> paymentMethods = getSalesByPaymentMethod(todayStart, now);

        long pendingOrders = orderRepository.countByStatus(com.example.demo.model.Order.OrderStatus.PENDING);

        return DashboardReportDTO.builder()
            .todaySales(todaySales)
            .yesterdaySales(yesterdaySales)
            .salesGrowthPercent(Math.round(salesGrowthPercent * 10.0) / 10.0)
            .weekSales(weekSales)
            .monthSales(monthSales)
            .monthProfit(monthProfit)
            .todayOrders(todayOrders)
            .weekOrders(weekOrders)
            .monthOrders(monthOrders)
            .pendingOrders(pendingOrders)
            .topProducts(topProducts)
            .weeklyTrend(weeklyTrend)
            .salesByPaymentMethod(paymentMethods)
            .build();
    }

    public List<HourlyReportDTO> getHourlySales(LocalDate start, LocalDate end) {
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(23, 59, 59);
        return orderRepository.findHourlySales(startDT, endDT)
            .stream()
            .map(row -> new HourlyReportDTO(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).doubleValue()
            ))
            .collect(Collectors.toList());
    }

    public List<CategoryReportDTO> getSalesByCategory(LocalDate start, LocalDate end) {
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(23, 59, 59);
        return orderRepository.findSalesByCategory(startDT, endDT)
            .stream()
            .map(row -> new CategoryReportDTO(
                (String) row[0],
                ((Number) row[1]).longValue(),
                ((Number) row[2]).doubleValue()
            ))
            .collect(Collectors.toList());
    }

    public YearComparisonDTO getYearComparison(int year1, int year2) {
        List<MonthlyReportDTO> m1 = getMonthlyReport(year1);
        List<MonthlyReportDTO> m2 = getMonthlyReport(year2);

        double y1Sales = m1.stream().mapToDouble(MonthlyReportDTO::getTotalSales).sum();
        double y2Sales = m2.stream().mapToDouble(MonthlyReportDTO::getTotalSales).sum();
        double y1Profit = m1.stream().mapToDouble(MonthlyReportDTO::getTotalProfit).sum();
        double y2Profit = m2.stream().mapToDouble(MonthlyReportDTO::getTotalProfit).sum();

        double salesGrowth = y1Sales > 0 ? ((y2Sales - y1Sales) / y1Sales) * 100 : 0;
        double profitGrowth = y1Profit > 0 ? ((y2Profit - y1Profit) / y1Profit) * 100 : 0;

        return new YearComparisonDTO(year1, year2, y1Sales, y2Sales, y1Profit, y2Profit,
            Math.round(salesGrowth * 10.0) / 10.0,
            Math.round(profitGrowth * 10.0) / 10.0,
            m1, m2);
    }

    public InventoryValueDTO getInventoryValue() {
        List<Object[]> data = productRepository.findInventoryValue();
        if (data.isEmpty()) {
            return InventoryValueDTO.builder().totalCostValue(0).totalSaleValue(0)
                .potentialProfit(0).totalProducts(0).lowStockProducts(0).build();
        }
        Object[] row = data.get(0);
        double costValue = ((Number) row[0]).doubleValue();
        double saleValue = ((Number) row[1]).doubleValue();
        long totalProducts = ((Number) row[2]).longValue();
        long lowStock = ((Number) row[3]).longValue();

        return InventoryValueDTO.builder()
            .totalCostValue(costValue)
            .totalSaleValue(saleValue)
            .potentialProfit(saleValue - costValue)
            .totalProducts(totalProducts)
            .lowStockProducts(lowStock)
            .build();
    }

    private List<ProductReportDTO> getBestSellingProductsByDateRange(LocalDateTime start, LocalDateTime end) {
    return orderRepository.findBestSellingProductsByDateRange(start, end)
        .stream()
        .map(row -> new ProductReportDTO(
            (String) row[0],
            ((Number) row[1]).longValue(),
            ((Number) row[2]).doubleValue()
        ))
        .collect(Collectors.toList());
}
    private List<DailySalesReportDTO> getDailySales(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findDailySales(start, end)
            .stream()
            .map(row -> new DailySalesReportDTO(
                row[0] instanceof java.sql.Date
                    ? ((java.sql.Date) row[0]).toLocalDate()
                    : (java.time.LocalDate) row[0],
                ((Number) row[1]).longValue(),
                ((Number) row[2]).doubleValue()
            ))
            .collect(Collectors.toList());
    }

    private List<PaymentMethodReportDTO> getSalesByPaymentMethod(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findSalesByPaymentMethod(start, end)
            .stream()
            .map(row -> new PaymentMethodReportDTO(
                (String) row[0],
                ((Number) row[1]).longValue(),
                ((Number) row[2]).doubleValue()
            ))
            .collect(Collectors.toList());
    }

    public List<OrderResponse> getDeliveredOrders(LocalDate start, LocalDate end) {
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(23, 59, 59);
        return orderRepository.findDeliveredOrdersWithDetails(startDT, endDT)
            .stream()
            .map(OrderResponse::from)
            .collect(Collectors.toList());
    }
}
