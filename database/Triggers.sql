DELIMITER //

CREATE TRIGGER trg_before_yumedant_insert
BEFORE INSERT ON YUMEDANT
FOR EACH ROW
BEGIN
    IF NEW.yu_hp <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: L''HP ha de ser major que 0';
    END IF;
    IF NEW.yu_level < 1 THEN
        SET NEW.yu_level = 1;
    END IF;
END //

DELIMITER ;

DELIMITER //

CREATE TRIGGER trg_before_act_delete
BEFORE DELETE ON ACT
FOR EACH ROW
BEGIN
    -- Borrem els valors dels paràmetres associats a les relacions d'aquesta acció
    DELETE FROM PARAMETER_VALUE 
    WHERE pv_actioneffect_id IN (SELECT ae_id FROM ACTION_EFFECT WHERE ae_action_id = OLD.ac_id);
    
    -- Borrem la relació de l'efecte amb l'acció
    DELETE FROM ACTION_EFFECT WHERE ae_action_id = OLD.ac_id;
END //

DELIMITER ;

DELIMITER //

CREATE TRIGGER trg_after_act_insert
AFTER INSERT ON ACT
FOR EACH ROW
BEGIN
    -- Si és una habilitat, podríem crear automàticament l'entrada a ABILITY amb cost per defecte
    IF NEW.ac_type = 'A' THEN
        INSERT IGNORE INTO ABILITY (ab_action_id, ab_cost) VALUES (NEW.ac_id, 10);
    -- Si és un objecte, l'inserim a la taula ITEM
    ELSEIF NEW.ac_type = 'I' THEN
        INSERT IGNORE INTO ITEM (it_action_id) VALUES (NEW.ac_id);
    END IF;
END //

DELIMITER ;

DELIMITER //

CREATE TRIGGER trg_before_enemy_drops_update
BEFORE INSERT ON ENEMY_DROPS
FOR EACH ROW
BEGIN
    IF NEW.ed_chance < 0 OR NEW.ed_chance > 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: El drop chance ha d''estar entre 0 i 1 (ex: 0.05 per a un 5%)';
    END IF;
END //

DELIMITER ;