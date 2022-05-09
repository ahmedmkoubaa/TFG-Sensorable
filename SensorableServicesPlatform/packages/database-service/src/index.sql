use test;

DROP TABLE  IF EXISTS sensors, events_for_adls, events, adls;   

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

CREATE TABLE adls (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL
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
    tag ENUM(
        'HOME',
        'FRIENDS',
        'FAMILY',
        'SHOPPING',
        'HOSPITAL',
        'PHARMACY'
    ),
    CHECK (
        pos >= 0
        AND pos < 3
    )
);

CREATE TABLE events_for_adls (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_adl INT,
    id_event INT,
    FOREIGN KEY (id_adl) REFERENCES adls(id) ON DELETE CASCADE,
    FOREIGN KEY (id_event) REFERENCES events(id) ON DELETE CASCADE,
    UNIQUE KEY(id_adl, id_event)
);

INSERT INTO
    adls(title, description)
VALUES
    (
        "Llamada telefónica detectada",
        "En base a criterios del sistema juzgando la postura del usuario se ha detectado una posible llamada teléfonica."
    );

INSERT INTO
    adls(title, description)
VALUES
    (
        "Paseo por la calle, en exteriores",
        "Se ha detectado una caminata a ritmo ligero fuera de casa."
    );

INSERT INTO
    adls(title, description)
VALUES
    (
        "Paseo por casa, en interior",
        "Se ha detectado al usuario andando dentro de casa."
    );

INSERT INTO
    adls(title, description)
VALUES
    (
        "Actividad física avanzada",
        "Se ha detectado una actividad física detectada, el ritmo cardíaco subió durante la actividad y hubo bastante movimiento."
    );

INSERT INTO
    adls(title, description)
VALUES
    (
        "Visita a casa de amigos detectada",
        "El usuario ha estado cerca de la casa de unos amigos."
    );

INSERT INTO
    adls(title, description)
VALUES
    (
        "Desplazamiento por cuenta propia usando un medio de transporte",
        "Aparentemente se ha usado un medio de transporte para desplazarse."
    );

/*events for phone call*/
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
    (0, 1, 0, 'GREATER_EQUAL', 4);

INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 1, 0, 'LESS_EQUAL', -4);

/*----------------------------------------------------------------------*/
/*events to detect a walk*/
/*5*/
INSERT INTO
    events(
        id,
        device_type,
        sensor_type,
        pos,
        operator,
        operand
    )
VALUES
    (5, 0, 19, 0, 'GREATER_EQUAL', 1);

/*6*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 10, 4, 'GREATER', 1);

/*7*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 21, 0, 'GREATER', 50);

/*8*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 21, 0, 'LESS', 80);

/*far from home*/
/*9*/
INSERT INTO
    events(
        device_type,
        sensor_type,
        pos,
        operator,
        operand,
        tag
    )
VALUES
    (0, 2411, 3, 'GREATER', 100, "HOME");

/*10*/
/*close from home*/
INSERT INTO
    events(
        device_type,
        sensor_type,
        pos,
        operator,
        operand,
        tag
    )
VALUES
    (0, 2411, 3, 'LESS_EQUAL', 100, "HOME");

/*events for workout detection (the previous were also for this)*/
/*11*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 21, 0, 'GREATER_EQUAL', 90);

/*12*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 21, 0, 'LESS_EQUAL', 150);

/*events for places visiting detection*/
/*13*/
INSERT INTO
    events(
        device_type,
        sensor_type,
        pos,
        operator,
        operand,
        tag
    )
VALUES
    (0, 2411, 3, 'GREATER_EQUAL', 200, "HOME");

/*14*/
INSERT INTO
    events(
        device_type,
        sensor_type,
        pos,
        operator,
        operand,
        tag
    )
VALUES
    (0, 2411, 3, 'LESS_EQUAL', 200, "FRIENDS");

/*15*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 10, 4, 'LESS', -1);

/*events for displacement*/
/*16*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 10, 4, 'GREATER', 2);

/*17*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 10, 4, 'LESS', -2);

/*events to detect a phone call*/
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

/*events to detect a walk in the outside*/
INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (2, 5);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (2, 6);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (2, 7);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (2, 8);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (2, 9);

/*events to detect a walk inside house*/
INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (3, 5);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (3, 6);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (3, 7);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (3, 8);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (3, 10);

/*events to detect workout in the outside*/
INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (4, 5);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (4, 6);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (4, 9);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (4, 11);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (4, 12);

/*events for places visiting detection*/
INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (5, 13);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (5, 14);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (5, 6);

/*events for displacement detection*/
INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (6, 15);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (6, 16);