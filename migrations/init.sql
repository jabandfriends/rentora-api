CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'tenant' or 'admin'
    created_at TIMESTAMP DEFAULT NOW()
);

-- Insert default admin
INSERT INTO users (name, email, password, role)
VALUES (
    'Admin',
    'admin@example.com',
    'admin123',
    'admin'
)
ON CONFLICT (email) DO NOTHING; 

CREATE TABLE IF NOT EXISTS apartments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    created_by INT REFERENCES users(id), -- creator, admin
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS apartment_admins (
    id SERIAL PRIMARY KEY,
    apartment_id INT REFERENCES apartments(id) ON DELETE CASCADE,
    admin_id INT REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'admin', -- 'creator' or 'admin'
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(apartment_id, admin_id)
);

CREATE TABLE IF NOT EXISTS units (
    id SERIAL PRIMARY KEY,
    apartment_id INT REFERENCES apartments(id) ON DELETE CASCADE,
    name VARCHAR(50), -- e.g., Room 101
    floor INT,
    status VARCHAR(20) DEFAULT 'available', -- 'available' or 'occupied'
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS utilities (
    id SERIAL PRIMARY KEY,
    apartment_id INT REFERENCES apartments(id) ON DELETE CASCADE,
    name VARCHAR(50), -- e.g., water, power
    type VARCHAR(20) NOT NULL, -- 'fixed' or 'meter'
    fixed_price NUMERIC(10,2),
    unit_price NUMERIC(10,2),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS extra_services (
    id SERIAL PRIMARY KEY,
    apartment_id INT REFERENCES apartments(id) ON DELETE CASCADE,
    name VARCHAR(50), -- e.g., fitness, parking
    price NUMERIC(10,2),
    billing_type VARCHAR(20) DEFAULT 'monthly', -- 'monthly' or 'one-time'
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS apartment_payments (
    id SERIAL PRIMARY KEY,
    apartment_id INT REFERENCES apartments(id) ON DELETE CASCADE,
    bank_name VARCHAR(100),
    bank_account_number VARCHAR(50),
    promptpay_qr_url TEXT,
    created_by INT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS contracts (
    id SERIAL PRIMARY KEY,
    unit_id INT REFERENCES units(id) ON DELETE CASCADE,
    tenant_id INT REFERENCES users(id),
    rental_type VARCHAR(20) NOT NULL, -- daily, monthly, yearly
    start_date DATE NOT NULL,
    end_date DATE,
    price NUMERIC(10,2),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS unit_utilities (
    id SERIAL PRIMARY KEY,
    unit_id INT REFERENCES units(id) ON DELETE CASCADE,
    utility_id INT REFERENCES utilities(id) ON DELETE CASCADE,
    meter_start NUMERIC(10,2),
    meter_end NUMERIC(10,2),
    month DATE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS invoices (
    id SERIAL PRIMARY KEY,
    contract_id INT REFERENCES contracts(id) ON DELETE CASCADE,
    invoice_type VARCHAR(20), -- rent, utility, extra_service, normal
    total_amount NUMERIC(10,2),
    month DATE,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,
    invoice_id INT REFERENCES invoices(id) ON DELETE CASCADE,
    amount NUMERIC(10,2),
    payment_method VARCHAR(50),
    paid_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS maintenance_requests (
    id SERIAL PRIMARY KEY,
    unit_id INT REFERENCES units(id) ON DELETE CASCADE,
    tenant_id INT REFERENCES users(id),
    title VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'pending', -- pending, in_progress, completed
    priority VARCHAR(20) DEFAULT 'normal',
    assigned_admin_id INT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
