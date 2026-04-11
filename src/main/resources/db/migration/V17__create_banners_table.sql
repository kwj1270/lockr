CREATE TABLE banners (
    id            VARCHAR(26)   NOT NULL PRIMARY KEY,
    title         VARCHAR(100)  NOT NULL,
    subtitle      VARCHAR(200)  NOT NULL,
    placement     VARCHAR(20)   NOT NULL,
    image_url     VARCHAR(500)  NULL,
    action_url    VARCHAR(500)  NULL,
    action_route  VARCHAR(200)  NULL,
    bg_color      VARCHAR(7)    NULL,
    text_color    VARCHAR(7)    NULL,
    icon_type     VARCHAR(20)   NULL,
    display_order INT           NOT NULL DEFAULT 0,
    is_active     TINYINT(1)    NOT NULL DEFAULT 1,
    created_at    DATETIME(6)   NOT NULL,
    updated_at    DATETIME(6)   NOT NULL
);

CREATE INDEX idx_banners_placement_active_order ON banners (placement, is_active, display_order);
