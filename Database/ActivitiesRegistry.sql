use test;

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

CREATE TABLE steps_for_activities_registry (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_step_for_activity INT NOT NULL,
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (id_step_for_activity) REFERENCES steps_for_activities(id) ON DELETE CASCADE
);

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
    ("Coger la bata");

INSERT INTO
    activity_steps (title)
VALUES
    ("Ponerse la manga del brazo dominante");

INSERT INTO
    activity_steps (title)
VALUES
    ("Coger la otra manga");

INSERT INTO
    activity_steps (title)
VALUES
    ("Ponerse la manga del brazo NO dominante");

INSERT INTO
    activity_steps (title)
VALUES
    ("Colocarse la bata correctamente");

INSERT INTO
    activity_steps (title)
VALUES
    ("Acomodarse el cuello correctamente");

INSERT INTO
    activity_steps (title)
VALUES
    ("Se abrocha un botón");

INSERT INTO
    activity_steps (title)
VALUES
    ("Termina de acomodarse la bata finalmente");

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
    ("Acaba de meter el pie en el zapato");

INSERT INTO
    activity_steps (title)
VALUES
    ("Ajusta la lengüeta");

INSERT INTO
    activity_steps (title)
VALUES
    ("Se ata los cordones");

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 9);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 10);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 11);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 12);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 13);

INSERT INTO
    steps_for_activities (id_activity, id_step)
VALUES
    (2, 14);