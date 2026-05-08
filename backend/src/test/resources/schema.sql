CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation_type VARCHAR(50),
    module VARCHAR(100),
    description CLOB,
    ip_address VARCHAR(50),
    request_url VARCHAR(500),
    request_method VARCHAR(20),
    request_params CLOB,
    response_result CLOB,
    status INT DEFAULT 1,
    error_msg CLOB,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS community (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    description VARCHAR(1000),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS building (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    community_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    floors INT DEFAULT 1,
    units_per_floor INT DEFAULT 1,
    description VARCHAR(1000),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    building_id BIGINT NOT NULL,
    room_number VARCHAR(50) NOT NULL,
    floor INT NOT NULL,
    area DECIMAL(10,2),
    rent DECIMAL(10,2),
    status INT DEFAULT 0,
    remark VARCHAR(1000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT,
    name VARCHAR(50) NOT NULL,
    id_card VARCHAR(20) NOT NULL,
    marital_status INT,
    family_size INT DEFAULT 1,
    phone VARCHAR(20) NOT NULL,
    income_status INT,
    community_belong VARCHAR(100),
    check_in_time TIMESTAMP,
    is_disabled INT DEFAULT 0,
    relocation_type INT,
    sale_exchange INT,
    remark VARCHAR(1000),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS family_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    id_card VARCHAR(20) NOT NULL,
    relationship VARCHAR(50),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS room_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    tenant_id BIGINT,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    operation_type VARCHAR(50),
    description CLOB,
    before_data CLOB,
    after_data CLOB,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
