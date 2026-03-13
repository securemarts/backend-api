-- Drop legacy attributes_json from product_variants; options are now in variant_option_values
ALTER TABLE product_variants DROP COLUMN IF EXISTS attributes_json;
