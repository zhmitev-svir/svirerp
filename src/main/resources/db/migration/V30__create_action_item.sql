-- V30__create_action_item.sql
-- Action items captured live during a meeting, optionally assigned to a trustee.

CREATE TABLE action_item (
    id                   CHAR(36)            PRIMARY KEY DEFAULT (UUID()),
    meeting_minutes_id   CHAR(36)            NOT NULL,
    assignee_trustee_id  CHAR(36),
    note                 TEXT                NOT NULL,
    priority             VARCHAR(20)         NOT NULL DEFAULT 'normal'
                             CHECK (priority IN ('high', 'normal', 'low')),
    due_date             DATE,
    status               VARCHAR(20)         NOT NULL DEFAULT 'new'
                             CHECK (status IN ('new', 'planned', 'done')),
    notes                TEXT,
    created_at           DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_action_item_meeting FOREIGN KEY (meeting_minutes_id)  REFERENCES meeting_minutes (id) ON DELETE CASCADE,
    CONSTRAINT fk_action_item_trustee FOREIGN KEY (assignee_trustee_id) REFERENCES trustee (id)         ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_action_item_meeting  ON action_item (meeting_minutes_id);
CREATE INDEX idx_action_item_trustee  ON action_item (assignee_trustee_id);
CREATE INDEX idx_action_item_status   ON action_item (status);
