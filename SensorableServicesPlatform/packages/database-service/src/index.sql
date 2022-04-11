/*EXAMPLE OF INSERT:
 INSERT INTO sensors (device_type, sensor_type, values_x, values_y, values_z, timestamp) VALUES (0,21, 67, -1, -1, 1234567981)*/
CREATE TABLE sensors(
    Id INT AUTO_INCREMENT PRIMARY KEY,
    device_type INT NOT NULL,
    sensor_type INT NOT NULL,
    values_x FLOAT NOT NULL,
    values_y FLOAT,
    values_z FLOAT,
    timestamp BIGINT NOT NULL
);

/ /
INSERT INTO
    sensors (
        device_type,
        sensor_type,
        values_x,
        values_y,
        values_z,
        timestamp
    )
VALUES
    (0, 21, 67, -1, -1, 1234567981);

CREATE TABLE adls (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL
);

INSERT INTO
    adls(title, description)
VALUES
(
        "Llamada telefónica detectada",
        "En base a criterios del sistema juzgando la postura del usuario se ha detectado una posible llamada teléfonica."
    );

CREATE TABLE events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    device_type INT NOT NULL,
    sensor_type INT NOT NULL,
    pos INT DEFAULT 0,
    operator ENUM(
        'GREATER',
        'LESS',
        'EQUAL',
        'GREATER_EQUAL',
        'LESS_EQUAL',
        'NOT_EQUAL'
    ) NOT NULL,
    operand FLOAT NOT NULL,
    CHECK (
        pos >= 0
        AND pos < 3
    )
);

INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 8, 0, 'EQUAL', 0);

INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 5, 0, 'LESS_EQUAL', 120);

INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 1, 2, 'LESS_EQUAL', 4);

INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 1, 2, 'GREATER_EQUAL', -4);

CREATE TABLE events_for_adls (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_adl INT,
    id_event INT,
    FOREIGN KEY (id_adl) REFERENCES adls(id) ON DELETE CASCADE,
    FOREIGN KEY (id_event) REFERENCES events(id) ON DELETE CASCADE,
    UNIQUE KEY(id_adl, id_event)
);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (1, 1);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (1, 2);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (1, 3);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (1, 4);