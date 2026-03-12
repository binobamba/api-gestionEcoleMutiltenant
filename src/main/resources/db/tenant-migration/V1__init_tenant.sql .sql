-- ══════════════════════════════════════════════════════════
-- TENANT DATABASE — copiée sur chaque BDD d'école
-- ══════════════════════════════════════════════════════════

-- Table des élèves
CREATE TABLE IF NOT EXISTS students (
                                        id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    student_number VARCHAR(20)  NOT NULL UNIQUE,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(200) UNIQUE,
    date_of_birth  DATE,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    is_deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    version        BIGINT       NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),

    CONSTRAINT chk_student_status
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'GRADUATED', 'TRANSFERRED'))
    );

CREATE INDEX IF NOT EXISTS idx_students_number
    ON students (student_number);

CREATE INDEX IF NOT EXISTS idx_students_email
    ON students (email);

-- Table des professeurs
CREATE TABLE IF NOT EXISTS teachers (
                                        id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(200) NOT NULL UNIQUE,
    subject    VARCHAR(100),
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    version    BIGINT       NOT NULL DEFAULT 0,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT chk_teacher_status
    CHECK (status IN ('ACTIVE', 'INACTIVE'))
    );

CREATE INDEX IF NOT EXISTS idx_teachers_email
    ON teachers (email);

-- Table des cours
CREATE TABLE IF NOT EXISTS courses (
                                       id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(20)  NOT NULL UNIQUE,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    teacher_id  UUID REFERENCES teachers(id),
    credits     INTEGER      NOT NULL DEFAULT 1,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
    );

-- Table des inscriptions (student ↔ course)
CREATE TABLE IF NOT EXISTS enrollments (
                                           id          UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id  UUID      NOT NULL REFERENCES students(id),
    course_id   UUID      NOT NULL REFERENCES courses(id),
    enrolled_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_enrollment UNIQUE (student_id, course_id),
    CONSTRAINT chk_enrollment_status
    CHECK (status IN ('ACTIVE', 'DROPPED', 'COMPLETED'))
    );

-- Table des notes
CREATE TABLE IF NOT EXISTS grades (
                                      id            UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id UUID    NOT NULL REFERENCES enrollments(id),
    grade         DECIMAL(5,2),
    comment       TEXT,
    graded_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
    );