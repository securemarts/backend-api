-- Allow DEACTIVATED status for business members (owner can deactivate instead of remove)
ALTER TABLE business_members ALTER COLUMN status TYPE VARCHAR(30);
