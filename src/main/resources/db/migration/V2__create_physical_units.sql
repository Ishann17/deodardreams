CREATE TABLE physical_units (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6),
    created_by BIGINT,
    updated_at DATETIME(6),
    updated_by BIGINT,
    floor VARCHAR(255),
    is_active BIT(1) NOT NULL,
    name VARCHAR(255),
    parent_unit_id BIGINT,
    unit_type ENUM(
        'COMMON_HALL_FLOOR_ONE',
        'INDIVIDUAL_ROOM',
        'ONE_BHK_BEDROOM',
        'TWO_BHK_UNIT'
    ),
    PRIMARY KEY (id)
);