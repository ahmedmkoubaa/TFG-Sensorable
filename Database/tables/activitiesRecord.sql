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