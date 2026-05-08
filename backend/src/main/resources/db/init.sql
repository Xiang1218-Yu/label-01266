-- 公租房管理系统 - 达梦数据库建表脚本

-- 1. 用户表
CREATE TABLE sys_user (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR2(50) NOT NULL,
    password VARCHAR2(100) NOT NULL,
    real_name VARCHAR2(50),
    phone VARCHAR2(20),
    email VARCHAR2(100),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_username UNIQUE (username)
);

-- 2. 操作日志表
CREATE TABLE sys_operation_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR2(50),
    operation_type VARCHAR2(50),
    module VARCHAR2(100),
    description CLOB,
    ip_address VARCHAR2(50),
    request_url VARCHAR2(500),
    request_method VARCHAR2(20),
    request_params CLOB,
    response_result CLOB,
    status INT DEFAULT 1,
    error_msg CLOB,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 小区表
CREATE TABLE community (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    address VARCHAR2(500),
    description VARCHAR2(1000),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. 楼栋表
CREATE TABLE building (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    community_id BIGINT NOT NULL,
    name VARCHAR2(100) NOT NULL,
    floors INT DEFAULT 1,
    units_per_floor INT DEFAULT 1,
    description VARCHAR2(1000),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_building_community FOREIGN KEY (community_id) REFERENCES community(id)
);

-- 5. 房屋/户室表
CREATE TABLE room (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    building_id BIGINT NOT NULL,
    room_number VARCHAR2(50) NOT NULL,
    floor INT NOT NULL,
    area DECIMAL(10,2),
    rent DECIMAL(10,2),
    status INT DEFAULT 0,
    remark VARCHAR2(1000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_building FOREIGN KEY (building_id) REFERENCES building(id)
);

-- 6. 承租人表
CREATE TABLE tenant (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    room_id BIGINT,
    name VARCHAR2(50) NOT NULL,
    id_card VARCHAR2(20) NOT NULL,
    marital_status INT,
    family_size INT DEFAULT 1,
    phone VARCHAR2(20) NOT NULL,
    income_status INT,
    community_belong VARCHAR2(100),
    check_in_time TIMESTAMP NOT NULL,
    is_disabled INT DEFAULT 0,
    relocation_type INT,
    sale_exchange INT,
    remark VARCHAR2(1000),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tenant_room FOREIGN KEY (room_id) REFERENCES room(id)
);

-- 7. 家庭成员表
CREATE TABLE family_member (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR2(50) NOT NULL,
    id_card VARCHAR2(20) NOT NULL,
    relationship VARCHAR2(50),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id)
);

-- 8. 房屋操作记录表
CREATE TABLE room_operation_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    room_id BIGINT NOT NULL,
    tenant_id BIGINT,
    operator_id BIGINT,
    operator_name VARCHAR2(50),
    operation_type VARCHAR2(50),
    description CLOB,
    before_data CLOB,
    after_data CLOB,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_log_room FOREIGN KEY (room_id) REFERENCES room(id)
);

-- 创建索引
CREATE INDEX idx_building_community ON building(community_id);
CREATE INDEX idx_room_building ON room(building_id);
CREATE INDEX idx_room_status ON room(status);
CREATE INDEX idx_room_floor ON room(floor);
CREATE INDEX idx_tenant_room ON tenant(room_id);
CREATE INDEX idx_tenant_id_card ON tenant(id_card);
CREATE INDEX idx_member_tenant ON family_member(tenant_id);
CREATE INDEX idx_room_log_room ON room_operation_log(room_id);
CREATE INDEX idx_sys_log_time ON sys_operation_log(operation_time);

-- 初始化管理员账号 (密码会在应用启动时自动设置)
INSERT INTO sys_user (username, password, real_name, status) VALUES ('admin', 'temp', '系统管理员', 1);

-- 初始化测试数据 - 小区
INSERT INTO community (name, address, description) VALUES ('阳光花园', '市中心路100号', '市级公租房小区');
INSERT INTO community (name, address, description) VALUES ('和谐家园', '新城区路200号', '区级公租房小区');

-- 初始化测试数据 - 楼栋
INSERT INTO building (community_id, name, floors, units_per_floor) VALUES (1, '1栋', 18, 4);
INSERT INTO building (community_id, name, floors, units_per_floor) VALUES (1, '2栋', 18, 4);
INSERT INTO building (community_id, name, floors, units_per_floor) VALUES (2, 'A栋', 12, 6);

