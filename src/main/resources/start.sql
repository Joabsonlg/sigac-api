CREATE TABLE users
(
    cpf      VARCHAR(45) PRIMARY KEY,
    email    VARCHAR(255),
    name     VARCHAR(255),
    password VARCHAR(255),
    address  VARCHAR(255),
    phone    VARCHAR(255)
);

CREATE TABLE client
(
    user_cpf VARCHAR(45) PRIMARY KEY,
    FOREIGN KEY (user_cpf) REFERENCES users (cpf)
);

CREATE TABLE employee
(
    user_cpf VARCHAR(45) PRIMARY KEY,
    role     VARCHAR(45),
    FOREIGN KEY (user_cpf) REFERENCES users (cpf)
);

CREATE TABLE vehicle
(
    plate     VARCHAR(45) PRIMARY KEY,
    year      INTEGER,
    model     VARCHAR(45),
    brand     VARCHAR(45),
    status    VARCHAR(45),
    image_url VARCHAR(255)
);

CREATE TABLE promotion
(
    code                SERIAL PRIMARY KEY,
    discount_percentage INT,
    status              VARCHAR(45),
    start_date          TIMESTAMP,
    end_date            TIMESTAMP
);

CREATE TABLE reservation
(
    id                SERIAL PRIMARY KEY,
    start_date        TIMESTAMP,
    end_date          TIMESTAMP,
    reservation_date  TIMESTAMP,
    status            VARCHAR(45),
    promotion_code    INT,
    client_user_cpf   VARCHAR(45),
    employee_user_cpf VARCHAR(45),
    vehicle_plate     VARCHAR(45),
    FOREIGN KEY (promotion_code) REFERENCES promotion (code),
    FOREIGN KEY (client_user_cpf) REFERENCES client (user_cpf),
    FOREIGN KEY (employee_user_cpf) REFERENCES employee (user_cpf),
    FOREIGN KEY (vehicle_plate) REFERENCES vehicle (plate)
);

CREATE TABLE payment
(
    id             SERIAL PRIMARY KEY,
    status         VARCHAR(45),
    payment_date   TIMESTAMP,
    payment_method VARCHAR(45),
    amount         DECIMAL(10,2),
    reservation_id INT,
    FOREIGN KEY (reservation_id) REFERENCES reservation (id)
);

CREATE TABLE maintenance
(
    id                SERIAL PRIMARY KEY,
    scheduled_date    TIMESTAMP,
    performed_date    TIMESTAMP,
    description       VARCHAR(45),
    type              VARCHAR(45),
    status            VARCHAR(45),
    cost              DECIMAL(10,2),
    employee_user_cpf VARCHAR(45),
    vehicle_plate     VARCHAR(45),
    FOREIGN KEY (employee_user_cpf) REFERENCES employee (user_cpf),
    FOREIGN KEY (vehicle_plate) REFERENCES vehicle (plate)
);

CREATE TABLE daily_rate
(
    id            SERIAL PRIMARY KEY,
    amount        DOUBLE,
    date_time     TIMESTAMP,
    vehicle_plate VARCHAR(45),
    FOREIGN KEY (vehicle_plate) REFERENCES vehicle (plate)
);

-- Inserindo dados de exemplo

