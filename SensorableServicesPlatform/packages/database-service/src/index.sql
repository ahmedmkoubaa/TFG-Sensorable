use test;

DROP TABLE IF EXISTS sensors,
events_for_adls,
events,
adls;

DECLARE DEVICE_MOBILE CONSTANT number (1) := 0;

DECLARE DEVICE_WEAR_OS CONSTANT number (1) := 1;

DECLARE DEVICE_EMPATICA CONSTANT number (1) := 2;

DECLARE TYPE_ACCELEROMETER CONSTANT number (1) := 1;

DECLARE TYPE_LIGHT CONSTANT number (1) := 5;

DECLARE TYPE_PRESSURE CONSTANT number (1) := 6;

DECLARE TYPE_LINEAR_ACCELERATION CONSTANT number (1) := 10;

DECLARE TYPE_RELATIVE_HUMIDITY CONSTANT number (1) := 12;

DECLARE TYPE_AMBIENT_TEMPERATURE CONSTANT number (1) := 13;

DECLARE TYPE_STEP_DETECTOR CONSTANT number (1) := 18;

DECLARE TYPE_STEP_COUNTER CONSTANT number (1) := 19;

DECLARE TYPE_HEART_RATE CONSTANT number (1) := 21;

DECLARE TYPE_HEART_RATE CONSTANT number (1) := 2411;

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
    version INT DEFAULT 1,
    FOREIGN KEY (id_adl) REFERENCES adls(id) ON DELETE CASCADE,
    FOREIGN KEY (id_event) REFERENCES events(id) ON DELETE CASCADE,
    UNIQUE KEY(id_adl, id_event, version)
);

/*----------------------------------------------------------------------*/
/*phone call adl detection*/
INSERT INTO
    adls(title, description)
VALUES
    (
        "Llamada telefónica detectada",
        "En base a criterios del sistema, juzgando la postura del usuario y demás, se ha detectado una posible llamada teléfonica."
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

/*events for adls to detect a phone call*/
/*version 1 (left hand)*/
INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (1, 1, 1);

INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (1, 2, 1);

INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (1, 3, 1);

/*version 2 (right hand)*/
INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (1, 1, 2);

INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (1, 2, 2);

INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (1, 4, 2);

/*----------------------------------------------------------------------*/
INSERT INTO
    adls(title, description)
VALUES
    (
        "Paseo por la calle, en exteriores",
        "Se ha detectado una caminata a ritmo ligero fuera de casa."
    );

/*events to detect a walk*/
/*5*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 10, 4, 'GREATER', 1.5);

/*6*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 19, 0, 'GREATER_EQUAL', 1);

/*7*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 21, 0, 'GREATER', 60);

/*8*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 21, 0, 'LESS', 80);

/*9*/
/*far from home*/
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
    (0, 2411, 3, 'GREATER', 50, "HOME");

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

INSERT INTO
    adls(title, description)
VALUES
    (
        "Paseo por casa, en interior",
        "Se ha detectado al usuario andando dentro de casa."
    );

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
    (0, 2411, 3, 'LESS_EQUAL', 50, "HOME");

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

INSERT INTO
    adls(title, description)
VALUES
    (
        "Actividad física avanzada",
        "Se ha detectado una actividad física detectada, el ritmo cardíaco subió durante la actividad y hubo bastante movimiento."
    );

/*events to detect a walk*/
/*11*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 10, 4, 'GREATER', 2);

/*12*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 21, 0, 'GREATER_EQUAL', 85);

/*events to detect workout in the outside*/
INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (4, 11);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (4, 6);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (4, 12);

INSERT INTO
    adls(title, description)
VALUES
    (
        "Visita a casa de amigos detectada",
        "El usuario ha estado cerca de la casa de unos amigos."
    );

/*events for places visiting detection*/
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
    (0, 2411, 3, 'GREATER_EQUAL', 100, "HOME");

/*15*/
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
    (0, 2411, 3, 'LESS', 100, "FRIENDS");

/*events for places visiting detection*/
/*version 1: go walking*/
INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (5, 14, 1);

INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (5, 5, 1);

/*events for places visiting detection*/
INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (5, 6, 1);

INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (5, 15, 1);

/*version 2: go in a vehicle*/
INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (5, 14, 2);

/*new events here using positivo and negative strong acceleration*/
INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (5, 15, 2);

INSERT INTO
    adls(title, description)
VALUES
    (
        "Desplazamiento por cuenta propia usando un medio de transporte",
        "Aparentemente se ha usado un medio de transporte para desplazarse."
    );

/*13*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 21, 0, 'LESS_EQUAL', 150);

/*16*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 10, 4, 'LESS', -1);

/*events for displacement*/
/*17*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 10, 4, 'GREATER', 2);

/*18*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (0, 10, 4, 'LESS', -2);

/*events for displacement detection*/
INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (6, 15);

INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (6, 16);

/*Example: how to add a new adl*/
/*INSERT INTO
 adls(title, description)
 VALUES
 (
 "Agitado teléfono",
 "Hemos visto que has agitado el teléfono ferozmente, se  ha detectado pues esta pseudoadl de prueba"
 );
 
 INSERT INTO
 events(device_type, sensor_type, pos, operator, operand)
 VALUES
 (0, 10, 4, 'GREATER', 5);
 
 INSERT INTO
 events(device_type, sensor_type, pos, operator, operand)
 VALUES
 (0, 10, 4, 'LESS', -5);
 
 INSERT INTO
 events_for_adls (id_adl, id_event, version)
 VALUES
 (7, 17, 1);
 
 INSERT INTO
 events_for_adls (id_adl, id_event)
 VALUES
 (7, 18, 1);
 */