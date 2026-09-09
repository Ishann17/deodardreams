INSERT INTO physical_units
    (id, created_at, created_by, updated_at, updated_by,
     floor, is_active, name, parent_unit_id, unit_type)
VALUES
    (1, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     'First Floor', 1, 'Single Suite 1', NULL, 'INDIVIDUAL_ROOM'),

    (2, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     'First Floor', 1, 'Single Suite 2', NULL, 'INDIVIDUAL_ROOM'),

    (3, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     'First Floor', 1, 'Single Suite 3', NULL, 'INDIVIDUAL_ROOM'),

    (4, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     'First Floor', 1, 'Single Suite 4', NULL, 'INDIVIDUAL_ROOM'),

    (5, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     'First Floor', 1, 'Common Hall', NULL, 'COMMON_HALL_FLOOR_ONE'),

    (6, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     'Second Floor', 1, '2 BHK Unit', NULL, 'TWO_BHK_UNIT'),

    (7, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     'Second Floor', 1, '2 BHK Bedroom 1', 6, 'ONE_BHK_BEDROOM'),

    (8, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     'Second Floor', 1, '2 BHK Bedroom 2', 6, 'ONE_BHK_BEDROOM'),

    (9, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     'Second Floor', 1, '2nd 2 BHK Unit', NULL, 'TWO_BHK_UNIT'),

    (10, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     'Second Floor', 1, '2nd 2 BHK Bedroom 1', 9, 'ONE_BHK_BEDROOM'),

    (11, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     'Second Floor', 1, '2nd 2 BHK Bedroom 2', 9, 'ONE_BHK_BEDROOM');