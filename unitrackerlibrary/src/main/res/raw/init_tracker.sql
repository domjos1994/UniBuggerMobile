CREATE TABLE IF NOT EXISTS projects(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(1000) NOT NULL,
    shortTitle VARCHAR(255) DEFAULT '',
    privateProject TINYINT DEFAULT 0,
    enabledProject TINYINT NOT NULL DEFAULT 1,
    website VARCHAR(1000) DEFAULT '',
    statusText VARCHAR(50) DEFAULT '',
    statusID INTEGER DEFAULT 0,
    iconUrl VARCHAR(1000) DEFAULT '',
    defaultVersion VARCHAR(1000) DEFAULT '',
    description TEXT,
    authentication VARCHAR(1000) NOT NULL
);

CREATE TABLE IF NOT EXISTS subProjects(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    parentProject INTEGER NOT NULL,
    childProject INTEGER NOT NULL,
    FOREIGN KEY(parentProject) REFERENCES projects(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(childProject) REFERENCES projects(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS versions(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(1000) NOT NULL,
    releasedVersion TINYINT DEFAULT 0,
    deprecatedVersion TINYINT NOT NULL DEFAULT 1,
    releasedVersionAt LONG DEFAULT 0,
    description TEXT,
    project INTEGER NOT NULL,
    FOREIGN KEY(project) REFERENCES projects(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS issues(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(1000) NOT NULL,
    category VARCHAR(1000) DEFAULT '',
    state_id INTEGER DEFAULT 0,
    priority_id INTEGER DEFAULT 0,
    severity_id INTEGER DEFAULT 0,
    status_id INTEGER DEFAULT 0,
    reproducibility_id INTEGER DEFAULT 0,
    resolution_id INTEGER DEFAULT 0,
    version VARCHAR(1000) DEFAULT '',
    fixedInVersion VARCHAR(1000) DEFAULT '',
    targetVersion VARCHAR(1000) DEFAULT '',
    tags TEXT,
    dueDate LONG DEFAULT 0,
    lastUpdated LONG DEFAULT 0,
    submitDate LONG DEFAULT 0,
    description TEXT,
    steps_to_reproduce TEXT,
    additional_information TEXT,
    platform VARCHAR(500) DEFAULT '',
    os VARCHAR(500) DEFAULT '',
    os_build VARCHAR(500) DEFAULT '',
    project INTEGER NOT NULL,
    FOREIGN KEY(project) REFERENCES projects(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(version) REFERENCES versions(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(fixedInVersion) REFERENCES versions(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(targetVersion) REFERENCES versions(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS notes(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(1000) NOT NULL,
    state_id INTEGER DEFAULT 0,
    description TEXT,
    lustUpdated LONG DEFAULT 0,
    submitDate LONG DEFAULT 0,
    issue INTEGER DEFAULT 0,
    FOREIGN KEY(issue) REFERENCES issues(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS attachments(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(1000) NOT NULL,
    download_url VARCHAR(200) DEFAULT '',
    content BLOB NOT NULL,
    issue INTEGER DEFAULT 0,
    FOREIGN KEY(issue) REFERENCES issues(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS customFields(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(1000) NOT NULL,
    type INTEGER DEFAULT 0,
    possibleValues TEXT,
    defaultValue VARCHAR(1000) NOT NULL,
    minLength INTEGER DEFAULT 0,
    maxLength INTEGER DEFAULT 0,
    description TEXT,
    project INTEGER DEFAULT 0,
    FOREIGN KEY(project) REFERENCES projects(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS fieldResult(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    fieldValue TEXT,
    field INTEGER NOT NULL,
    issue INTEGER NOT NULL,
    FOREIGN KEY(field) REFERENCES customFields(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(issue) REFERENCES issues(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS history(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    field VARCHAR(100),
    oldVal TEXT,
    newVal TEXT,
    timestamp LONG,
    issue INTEGER NOT NULL,
    FOREIGN KEY(issue) REFERENCES issues(id) ON DELETE CASCADE ON UPDATE CASCADE
);