-- VirtualWorkspace 단일-row 갱신 모델 스키마
-- - PK: (player_id, stage_code)
-- - UNIQUE: public_id (Gateway lookup key)

CREATE TABLE virtual_workspace (
    player_id        BIGINT       NOT NULL,
    stage_code       VARCHAR(32)   NOT NULL,

    public_id        VARCHAR(63)   NOT NULL,
    lifecycle_status VARCHAR(20)   NOT NULL,

    owner_subject_id VARCHAR(255)  NOT NULL,
    access_mode      VARCHAR(20)   NOT NULL,

    base_host        VARCHAR(255)  NOT NULL,

    k8s_namespace    VARCHAR(63)   NULL,
    service_name     VARCHAR(63)   NULL,
    service_port     INT           NULL,

    image            VARCHAR(255)  NOT NULL,
    port             INT           NOT NULL,
    cpu              VARCHAR(32)   NOT NULL,
    memory           VARCHAR(32)   NOT NULL,
    readiness_path   VARCHAR(128)  NOT NULL,

    created_date     DATETIME(0)   NULL,
    modified_date    DATETIME(0)   NULL,

    PRIMARY KEY (player_id, stage_code),
    UNIQUE KEY uk_virtual_workspace_public_id (public_id)
);

