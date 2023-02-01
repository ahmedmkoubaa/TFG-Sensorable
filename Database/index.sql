use ahmed_test;

DROP TABLE IF EXISTS custom_adls_for_users,
generic_adls,
events_for_adls,
sensors,
adls,
events,
users;

SET
    @DEVICE_MOBILE := 0;

SET
    @DEVICE_WEAR_OS_RIGHT := 1;

SET
    @DEVICE_EMPATICA := 2;

SET
    @DEVICE_WEAR_OS_LEFT := 3;

SET
    @TYPE_ACCELEROMETER := 1;

SET
    @TYPE_LIGHT := 5;

SET
    @TYPE_PRESSURE := 6;

SET
    @TYPE_PROXIMITY := 8;

SET
    @TYPE_LINEAR_ACCELERATION := 10;

SET
    @TYPE_RELATIVE_HUMIDITY := 12;

SET
    @TYPE_AMBIENT_TEMPERATURE := 13;

SET
    @TYPE_STEP_DETECTOR := 18;

SET
    @TYPE_STEP_COUNTER := 19;

SET
    @TYPE_HEART_RATE := 21;

SET
    @TYPE_GPS := 2411;

SET
    @FIRST := 0;

SET
    @SECOND := 1;

SET
    @THIRD := 2;

SET
    @DISTANCE := 3;

SET
    @ANY := 4;

SET
    @ALL := 5;

/*EXAMPLE OF INSERT:
 INSERT INTO sensors (device_type, sensor_type, values_x, values_y, values_z, timestamp) VALUES (0,21, 67, -1, -1, 1234567981, "EX-001")*/
CREATE TABLE sensors(
    Id INT AUTO_INCREMENT PRIMARY KEY,
    device_type INT NOT NULL,
    sensor_type INT NOT NULL,
    values_x FLOAT NOT NULL,
    values_y FLOAT,
    values_z FLOAT,
    /* use of bigint due to java constraints*/
    timestamp BIGINT NOT NULL,
    user_id VARCHAR(8) NOT NULL
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

CREATE TABLE users (id INT PRIMARY KEY AUTO_INCREMENT);

CREATE TABLE custom_adls_for_users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_user INT NOT NULL,
    id_adl INT NOT NULL,
    version INT DEFAULT 1,
    FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (id_adl) REFERENCES adls(id) ON DELETE CASCADE,
    UNIQUE KEY(id_user, id_adl, version)
);