-- 初始化测试数据 - 房源 (阳光花园1栋 1-3层)
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '101', 1, 65.5, 800, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '102', 1, 72.0, 850, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '103', 1, 65.5, 800, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '104', 1, 72.0, 850, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '201', 2, 65.5, 820, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '202', 2, 72.0, 870, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '203', 2, 65.5, 820, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '204', 2, 72.0, 870, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '301', 3, 65.5, 840, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '302', 3, 72.0, 890, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '303', 3, 65.5, 840, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (1, '304', 3, 72.0, 890, 1);

-- 初始化测试数据 - 房源 (阳光花园2栋 1-2层)
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (2, '101', 1, 58.0, 750, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (2, '102', 1, 58.0, 750, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (2, '103', 1, 68.0, 820, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (2, '104', 1, 68.0, 820, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (2, '201', 2, 58.0, 770, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (2, '202', 2, 58.0, 770, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (2, '203', 2, 68.0, 840, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (2, '204', 2, 68.0, 840, 1);

-- 初始化测试数据 - 房源 (和谐家园A栋 1-2层)
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A101', 1, 55.0, 700, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A102', 1, 55.0, 700, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A103', 1, 60.0, 750, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A104', 1, 60.0, 750, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A105', 1, 65.0, 800, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A106', 1, 65.0, 800, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A201', 2, 55.0, 720, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A202', 2, 55.0, 720, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A203', 2, 60.0, 770, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A204', 2, 60.0, 770, 1);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A205', 2, 65.0, 820, 0);
INSERT INTO room (building_id, room_number, floor, area, rent, status) VALUES (3, 'A206', 2, 65.0, 820, 1);

-- 初始化测试数据 - 承租人 (已出租房源的承租人)
INSERT INTO tenant (room_id, name, id_card, marital_status, family_size, phone, income_status, community_belong, check_in_time, is_disabled, relocation_type, sale_exchange, status) 
VALUES (1, '张三', '110101199001011234', 1, 3, '13800138001', 0, '阳光社区', '2024-01-15 10:00:00', 0, 0, 0, 1);
INSERT INTO tenant (room_id, name, id_card, marital_status, family_size, phone, income_status, community_belong, check_in_time, is_disabled, relocation_type, sale_exchange, status) 
VALUES (3, '李四', '110101198505052345', 0, 1, '13800138002', 1, '阳光社区', '2024-02-20 14:30:00', 0, 0, 0, 1);
INSERT INTO tenant (room_id, name, id_card, marital_status, family_size, phone, income_status, community_belong, check_in_time, is_disabled, relocation_type, sale_exchange, status) 
VALUES (5, '王五', '110101199203033456', 1, 4, '13800138003', 0, '和平社区', '2024-03-10 09:00:00', 1, 1, 0, 1);
INSERT INTO tenant (room_id, name, id_card, marital_status, family_size, phone, income_status, community_belong, check_in_time, is_disabled, relocation_type, sale_exchange, status) 
VALUES (7, '赵六', '110101198808084567', 2, 2, '13800138004', 1, '和平社区', '2024-04-05 11:00:00', 0, 0, 1, 1);
INSERT INTO tenant (room_id, name, id_card, marital_status, family_size, phone, income_status, community_belong, check_in_time, is_disabled, relocation_type, sale_exchange, status) 
VALUES (8, '钱七', '110101199506065678', 1, 3, '13800138005', 0, '幸福社区', '2024-05-18 15:00:00', 0, 2, 0, 1);
INSERT INTO tenant (room_id, name, id_card, marital_status, family_size, phone, income_status, community_belong, check_in_time, is_disabled, relocation_type, sale_exchange, status) 
VALUES (10, '孙八', '110101199108086789', 1, 2, '13800138006', 1, '幸福社区', '2024-06-22 10:30:00', 0, 0, 0, 1);
INSERT INTO tenant (room_id, name, id_card, marital_status, family_size, phone, income_status, community_belong, check_in_time, is_disabled, relocation_type, sale_exchange, status) 
VALUES (12, '周九', '110101198712127890', 0, 1, '13800138007', 2, '阳光社区', '2024-07-08 16:00:00', 0, 0, 2, 1);
