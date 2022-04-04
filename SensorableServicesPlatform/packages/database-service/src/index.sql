CREATE TABLE sensors(
    Id INT  AUTO_INCREMENT PRIMARY KEY, 
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