package com.acme.coldtrace.platform.reports.application.internal.commandservices;

import com.acme.coldtrace.platform.aiassistance.application.commandservices.AiAssistanceCommandService;
import com.acme.coldtrace.platform.aiassistance.application.model.ComplianceFindingDraft;
import com.acme.coldtrace.platform.aiassistance.application.model.ComplianceSummaryDraft;
import com.acme.coldtrace.platform.aiassistance.domain.model.commands.GenerateComplianceSummaryCommand;
import com.acme.coldtrace.platform.alerts.interfaces.acl.AlertsContextFacade;
import com.acme.coldtrace.platform.alerts.interfaces.acl.AlertsContextFacade.IncidentSnapshot;
import com.acme.coldtrace.platform.assetmanagement.interfaces.acl.AssetManagementContextFacade;
import com.acme.coldtrace.platform.billing.interfaces.acl.SubscriptionBillingContextFacade;
import com.acme.coldtrace.platform.assetmanagement.interfaces.acl.AssetManagementContextFacade.AssetSnapshot;
import com.acme.coldtrace.platform.iam.interfaces.acl.IamContextFacade;
import com.acme.coldtrace.platform.maintenancemanagement.interfaces.acl.MaintenanceManagementContextFacade;
import com.acme.coldtrace.platform.maintenancemanagement.interfaces.acl.MaintenanceManagementContextFacade.TechnicalServiceRequestSnapshot;
import com.acme.coldtrace.platform.monitoring.interfaces.acl.MonitoringContextFacade;
import com.acme.coldtrace.platform.monitoring.interfaces.acl.MonitoringContextFacade.SensorReadingSnapshot;
import com.acme.coldtrace.platform.reports.application.commandservices.ReportAiSummaryCommandFailure;
import com.acme.coldtrace.platform.reports.application.commandservices.ReportAiSummaryCommandService;
import com.acme.coldtrace.platform.reports.application.model.ReportAiSummary;
import com.acme.coldtrace.platform.reports.application.ports.ReportEvidenceSnapshot;
import com.acme.coldtrace.platform.reports.domain.model.commands.GenerateReportAiSummaryCommand;
import com.acme.coldtrace.platform.reports.application.ports.ReportEvidenceReader;
import com.acme.coldtrace.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static com.acme.coldtrace.platform.billing.interfaces.acl.SubscriptionBillingContextFacade.ENTITLEMENT_AI_REPORT_SUMMARY;

