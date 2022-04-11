CREATE TABLE sensors(
    Id INT AUTO_INCREMENT PRIMARY KEY,
    device_type INT NOT NULL,
    sensor_type INT NOT NULL,
    values_x FLOAT NOT NULL,
    values_y FLOAT,
    values_z FLOAT,
    timestamp BIGINT NOT NULL
);

/*EXAMPLE OF INSERT:
 INSERT INTO sensors (device_type, sensor_type, values_x, values_y, values_z, timestamp) VALUES (0,21, 67, -1, -1, 1234567981)
 */


CREATE TABLE adls (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL
) 

CREATE TABLE events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    device_type INT NOT NULL,
    sensor_type INT NOT NULL,
    pos INT DEFAULT 0,
    operator ENUM('GREATER', 'LESS', 'EQUAL', 'GREATER_EQUAL', 'LESS_EQUAL', 'NOT_EQUAL') NOT NULL,
    operand FLOAT NOT NULL
)

CREATE TABLE events_for_adls (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_adl INT,
    id_event INT,

    FOREIGN KEY (id_adl) REFERENCES adls(id) ON DELETE CASCADE,
    FOREIGN KEY (id_event) REFERENCES events(id) ON DELETE CASCADE
)