-- Usuários (funcionários e clientes) - Senhas criptografadas com BCrypt
INSERT INTO users (cpf, email, name, password, address, phone) VALUES
('26733453029', 'admin@sigac.com', 'João Silva', '$2a$10$kmycAsrWHpn6/dK3gB03Le7.QvmM2FdbkzmOZyn0oD4u3yHZP4PPa', 'Rua das Flores, 123, Centro', '(11) 98765-4321'),
('06619780016', 'ana.santos@sigac.com', 'Ana Santos', '$2a$10$nvvuC38H8O6VUDl7vWoq6erOLwajM1Pe6fz/N19nmL10E5MH1NKqG', 'Av. Paulista, 1000, Bela Vista', '(11) 87654-3210'),
('09206821032', 'carlos.oliveira@email.com', 'Carlos Oliveira', '$2a$10$SDydrUszbhuCUWAvDs513eArLbgB.7rtVTHvW2DWhATyA4THqyzj2', 'Rua Augusta, 500, Consolação', '(11) 76543-2109'),
('96256913086', 'maria.costa@email.com', 'Maria Costa', '$2a$10$dcAIq.j4hGkXnMO.JL0gFu9qrplztlqRszmXDnd5TL3nSHh2Z1TIe', 'Rua Oscar Freire, 200, Jardins', '(11) 65432-1098'),
('16835741090', 'pedro.almeida@email.com', 'Pedro Almeida', '$2a$10$FKvttLGy6Zd7g/.Y6tx6X.Pde5iH9Oav2k/Fx61D2rl6EXiEkmLWC', 'Av. Rebouças, 300, Pinheiros', '(11) 54321-0987'),
('56742834005', 'lucia.ferreira@sigac.com', 'Lúcia Ferreira', '$2a$10$O.CEM8q7UKmWbYMRBsh0SeEeXh3mpfjYnMlBaDBcv.Hc/ZfDwvc4C', 'Rua Haddock Lobo, 400, Cerqueira César', '(11) 43210-9876'),
('43918843050', 'jose.rodrigues@email.com', 'José Rodrigues', '$2a$10$pVMODYtOmeFVGF0/AQc9ie6NjdmBmEvbAVrmnz.LpAPa/2L7wGowq', 'Av. Ibirapuera, 600, Moema', '(11) 32109-8765'),
('50420937021', 'fernanda.lima@email.com', 'Fernanda Lima', '$2a$10$UnMGetlD0/Ok53zwRCR6jOYhyOv1gLDsf1N0Ts3b49Kvu2nSy8G2W', 'Rua Pamplona, 700, Jardim Paulista', '(11) 21098-7654');

-- Funcionários, role em ingles
INSERT INTO employee (user_cpf, role) VALUES
('26733453029', 'ADMIN'),
('06619780016', 'ATTENDANT'),
('56742834005', 'MANAGER');

-- Clientes
INSERT INTO client (user_cpf) VALUES
('09206821032'),
('96256913086'),
('16835741090'),
('43918843050'),
('50420937021');

-- Veículos
INSERT INTO vehicle (plate, year, model, brand, status, image_url) VALUES
('ABC1234', '2023', 'Civic', 'Honda', 'DISPONIVEL', 'https://example.com/civic.jpg'),
('DEF5678', '2022', 'Corolla', 'Toyota', 'DISPONIVEL', 'https://example.com/corolla.jpg'),
('GHI9012', '2023', 'HB20', 'Hyundai', 'DISPONIVEL', 'https://example.com/hb20.jpg'),
('JKL3456', '2021', 'Onix', 'Chevrolet', 'DISPONIVEL', 'https://example.com/onix.jpg'),
('MNO7890', '2023', 'Polo', 'Volkswagen', 'MANUTENCAO', 'https://example.com/polo.jpg'),
('PQR1234', '2022', 'Compass', 'Jeep', 'DISPONIVEL', 'https://example.com/compass.jpg'),
('STU5678', '2023', 'T-Cross', 'Volkswagen', 'DISPONIVEL', 'https://example.com/tcross.jpg'),
('VWX9012', '2021', 'Ecosport', 'Ford', 'DISPONIVEL', 'https://example.com/ecosport.jpg');

-- Promoções
INSERT INTO promotion (discount_percentage, status, start_date, end_date) VALUES
(15, 'ATIVA', '2025-06-01 00:00:00', '2025-07-31 23:59:59'),
(20, 'ATIVA', '2025-07-01 00:00:00', '2025-08-15 23:59:59'),
(10, 'INATIVA', '2025-05-01 00:00:00', '2025-05-31 23:59:59'),
(25, 'PROGRAMADA', '2025-08-01 00:00:00', '2025-08-31 23:59:59');

