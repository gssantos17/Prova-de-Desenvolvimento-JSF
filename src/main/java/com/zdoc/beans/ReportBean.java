package com.zdoc.beans;

import com.zdoc.service.ReportService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@RequestScoped
public class ReportBean implements Serializable {

    private String reportHtml;

    @Inject
    private ReportService reportService;

    public void generatePayrollReport() {
        try {
            reportHtml = reportService.generatePayrollReportAsHtml();

            if (reportHtml == null || reportHtml.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Aviso", "Nenhum dado encontrado para o relatório."));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erro", "Falha ao gerar relatório: " + e.getMessage()));
        }
    }

    public String getReportHtml() {
        return reportHtml;
    }
}