/**
 * Application service that generates advisory report summaries from backend-owned evidence.
 *
 * @since 1.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ReportAiSummaryCommandServiceImpl implements ReportAiSummaryCommandService {
    private static final int MAX_READING_EVIDENCE = 12;
    private static final int MAX_INCIDENT_EVIDENCE = 10;
    private static final int MAX_ASSET_EVIDENCE = 10;
    private static final int MAX_CORRECTIVE_ACTION_EVIDENCE = 8;

    private final ReportEvidenceReader reportRepository;
    private final IamContextFacade iamContextFacade;
    private final MonitoringContextFacade monitoringContextFacade;
    private final AlertsContextFacade alertsContextFacade;
    private final AssetManagementContextFacade assetManagementContextFacade;
    private final MaintenanceManagementContextFacade maintenanceManagementContextFacade;
    private final AiAssistanceCommandService aiAssistanceCommandService;
    private final ObjectMapper jsonMapper;
    private final SubscriptionBillingContextFacade subscriptionBillingContextFacade;

    public ReportAiSummaryCommandServiceImpl(
            ReportEvidenceReader reportRepository,
            IamContextFacade iamContextFacade,
            MonitoringContextFacade monitoringContextFacade,
            AlertsContextFacade alertsContextFacade,
            AssetManagementContextFacade assetManagementContextFacade,
            MaintenanceManagementContextFacade maintenanceManagementContextFacade,
            AiAssistanceCommandService aiAssistanceCommandService,
            ObjectMapper jsonMapper,
            SubscriptionBillingContextFacade subscriptionBillingContextFacade
    ) {
        this.reportRepository = reportRepository;
        this.iamContextFacade = iamContextFacade;
        this.monitoringContextFacade = monitoringContextFacade;
        this.alertsContextFacade = alertsContextFacade;
        this.assetManagementContextFacade = assetManagementContextFacade;
        this.maintenanceManagementContextFacade = maintenanceManagementContextFacade;
        this.aiAssistanceCommandService = aiAssistanceCommandService;
        this.jsonMapper = jsonMapper;
        this.subscriptionBillingContextFacade = subscriptionBillingContextFacade;
    }

    /**
     * Generates an advisory summary without mutating the original report metrics.
     *
     * @param command command containing organization and report identifiers
     * @return success with generated summary or failure with command/provider error
     */
    @Override
    public Result<ReportAiSummary, ReportAiSummaryCommandFailure> handle(GenerateReportAiSummaryCommand command) {
        if (!iamContextFacade.organizationExists(command.organizationId())) {
            log.warn("Organization not found for AI report summary: organizationId={}", command.organizationId());
            return Result.failure(new ReportAiSummaryCommandFailure.OrganizationNotFound());
        }

        var report = reportRepository.findByIdAndOrganizationId(command.reportId(), command.organizationId());
        if (report.isEmpty()) {
            log.warn("ReportEvidenceSnapshot not found for AI summary: organizationId={}, reportId={}",
                    command.organizationId(), command.reportId());
            return Result.failure(new ReportAiSummaryCommandFailure.ReportNotFound());
        }
        var entitlement = subscriptionBillingContextFacade.checkEntitlement(
                command.organizationId(),
                ENTITLEMENT_AI_REPORT_SUMMARY
        );
        if (entitlement.isPresent() && !Boolean.TRUE.equals(entitlement.get().enabled())) {
            log.warn("AI report summary blocked by plan limit: organizationId={}, entitlement={}",
                    command.organizationId(), ENTITLEMENT_AI_REPORT_SUMMARY);
            return Result.failure(new ReportAiSummaryCommandFailure.PlanLimitExceeded(entitlement.get()));
        }

        var context = buildReportSummaryContext(command.organizationId(), report.get());
        var serializedContext = serializeContext(context);
        if (serializedContext.isFailure()) {
            return Result.failure(serializedContext.failure().orElseThrow());
        }

        var generatedSummary = aiAssistanceCommandService.handle(
                new GenerateComplianceSummaryCommand(serializedContext.success().orElseThrow())
        );
        if (generatedSummary.isFailure()) {
            return Result.failure(new ReportAiSummaryCommandFailure.ProviderFailure(
                    generatedSummary.failure().orElseThrow()
            ));
        }

        var response = generatedSummary.success().orElseThrow();
        var summary = sanitizeComplianceSummary(response.content(), context);
        log.info("AI report summary generated: organizationId={}, reportId={}, modelProvider={}, modelName={}",
                command.organizationId(), command.reportId(), response.modelProvider(), response.modelName());
        return Result.success(new ReportAiSummary(
                report.get(),
                summary,
                response.modelProvider(),
                response.modelName(),
                OffsetDateTime.now()
        ));
    }

    private Result<String, ReportAiSummaryCommandFailure> serializeContext(ReportSummaryContext context) {
        try {
            return Result.success(jsonMapper.writeValueAsString(context));
        } catch (JsonProcessingException exception) {
            log.warn("ReportEvidenceSnapshot context serialization failed for AI summary", exception);
            return Result.failure(new ReportAiSummaryCommandFailure.ContextAssemblyFailed());
        }
    }

    private ReportSummaryContext buildReportSummaryContext(Long organizationId, ReportEvidenceSnapshot report) {
        var readings = monitoringContextFacade.fetchSensorReadingsByOrganizationId(organizationId).stream()
                .filter(reading -> isInReportPeriod(reading.recordedAt(), report))
                .toList();
        var incidents = alertsContextFacade.fetchIncidentsByOrganizationId(organizationId).stream()
                .filter(incident -> incident.detectedAt() != null)
                .filter(incident -> isInReportPeriod(incident.detectedAt(), report))
                .toList();
        var affectedAssets = fetchAffectedAssets(organizationId, readings, incidents);
        var correctiveActions = fetchCorrectiveActions(organizationId, affectedAssets, incidents);

        return new ReportSummaryContext(
                "report-ai-summary.v1",
                toReportContext(report),
                toReportMetricsContext(report),
                readings.stream()
                        .filter(reading -> Boolean.TRUE.equals(reading.outOfRange()))
                        .sorted(this::compareReadingByRecordedAtDesc)
                        .limit(MAX_READING_EVIDENCE)
                        .map(this::toReadingEvidenceContext)
                        .toList(),
                incidents.stream()
                        .sorted(this::compareIncidentByDetectedAtDesc)
                        .limit(MAX_INCIDENT_EVIDENCE)
                        .map(this::toIncidentEvidenceContext)
                        .toList(),
                affectedAssets.stream()
                        .limit(MAX_ASSET_EVIDENCE)
                        .map(this::toAssetEvidenceContext)
                        .toList(),
                correctiveActions.stream()
                        .limit(MAX_CORRECTIVE_ACTION_EVIDENCE)
                        .map(this::toCorrectiveActionEvidenceContext)
                        .toList(),
                buildEvidenceNotes(report, readings, incidents, correctiveActions)
        );
    }

    private boolean isInReportPeriod(OffsetDateTime recordedAt, ReportEvidenceSnapshot report) {
        if (recordedAt == null) {
            return false;
        }
        return !recordedAt.isBefore(report.periodStart()) && !recordedAt.isAfter(report.periodEnd());
    }

    private boolean isInReportPeriod(Instant detectedAt, ReportEvidenceSnapshot report) {
        if (detectedAt == null) {
            return false;
        }
        var start = report.periodStart().toInstant();
        var end = report.periodEnd().toInstant();
        return !detectedAt.isBefore(start) && !detectedAt.isAfter(end);
    }

    private List<AssetSnapshot> fetchAffectedAssets(
            Long organizationId,
            List<SensorReadingSnapshot> readings,
            List<IncidentSnapshot> incidents
    ) {
        var assetIds = new LinkedHashMap<Long, Long>();
        readings.stream()
                .map(SensorReadingSnapshot::assetId)
                .filter(Objects::nonNull)
                .forEach(assetId -> assetIds.putIfAbsent(assetId, assetId));
        incidents.stream()
                .map(IncidentSnapshot::assetId)
                .filter(Objects::nonNull)
                .forEach(assetId -> assetIds.putIfAbsent(assetId, assetId));

        return assetIds.keySet().stream()
                .map(assetId -> assetManagementContextFacade.fetchAssetByIdAndOrganizationId(
                        organizationId,
                        assetId
                ))
                .flatMap(java.util.Optional::stream)
                .sorted(Comparator.comparing(AssetSnapshot::id))
                .toList();
    }

    private List<TechnicalServiceRequestSnapshot> fetchCorrectiveActions(
            Long organizationId,
            List<AssetSnapshot> affectedAssets,
            List<IncidentSnapshot> incidents
    ) {
        var correctiveActions = new LinkedHashMap<Long, TechnicalServiceRequestSnapshot>();
        incidents.stream()
                .map(IncidentSnapshot::id)
                .filter(Objects::nonNull)
                .forEach(incidentId -> maintenanceManagementContextFacade
                        .fetchTechnicalServiceRequestsByOrganizationIdAndIncidentId(organizationId, incidentId)
                        .forEach(request -> correctiveActions.putIfAbsent(request.id(), request)));
        affectedAssets.stream()
                .map(AssetSnapshot::id)
                .filter(Objects::nonNull)
                .forEach(assetId -> maintenanceManagementContextFacade
                        .fetchTechnicalServiceRequestsByOrganizationIdAndAssetId(organizationId, assetId)
                        .forEach(request -> correctiveActions.putIfAbsent(request.id(), request)));

        return correctiveActions.values().stream()
                .sorted(this::compareTechnicalServiceRequestByRequestedAtDesc)
                .toList();
    }

    private List<String> buildEvidenceNotes(
            ReportEvidenceSnapshot report,
            List<SensorReadingSnapshot> readings,
            List<IncidentSnapshot> incidents,
            List<TechnicalServiceRequestSnapshot> correctiveActions
    ) {
        var notes = new ArrayList<String>();
        if (report.readingCount() == null || report.readingCount() == 0 || readings.isEmpty()) {
            notes.add("No sensor readings were available in the report period.");
        }
        if (report.outOfRangeReadingCount() != null && report.outOfRangeReadingCount() > 0 &&
                readings.stream().noneMatch(reading -> Boolean.TRUE.equals(reading.outOfRange()))) {
            notes.add("The report stores out-of-range metrics, but detailed out-of-range readings are unavailable.");
        }
        if (report.incidentCount() != null && report.incidentCount() > 0 && incidents.isEmpty()) {
            notes.add("The report stores incident metrics, but detailed incident records are unavailable.");
        }
        if (!incidents.isEmpty() && correctiveActions.isEmpty()) {
            notes.add("No corrective action evidence was found for incidents or affected assets in this report.");
        }
        if (report.compliancePercentage() == null) {
            notes.add("Compliance percentage is unavailable because the report has no eligible readings.");
        }
        return notes;
    }

    private int compareReadingByRecordedAtDesc(SensorReadingSnapshot left, SensorReadingSnapshot right) {
        if (left.recordedAt() == null && right.recordedAt() == null) {
            return 0;
        }
        if (left.recordedAt() == null) {
            return 1;
        }
        if (right.recordedAt() == null) {
            return -1;
        }
        return right.recordedAt().compareTo(left.recordedAt());
    }

    private int compareIncidentByDetectedAtDesc(IncidentSnapshot left, IncidentSnapshot right) {
        if (left.detectedAt() == null && right.detectedAt() == null) {
            return 0;
        }
        if (left.detectedAt() == null) {
            return 1;
        }
        if (right.detectedAt() == null) {
            return -1;
        }
        return right.detectedAt().compareTo(left.detectedAt());
    }

    private int compareTechnicalServiceRequestByRequestedAtDesc(
            TechnicalServiceRequestSnapshot left,
            TechnicalServiceRequestSnapshot right
    ) {
        if (left.requestedAt() == null && right.requestedAt() == null) {
            return 0;
        }
        if (left.requestedAt() == null) {
            return 1;
        }
        if (right.requestedAt() == null) {
            return -1;
        }
        return right.requestedAt().compareTo(left.requestedAt());
    }

    private ReportContext toReportContext(ReportEvidenceSnapshot report) {
        return new ReportContext(
                report.id(),
                report.organizationId(),
                limitText(report.uuid(), 80),
                limitText(report.type(), 80),
                limitText(report.title(), 160),
                report.periodStart(),
                report.periodEnd(),
                report.generatedAt()
        );
    }

    private ReportMetricsContext toReportMetricsContext(ReportEvidenceSnapshot report) {
        return new ReportMetricsContext(
                report.assetCount(),
                report.readingCount(),
                report.outOfRangeReadingCount(),
                report.incidentCount(),
                report.openIncidentCount(),
                report.averageTemperature(),
                report.averageHumidity(),
                report.compliancePercentage()
        );
    }

    private ReadingEvidenceContext toReadingEvidenceContext(SensorReadingSnapshot reading) {
        return new ReadingEvidenceContext(
                reading.id(),
                reading.assetId(),
                reading.iotDeviceId(),
                reading.gatewayId(),
                reading.locationId(),
                reading.temperature(),
                reading.humidity(),
                reading.outOfRange(),
                reading.recordedAt()
        );
    }

    private IncidentEvidenceContext toIncidentEvidenceContext(IncidentSnapshot incident) {
        return new IncidentEvidenceContext(
                incident.id(),
                incident.assetId(),
                limitText(incident.status(), 80),
                incident.detectedAt(),
                incident.isOpen()
        );
    }

    private AssetEvidenceContext toAssetEvidenceContext(AssetSnapshot asset) {
        return new AssetEvidenceContext(asset.id(), asset.locationId(), limitText(asset.name(), 120));
    }

    private CorrectiveActionEvidenceContext toCorrectiveActionEvidenceContext(
            TechnicalServiceRequestSnapshot request
    ) {
        return new CorrectiveActionEvidenceContext(
                request.id(),
                limitText(request.code(), 80),
                request.assetId(),
                request.incidentId(),
                limitText(request.assetName(), 120),
                limitText(request.issueDescription(), 240),
                limitText(request.priority(), 80),
                limitText(request.status(), 80),
                request.requestedAt(),
                request.closedAt(),
                limitText(request.closureSummary(), 240),
                limitText(request.evidence(), 240),
                limitText(request.closedBy(), 120)
        );
    }

    private String limitText(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private ComplianceSummaryDraft sanitizeComplianceSummary(
            ComplianceSummaryDraft summary,
            ReportSummaryContext context) {
        return new ComplianceSummaryDraft(
                sanitizeText(summary.executiveSummary(), 600, fallbackExecutiveSummary(context)),
                sanitizeFindings(summary.findings(), context),
                sanitizeTextList(summary.evidenceGaps(), fallbackEvidenceGaps(context), 2, 2, 240),
                sanitizeTextList(summary.recommendedActions(), fallbackRecommendedActions(context), 2, 2, 240),
                sanitizeTextList(summary.uncertaintyNotes(), fallbackUncertaintyNotes(context), 1, 1, 240)
        );
    }

    private List<ComplianceFindingDraft> sanitizeFindings(
            List<ComplianceFindingDraft> findings,
            ReportSummaryContext context) {
        var sanitized = new ArrayList<ComplianceFindingDraft>();
        if (findings != null) {
            findings.stream()
                    .filter(Objects::nonNull)
                    .map(this::sanitizeFinding)
                    .filter(Objects::nonNull)
                    .limit(3)
                    .forEach(sanitized::add);
        }
        fallbackFindings(context).stream()
                .limit(Math.max(0, 3 - sanitized.size()))
                .forEach(sanitized::add);
        return sanitized;
    }

    private ComplianceFindingDraft sanitizeFinding(ComplianceFindingDraft finding) {
        var area = sanitizeText(finding.area(), 100, null);
        var status = sanitizeText(finding.status(), 80, null);
        var evidence = sanitizeText(finding.evidence(), 360, null);
        var recommendation = sanitizeText(finding.recommendation(), 320, null);
        if (area == null || status == null || evidence == null || recommendation == null) {
            return null;
        }
        return new ComplianceFindingDraft(area, status, evidence, recommendation);
    }

    private List<String> sanitizeTextList(
            List<String> values,
            List<String> fallbacks,
            int minimumSize,
            int maximumSize,
            int maximumTextLength) {
        var sanitized = new ArrayList<String>();
        if (values != null) {
            values.stream()
                    .map(value -> sanitizeText(value, maximumTextLength, null))
                    .filter(Objects::nonNull)
                    .limit(maximumSize)
                    .forEach(sanitized::add);
        }
        fallbacks.stream()
                .map(value -> sanitizeText(value, maximumTextLength, null))
                .filter(Objects::nonNull)
                .limit(Math.max(0, minimumSize - sanitized.size()))
                .forEach(sanitized::add);
        return sanitized.stream().limit(maximumSize).toList();
    }

    private String sanitizeText(String value, int maxLength, String fallback) {
        var normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isBlank()) {
            normalized = fallback;
        }
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        normalized = normalized.replace("&#x0A;", " ")
                .replace("\\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return limitText(normalized, maxLength);
    }

    private String fallbackExecutiveSummary(ReportSummaryContext context) {
        return "The report requires operational review using persisted metrics and available evidence.";
    }

    private List<ComplianceFindingDraft> fallbackFindings(ReportSummaryContext context) {
        return List.of(
                new ComplianceFindingDraft(
                        "Thermal compliance",
                        fallbackStatus(context.metrics().compliancePercentage()),
                        "Compliance is based on persisted report metrics.",
                        "Review out-of-range readings and confirm corrective evidence."),
                new ComplianceFindingDraft(
                        "Incident pressure",
                        context.metrics().openIncidentCount() != null && context.metrics().openIncidentCount() > 0
                                ? "attention required"
                                : "stable",
                        "The report records %s open incident(s).".formatted(context.metrics().openIncidentCount()),
                        "Prioritize open incident review before final compliance closure."),
                new ComplianceFindingDraft(
                        "Evidence coverage",
                        context.correctiveActions().isEmpty() ? "limited evidence" : "evidence available",
                        "Corrective action evidence was assembled from backend records.",
                        "Attach final corrective notes where evidence is incomplete.")
        );
    }

    private String fallbackStatus(Double compliancePercentage) {
        if (compliancePercentage == null) {
            return "limited evidence";
        }
        return compliancePercentage >= 95.0 ? "stable" : "attention required";
    }

    private List<String> fallbackEvidenceGaps(ReportSummaryContext context) {
        return List.of(
                "AI summary is limited to persisted report metrics and related backend evidence.",
                "Manual quality validation is still required before using the summary as final compliance evidence."
        );
    }

    private List<String> fallbackRecommendedActions(ReportSummaryContext context) {
        return List.of(
                "Review out-of-range readings and open incidents referenced by the report.",
                "Attach corrective evidence before sharing the report with quality stakeholders."
        );
    }

    private List<String> fallbackUncertaintyNotes(ReportSummaryContext context) {
        return List.of("Generated summary is advisory and depends on the completeness of persisted ColdTrace records.");
    }

    private record ReportSummaryContext(
            String contextVersion,
            ReportContext report,
            ReportMetricsContext metrics,
            List<ReadingEvidenceContext> outOfRangeReadings,
            List<IncidentEvidenceContext> incidents,
            List<AssetEvidenceContext> affectedAssets,
            List<CorrectiveActionEvidenceContext> correctiveActions,
            List<String> evidenceNotes
    ) {
    }

    private record ReportContext(
            Long id,
            Long organizationId,
            String uuid,
            String type,
            String title,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd,
            OffsetDateTime generatedAt
    ) {
    }

    private record ReportMetricsContext(
            Integer assetCount,
            Integer readingCount,
            Integer outOfRangeReadingCount,
            Integer incidentCount,
            Integer openIncidentCount,
            Double averageTemperature,
            Double averageHumidity,
            Double compliancePercentage
    ) {
    }

    private record ReadingEvidenceContext(
            Long id,
            Long assetId,
            Long iotDeviceId,
            Long gatewayId,
            Long locationId,
            Double temperature,
            Double humidity,
            Boolean outOfRange,
            OffsetDateTime recordedAt
    ) {
    }

    private record IncidentEvidenceContext(
            Long id,
            Long assetId,
            String status,
            Instant detectedAt,
            Boolean open
    ) {
    }

    private record AssetEvidenceContext(Long id, Long locationId, String name) {
    }

    private record CorrectiveActionEvidenceContext(
            Long id,
            String code,
            Long assetId,
            Long incidentId,
            String assetName,
            String issueDescription,
            String priority,
            String status,
            OffsetDateTime requestedAt,
            OffsetDateTime closedAt,
            String closureSummary,
            String evidence,
            String closedBy
    ) {
    }
}