-- Reservas
INSERT INTO reservation (start_date, end_date, reservation_date, status, promotion_code, client_user_cpf, employee_user_cpf, vehicle_plate) VALUES
('2025-07-05 09:00:00', '2025-07-10 18:00:00', '2025-07-01 14:30:00', 'CONFIRMADA', 1, '09206821032', '06619780016', 'ABC1234'),
('2025-07-03 10:00:00', '2025-07-08 17:00:00', '2025-06-30 16:45:00', 'EM_ANDAMENTO', 2, '96256913086', '06619780016', 'GHI9012'),
('2025-07-08 08:00:00', '2025-07-12 20:00:00', '2025-07-02 11:20:00', 'CONFIRMADA', NULL, '16835741090', '56742834005', 'DEF5678'),
('2025-07-15 14:00:00', '2025-07-20 12:00:00', '2025-07-02 09:15:00', 'PENDENTE', 1, '43918843050', '06619780016', 'JKL3456'),
('2025-06-25 07:00:00', '2025-06-30 19:00:00', '2025-06-20 13:40:00', 'FINALIZADA', NULL, '50420937021', '56742834005', 'VWX9012');

-- Pagamentos
INSERT INTO payment (status, payment_date, payment_method, amount, reservation_id) VALUES
(1, '2025-07-01 15:00:00', 'CARTAO_CREDITO', 42500, 1),
(1, '2025-06-30 17:00:00', 'PIX', 38000, 2),
(0, NULL, 'CARTAO_DEBITO', 35000, 3),
(0, NULL, 'DINHEIRO', 40000, 4),
(1, '2025-06-30 20:00:00', 'CARTAO_CREDITO', 32000, 5);

-- Manutenções
INSERT INTO maintenance (scheduled_date, performed_date, description, type, status, cost, employee_user_cpf, vehicle_plate) VALUES
('2025-07-03 08:00:00', '2025-07-03 10:30:00', 'Troca de óleo e filtros', 'PREVENTIVA', 'CONCLUIDA', '250.00', '56742834005', 'MNO7890'),
('2025-06-28 14:00:00', '2025-06-28 16:45:00', 'Reparo no freio dianteiro', 'CORRETIVA', 'CONCLUIDA', '480.00', '06619780016', 'STU5678'),
('2025-07-05 09:00:00', NULL, 'Revisão dos 10.000 km', 'PREVENTIVA', 'AGENDADA', '350.00', '56742834005', 'PQR1234'),
('2025-07-10 13:00:00', NULL, 'Troca de pneus', 'PREVENTIVA', 'AGENDADA', '800.00', '06619780016', 'ABC1234');

-- Tarifas diárias
INSERT INTO daily_rate (amount, date_time, vehicle_plate) VALUES
(8500, '2025-07-01 00:00:00', 'ABC1234'),
(7600, '2025-07-01 00:00:00', 'DEF5678'),
(6500, '2025-07-01 00:00:00', 'GHI9012'),
(7000, '2025-07-01 00:00:00', 'JKL3456'),
(7200, '2025-07-01 00:00:00', 'MNO7890'),
(9500, '2025-07-01 00:00:00', 'PQR1234'),
(8200, '2025-07-01 00:00:00', 'STU5678'),
(6800, '2025-07-01 00:00:00', 'VWX9012'),
-- Tarifas para o dia seguinte (simulando variação de preços)
(8700, '2025-07-02 00:00:00', 'ABC1234'),
(7800, '2025-07-02 00:00:00', 'DEF5678'),
(6700, '2025-07-02 00:00:00', 'GHI9012'),
(7200, '2025-07-02 00:00:00', 'JKL3456'),
(7400, '2025-07-02 00:00:00', 'MNO7890'),
(9700, '2025-07-02 00:00:00', 'PQR1234'),
(8400, '2025-07-02 00:00:00', 'STU5678'),
(7000, '2025-07-02 00:00:00', 'VWX9012');