
CREATE DATABASE tusa
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

--Токены пользователей
CREATE TABLE IF NOT EXISTS public.token
(
    expired boolean NOT NULL,
    revoked boolean NOT NULL,
    id bigint NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    user_id bigint,
    token character varying(255) COLLATE pg_catalog."default",
    token_type character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT token_pkey PRIMARY KEY (id),
    CONSTRAINT token_token_key UNIQUE (token),
    CONSTRAINT fkebe1hlldfjpivnyt2tlydy4vl FOREIGN KEY (user_id)
        REFERENCES public.app_user (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT token_token_type_check CHECK (token_type::text = 'BEARER'::text)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
ALTER TABLE IF EXISTS public.token
    OWNER to postgres;

--Смс коды для пользователей для входа в систему
CREATE TABLE IF NOT EXISTS public.sms_code
(
    activated boolean NOT NULL,
    expired_at timestamp(6) without time zone,
    id bigint NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    code character varying(255) COLLATE pg_catalog."default",
    phone character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT sms_code_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
ALTER TABLE IF EXISTS public.sms_code
    OWNER to postgres;


-- Пользователи
CREATE TABLE IF NOT EXISTS public.app_user
(
    id bigint NOT NULL,
    name character varying(255) COLLATE pg_catalog."default",
    phone character varying(255) COLLATE pg_catalog."default",
    role character varying(255) COLLATE pg_catalog."default",
    user_unique_name character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT app_user_pkey PRIMARY KEY (id),
    CONSTRAINT app_user_user_unique_name_key UNIQUE (user_unique_name),
    CONSTRAINT app_user_role_check CHECK (role::text = ANY (ARRAY['USER'::character varying, 'ADMIN'::character varying]::text[]))
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
ALTER TABLE IF EXISTS public.app_user
    OWNER to postgres;


-- местоположения пользлвателей на карте
CREATE TABLE IF NOT EXISTS public.location
(
    creation timestamp(6) without time zone,
    id bigint NOT NULL,
    owner_id bigint NOT NULL,
    latitude character varying(255) COLLATE pg_catalog."default",
    longitude character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT location_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
ALTER TABLE IF EXISTS public.location
    OWNER to postgres;


-- запросы в друзья пользователей дург к другу
CREATE TABLE IF NOT EXISTS public.friend_request
(
    date timestamp(6) without time zone,
    from_user_id bigint NOT NULL,
    id bigint NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1 ),
    to_user_id bigint NOT NULL,
    CONSTRAINT friend_request_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
ALTER TABLE IF EXISTS public.friend_request
    OWNER to postgres;

    