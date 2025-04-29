package com.zdoc.service;

import com.zdoc.dao.ReportDAO;
import com.zdoc.model.Payroll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestScoped
public class ReportService {

    @Inject
    private ReportDAO reportDAO;
    public String generatePayrollReportAsHtml() {
        try {
            // 1. Carrega o relatório compilado (.jasper)
            InputStream jasperStream = getClass().getResourceAsStream("/reports/relatorio_folha_pagamento.jasper");
            if (jasperStream == null) {
                throw new RuntimeException("Arquivo .jasper não encontrado em /reports/");
            }


            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);


            List<Payroll> payrollList = reportDAO.getPayrollReportData();
            JRDataSource dataSource = new JRBeanCollectionDataSource(payrollList);


            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Folha de Pagamento");


            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    parameters,
                    dataSource
            );


            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            HtmlExporter exporter = new HtmlExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
            exporter.exportReport();


            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Falha ao gerar relatório HTML: " + e.getMessage(), e);
        }
    }
}