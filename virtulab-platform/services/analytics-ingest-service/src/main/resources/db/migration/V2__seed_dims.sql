INSERT INTO analytics.dim_org (org_id, tenant_id, org_name)
VALUES ('org-dev', 'tenant-dev', 'Dev Organization')
ON CONFLICT (org_id) DO NOTHING;

INSERT INTO analytics.dim_user (user_id, tenant_id, org_id, display_name)
VALUES ('student-1', 'tenant-dev', 'org-dev', 'Student One')
ON CONFLICT (user_id) DO NOTHING;
