CREATE TABLE barbershops (
    id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT barbershops_pkey PRIMARY KEY (id)
);

CREATE TABLE barbers (
    id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    specialties VARCHAR(500),
    active BOOLEAN NOT NULL,
    barbershop_id UUID NOT NULL,
    CONSTRAINT barbers_pkey PRIMARY KEY (id),
    CONSTRAINT barbers_email_key UNIQUE (email),
    CONSTRAINT fk_barbers_barbershop
        FOREIGN KEY (barbershop_id) REFERENCES barbershops (id)
);

CREATE TABLE customers (
    id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    birth_date DATE,
    notes VARCHAR(500),
    active BOOLEAN NOT NULL,
    barbershop_id UUID NOT NULL,
    CONSTRAINT customers_pkey PRIMARY KEY (id),
    CONSTRAINT fk_customers_barbershop
        FOREIGN KEY (barbershop_id) REFERENCES barbershops (id)
);

CREATE TABLE services (
    id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    duration_minutes INTEGER NOT NULL,
    price NUMERIC(7, 2) NOT NULL,
    active BOOLEAN NOT NULL,
    barbershop_id UUID NOT NULL,
    CONSTRAINT services_pkey PRIMARY KEY (id),
    CONSTRAINT services_duration_minutes_check
        CHECK (duration_minutes >= 1 AND duration_minutes <= 480),
    CONSTRAINT fk_services_barbershop
        FOREIGN KEY (barbershop_id) REFERENCES barbershops (id)
);

CREATE TABLE barber_schedules (
    id UUID NOT NULL,
    barber_id UUID NOT NULL,
    max_booking_days INTEGER NOT NULL,
    default_break_minutes INTEGER NOT NULL,
    CONSTRAINT barber_schedules_pkey PRIMARY KEY (id),
    CONSTRAINT barber_schedules_barber_id_key UNIQUE (barber_id),
    CONSTRAINT fk_barber_schedules_barber
        FOREIGN KEY (barber_id) REFERENCES barbers (id)
);

CREATE TABLE weekly_schedule (
    id UUID NOT NULL,
    barber_schedule_id UUID NOT NULL,
    day_of_week VARCHAR(255) NOT NULL,
    start_time TIME,
    end_time TIME,
    break_start_time TIME,
    break_end_time TIME,
    working_day BOOLEAN NOT NULL,
    CONSTRAINT weekly_schedule_pkey PRIMARY KEY (id),
    CONSTRAINT uk_weekly_schedule_barber_schedule_day
        UNIQUE (barber_schedule_id, day_of_week),
    CONSTRAINT weekly_schedule_day_of_week_check CHECK (
        day_of_week IN (
            'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY',
            'FRIDAY', 'SATURDAY', 'SUNDAY'
        )
    ),
    CONSTRAINT fk_weekly_schedule_barber_schedule
        FOREIGN KEY (barber_schedule_id) REFERENCES barber_schedules (id)
);

CREATE TABLE schedule_blocks (
    id UUID NOT NULL,
    barber_schedule_id UUID NOT NULL,
    start_date_time TIMESTAMP NOT NULL,
    end_date_time TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT schedule_blocks_pkey PRIMARY KEY (id),
    CONSTRAINT fk_schedule_blocks_barber_schedule
        FOREIGN KEY (barber_schedule_id) REFERENCES barber_schedules (id)
);

CREATE TABLE appointments (
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    barber_id UUID NOT NULL,
    total_price NUMERIC(7, 2),
    appointment_date_time TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    cancel_token VARCHAR(64),
    CONSTRAINT appointments_pkey PRIMARY KEY (id),
    CONSTRAINT appointments_cancel_token_key UNIQUE (cancel_token),
    CONSTRAINT appointments_status_check CHECK (
        status IN ('SCHEDULED', 'COMPLETED', 'CANCELED', 'NO_SHOW')
    ),
    CONSTRAINT fk_appointments_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_appointments_barber
        FOREIGN KEY (barber_id) REFERENCES barbers (id)
);

CREATE TABLE appointment_services (
    appointment_id UUID NOT NULL,
    service_id UUID NOT NULL,
    CONSTRAINT fk_appointment_services_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_appointment_services_service
        FOREIGN KEY (service_id) REFERENCES services (id)
);

CREATE TABLE availability_interests (
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    appointment_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT availability_interests_pkey PRIMARY KEY (id),
    CONSTRAINT availability_interests_status_check CHECK (
        status IN ('ACTIVE', 'COMPLETED', 'CANCELED')
    ),
    CONSTRAINT fk_availability_interests_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_availability_interests_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments (id)
);

CREATE TABLE available_slots (
    id UUID NOT NULL,
    barber_id UUID NOT NULL,
    available_date_time TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT available_slots_pkey PRIMARY KEY (id),
    CONSTRAINT uk_available_slot_barber_datetime
        UNIQUE (barber_id, available_date_time),
    CONSTRAINT available_slots_status_check CHECK (
        status IN ('AVAILABLE', 'RESERVED', 'BOOKED')
    ),
    CONSTRAINT fk_available_slots_barber
        FOREIGN KEY (barber_id) REFERENCES barbers (id)
);

CREATE TABLE admin_users (
    id UUID NOT NULL,
    barbershop_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT admin_users_pkey PRIMARY KEY (id),
    CONSTRAINT admin_users_email_key UNIQUE (email),
    CONSTRAINT fk_admin_users_barbershop
        FOREIGN KEY (barbershop_id) REFERENCES barbershops (id)
);

CREATE TABLE admin_sessions (
    id UUID NOT NULL,
    admin_user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    CONSTRAINT admin_sessions_pkey PRIMARY KEY (id),
    CONSTRAINT admin_sessions_token_hash_key UNIQUE (token_hash),
    CONSTRAINT fk_admin_sessions_admin_user
        FOREIGN KEY (admin_user_id) REFERENCES admin_users (id)
);

CREATE TABLE push_subscriptions (
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    endpoint VARCHAR(2048) NOT NULL,
    p256dh TEXT NOT NULL,
    auth TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT push_subscriptions_pkey PRIMARY KEY (id),
    CONSTRAINT fk_push_subscriptions_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE UNIQUE INDEX ux_appointments_scheduled_slot
    ON appointments (barber_id, appointment_date_time)
    WHERE status = 'SCHEDULED';

CREATE UNIQUE INDEX ux_push_subscriptions_endpoint
    ON push_subscriptions (endpoint);

CREATE INDEX ix_push_subscriptions_customer_id
    ON push_subscriptions (customer_id);
