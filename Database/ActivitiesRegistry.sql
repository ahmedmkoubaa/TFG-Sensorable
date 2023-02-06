use ahmed_test;

DROP TABLE IF EXISTS steps_for_activities_registry,
steps_for_activities,
activity_steps,
activities;

CREATE TABLE activities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(200) NOT NULL
);

CREATE TABLE activity_steps (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE steps_for_activities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_activity INT NOT NULL,
    id_step INT NOT NULL,
    FOREIGN KEY (id_activity) REFERENCES activities(id) ON DELETE CASCADE,
    FOREIGN KEY (id_step) REFERENCES activity_steps(id) ON DELETE CASCADE,
    UNIQUE KEY(id_activity, id_step)
);

/*THIS SOULD BE THE REAL VERSION BUT WE HAD SOME PROBLEMS AND NOT ENOUGH TIME TO FIX THEM*/
/*CREATE TABLE steps_for_activities_registry (
 id INT AUTO_INCREMENT PRIMARY KEY,
 id_step_for_activity INT NOT NULL,
 timestamp BIGINT NOT NULL,
 FOREIGN KEY (id_step_for_activity) REFERENCES steps_for_activities(id) ON DELETE CASCADE
 );*/
CREATE TABLE steps_for_activities_registry (
    id INT AUTO_INCREMENT,
    id_activity INT NOT NULL,
    id_step INT NOT NULL,
    FOREIGN KEY (id_activity) REFERENCES activities(id) ON DELETE CASCADE,
    FOREIGN KEY (id_step) REFERENCES activity_steps(id) ON DELETE CASCADE,
    timestamp BIGINT NOT NULL,
    user_id VARCHAR(8) NOT NULL,
    PRIMARY KEY (id, user_id, timestamp)
);

/*steps to make easy the registry*/
INSERT INTO
    activity_steps (id, title)
VALUES
    (-2, "Finalizar actividad");

/* steps to make easy the registry */
INSERT INTO
    activity_steps (id, title)
VALUES
    (-1, "Comenzar actividad");

INSERT INTO
    activities (id, title, description)
VALUES
    (
        1,
        "Ponerse una bata sanitaria",
        "Actividad dentro del vestido con baja complejidad qie se puede subdividir en pasos. Se debe realizar en una sala de ambiente tranquilo, sin demasiados distracotres, con luz natural."
    );

INSERT INTO
    activity_steps (title)
VALUES
    ("Coge la bata");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza a meter la manga dominante");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba de meter la manga dominante");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza a meter la manga NO dominante");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba de meter la manga NO dominante");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba de colocarse la bata");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza a colocar el cuello");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba de colocar el cuello");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza a abrocharse los dos botones");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba de abrocharse los dos botones");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza a desabrocharse los dos botones");

INSERT INTO
    activity_steps (title)
VALUES
    ("Termina de desabrochar los dos botones");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza a quitar la manga dominante");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba de quitar la manga dominante");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza a quitar la manga NO dominante");

INSERT INTO
    activity_steps (title)
VALUES
    ("Deja la bata donde estaba");

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 1);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 2);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 3);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 4);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 5);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 6);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 7);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 8);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 9);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 10);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 11);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 12);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 13);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 14);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 15);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (1, 16);

INSERT INTO
    activities (id, title, description)
VALUES
    (
        2,
        "Ponerse un zapato y atarse los cordones",
        "Actividad dentro del vestido con complejidad media que se puede subdividir en pasos. Se debe realizar en una sala de ambiente tranquilo, sin demasiados distractores, con luz natural."
    );

INSERT INTO
    activity_steps (title)
VALUES
    ("Estar sentado");

INSERT INTO
    activity_steps (title)
VALUES
    ("Se coloca la bolsa en el pie.");

INSERT INTO
    activity_steps (title)
VALUES
    ("Coge el zapato o toca con el pie el zapato");

INSERT INTO
    activity_steps (title)
VALUES
    (
        "Empieza a meter el pie ensanchando la zona del elástico"
    );

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba de meter el pie");

INSERT INTO
    activity_steps (title)
VALUES
    (
        "Empieza a quitárselo ensanchando la zona del elástico"
    );

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba de quitarse el zapato");

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 17);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 18);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 19);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 20);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 21);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 22);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 23);

INSERT INTO
    activities (id, title, description)
VALUES
    (
        3,
        "Prueba del algómetro",
        "Un dispositivo algométrico de presión de dial equipado con una cabeza circular."
    );

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza trapecio dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba trapecio dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza trapecio dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba trapecio dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza trapecio dominante 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba trapecio dominante 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza trapecio NO dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba trapecio NO dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza trapecio NO dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba trapecio NO dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza trapecio NO dominante 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba trapecio NO dominante 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza antebrazo dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba antebrazo dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza antebrazo dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba antebrazo dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza antebrazo dominante 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba antebrazo dominante 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza antebrazo NO dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba antebrazo NO dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza antebrazo NO dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba antebrazo NO dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza antebrazo NO dominante 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba antebrazo NO dominante 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza mano dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba mano dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza mano dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba mano dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza mano dominante 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba mano dominante 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza mano NO dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba mano NO dominante 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza mano NO dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba mano NO dominante 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza mano NO dominante 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba mano NO dominante 3");

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 24);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 25);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 26);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 27);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 28);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 29);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 30);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 31);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 32);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 33);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 34);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 35);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 36);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 37);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 38);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 39);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 40);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 41);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 42);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 43);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 44);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 45);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 46);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 47);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 48);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 49);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 50);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 51);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 52);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 53);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 54);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 55);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 56);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 57);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 58);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (3, 59);

INSERT INTO
    activities (id, title, description)
VALUES
    (
        4,
        "Cuestionarios",
        "Se le pasan una serie de cuestionarios al usuario para comprobar reacciona ante ciertas preguntas."
    );

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza el cuestionario 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba el cuestionario 1");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza el cuestionario 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba el cuestionario 2");

INSERT INTO
    activity_steps (title)
VALUES
    ("Empieza el cuestionario 3");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acaba el cuestionario 3");

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (4, 60);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (4, 61);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (4, 62);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (4, 63);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (4, 64);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (4, 65);
