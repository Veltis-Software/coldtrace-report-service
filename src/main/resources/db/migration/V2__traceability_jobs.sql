ALTER TABLE reading_aggregates ADD COLUMN temperature_count INT NOT NULL DEFAULT 0;
ALTER TABLE reading_aggregates ADD COLUMN humidity_count INT NOT NULL DEFAULT 0;
CREATE TABLE reading_samples (
  organization_id BIGINT NOT NULL,
  reading_id BIGINT NOT NULL,
  asset_id BIGINT NOT NULL,
  recorded_at DATETIME(6) NOT NULL,
  payload JSON NOT NULL,
  PRIMARY KEY (organization_id,reading_id),
  INDEX ix_samples_org_period (organization_id,recorded_at)
);
CREATE TABLE incident_projection (
  organization_id BIGINT NOT NULL,
  incident_id BIGINT NOT NULL,
  asset_id BIGINT NOT NULL,
  detected_at DATETIME(6) NOT NULL,
  payload JSON NOT NULL,
  PRIMARY KEY (organization_id,incident_id)
);
CREATE TABLE reports (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  organization_id BIGINT NOT NULL,
  requested_by BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  period_start DATETIME(6) NOT NULL,
  period_end DATETIME(6) NOT NULL,
  status VARCHAR(20) NOT NULL,
  incident_snapshot JSON NOT NULL,
  result JSON NULL,
  created_at DATETIME(6) NOT NULL,
  completed_at DATETIME(6) NULL,
  INDEX ix_reports_org (organization_id,id)
);
