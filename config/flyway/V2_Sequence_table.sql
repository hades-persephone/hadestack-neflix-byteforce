-- Sequence cho movies
CREATE SEQUENCE movie_code_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO CYCLE;

-- Sequence cho series
CREATE SEQUENCE series_code_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO CYCLE;


-- Function sinh code cho movies
CREATE OR REPLACE FUNCTION generate_movie_code()
RETURNS TRIGGER AS $$
BEGIN
    NEW.code := 'MOV-' || LPAD(nextval('movie_code_seq')::TEXT, 4, '0');
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function sinh code cho series
CREATE OR REPLACE FUNCTION generate_series_code()
RETURNS TRIGGER AS $$
BEGIN
    NEW.code := 'SER-' || LPAD(nextval('series_code_seq')::TEXT, 4, '0');
RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Trigger cho movies
CREATE TRIGGER trigger_generate_movie_code
    BEFORE INSERT ON movies
    FOR EACH ROW
    EXECUTE FUNCTION generate_movie_code();

-- Trigger cho series
CREATE TRIGGER trigger_generate_series_code
    BEFORE INSERT ON series
    FOR EACH ROW
    EXECUTE FUNCTION generate_series_code();