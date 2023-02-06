DROP TABLE IF EXISTS events_for_adls,
custom_adls_for_users,
generic_adls,
users,
events,
adls;

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

CREATE TABLE generic_adls (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_adl INT NOT NULL,
    version INT DEFAULT 1,
    FOREIGN KEY (id_adl) REFERENCES adls(id)
);

CREATE TABLE custom_adls_for_users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_user INT NOT NULL,
    id_adl INT NOT NULL,
    version INT DEFAULT 1,
    FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (id_adl) REFERENCES adls(id) ON DELETE CASCADE,
    UNIQUE KEY(id_user, id_adl, version)
);