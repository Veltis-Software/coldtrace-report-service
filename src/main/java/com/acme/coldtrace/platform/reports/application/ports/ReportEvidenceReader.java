package com.acme.coldtrace.platform.reports.application.ports;
import java.util.List;
import java.util.Optional;
public interface ReportEvidenceReader {
 List<ReportEvidenceSnapshot> findAllByOrganizationId(Long organizationId);
 Optional<ReportEvidenceSnapshot> findByIdAndOrganizationId(Long id, Long organizationId);
}
