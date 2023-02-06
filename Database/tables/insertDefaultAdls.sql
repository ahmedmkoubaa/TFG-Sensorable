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