CREATE TABLE generic_adls (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_adl INT NOT NULL,
    version INT DEFAULT 1,
    FOREIGN KEY (id_adl) REFERENCES adls(id)
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
    (
        @DEVICE_MOBILE,
        @TYPE_PROXIMITY,
        @FIRST,
        'EQUAL',
        0
    );

INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (
        @DEVICE_MOBILE,
        @TYPE_LIGHT,
        @FIRST,
        'LESS_EQUAL',
        120
    );

INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (
        @DEVICE_MOBILE,
        @TYPE_ACCELEROMETER,
        @FIRST,
        'GREATER_EQUAL',
        4
    );

INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (
        @DEVICE_MOBILE,
        @TYPE_ACCELEROMETER,
        @FIRST,
        'LESS_EQUAL',
        -4
    );

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
    (
        @DEVICE_MOBILE,
        @TYPE_LINEAR_ACCELERATION,
        @ANY,
        'GREATER',
        1.5
    );

/*6*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (
        @DEVICE_MOBILE,
        @TYPE_STEP_COUNTER,
        @FIRST,
        'GREATER_EQUAL',
        1
    );

/*7*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (
        @DEVICE_MOBILE,
        @TYPE_HEART_RATE,
        @FIRST,
        'GREATER',
        60
    );

/*8*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (@DEVICE_MOBILE, @TYPE_HEART_RATE, 0, 'LESS', 80);

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
    (
        @DEVICE_MOBILE,
        @TYPE_GPS,
        @DISTANCE,
        'GREATER',
        50,
        "HOME"
    );

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
    (
        @DEVICE_MOBILE,
        @TYPE_GPS,
        @DISTANCE,
        'LESS_EQUAL',
        50,
        "HOME"
    );

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
    (
        @DEVICE_MOBILE,
        @TYPE_LINEAR_ACCELERATION,
        @ANY,
        'GREATER',
        2
    );

/*12*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (
        @DEVICE_MOBILE,
        @TYPE_HEART_RATE,
        @FIRST,
        'GREATER_EQUAL',
        85
    );

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
    (
        @DEVICE_MOBILE,
        @TYPE_GPS,
        @DISTANCE,
        'GREATER_EQUAL',
        100,
        "HOME"
    );

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
    (
        @DEVICE_MOBILE,
        @TYPE_GPS,
        @DISTANCE,
        'LESS',
        100,
        "FRIENDS"
    );

/*events for places visiting detection*/
/*version 1: go walking*/
INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (5, 13, 1);

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
    (5, 14, 1);

/*version 2: go in a vehicle*/
/*15*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (
        @DEVICE_MOBILE,
        @TYPE_LINEAR_ACCELERATION,
        @ANY,
        'GREATER',
        3
    );

/*16*/
INSERT INTO
    events(device_type, sensor_type, pos, operator, operand)
VALUES
    (
        @DEVICE_MOBILE,
        @TYPE_LINEAR_ACCELERATION,
        @ANY,
        'LESS',
        -3
    );

INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (5, 13, 2);

INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (5, 15, 2);

INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (5, 16, 2);

/*new events here using positive and negative strong acceleration*/
INSERT INTO
    events_for_adls (id_adl, id_event, version)
VALUES
    (5, 14, 2);

INSERT INTO
    adls(title, description)
VALUES
    (
        "Desplazamiento por cuenta propia usando un medio de transporte",
        "Aparentemente se ha usado un medio de transporte para desplazarse."
    );

/*events for displacement detection in a vehicle*/
INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (6, 9);

/*events for displacement detection*/
INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (6, 5);

/*events for displacement detection*/
INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (6, 6);

/*events for displacement detection*/
INSERT INTO
    events_for_adls (id_adl, id_event)
VALUES
    (6, 15);

/*events for displacement detection*/
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
;

/*GET THE CUSTOM AND GENERICS ADLS*/
SELECT
    *
FROM
    adls
WHERE
    id IN (
        SELECT
            id_adl
        FROM
            custom_adls_for_users
        WHERE
            id_user = 1
        UNION
        SELECT
            id_adl
        FROM
            generic_adls
    );

/*GET THE EVENTS FOR ADLS RELATIONS BY VERSION*/
SELECT
    DISTINCT events_for_adls.*
FROM
    events_for_adls,
    custom_adls_for_users,
    generic_adls
WHERE
    (
        /*look for the custom adls*/
        custom_adls_for_users.id_adl = events_for_adls.id_adl
        AND custom_adls_for_users.version = events_for_adls.version
    )
    OR (
        /*look for the generic adls*/
        generic_adls.id_adl = events_for_adls.id_adl
        AND generic_adls.version = events_for_adls.version
    )
    AND custom_adls_for_users.id_user = 1
ORDER BY
    id ASC,
    id_adl ASC,
    id_event ASC;

/*GET THE EVENTS*/
SELECT
    *
FROM
    events
WHERE
    events.id IN (
        SELECT
            DISTINCT id_event
        FROM
            events_for_adls,
            custom_adls_for_users,
            generic_adls
        WHERE
            (
                /*look for the custom adls*/
                custom_adls_for_users.id_adl = events_for_adls.id_adl
                AND custom_adls_for_users.version = events_for_adls.version
            )
            OR (
                /*look for the generic adls*/
                generic_adls.id_adl = events_for_adls.id_adl
                AND generic_adls.version = events_for_adls.version
            )
            AND custom_adls_for_users.id_user = 1
    );

/*example of union of generics columns and custom columns*/
select
    id_adl,
    version
from
    custom_adls_for_users
where
    id_user = 1
UNION
select
    id_adl,
    version
from
    generic_adls
order by
    id_adl