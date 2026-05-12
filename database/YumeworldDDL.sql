-- Script de creació de les taules de la BD yumeworld

-- ==========================================
-- 1. ELIMINACIÓ DE TAULES (Ordre invers a la creació)
-- ==========================================

-- Primer les taules que connecten molts a molts o tenen moltes dependències
DROP TABLE IF EXISTS ENEMY_FLOORS;
DROP TABLE IF EXISTS ENEMY_DROPS;
DROP TABLE IF EXISTS YUMEDANT_ACT;
DROP TABLE IF EXISTS PLAYERROLE_ABILITY; 
DROP TABLE IF EXISTS PARAMETER_VALUE;
DROP TABLE IF EXISTS PARAMETER_EFFECT;
DROP TABLE IF EXISTS ACTION_EFFECT;

-- Després les entitats específiques (Fills)
DROP TABLE IF EXISTS PLAYER;
DROP TABLE IF EXISTS ENEMY;
DROP TABLE IF EXISTS ABILITY;
DROP TABLE IF EXISTS ITEM;
DROP TABLE IF EXISTS FLOOR;

-- Finalment les entitats base i catàlegs (Pares)
DROP TABLE IF EXISTS YUMEDANT;
DROP TABLE IF EXISTS PARAMETER_TYPES;
DROP TABLE IF EXISTS PLAYERROLE;
DROP TABLE IF EXISTS ACT;
DROP TABLE IF EXISTS EFFECT;
DROP TABLE IF EXISTS BIOME;
DROP TABLE IF EXISTS ALTERED_STATE;


-- ==========================================
-- 2. CREACIÓ DE TAULES (Pares primer)
-- ==========================================

-- Taules sense Referències
CREATE TABLE EFFECT (
    ef_id int NOT NULL AUTO_INCREMENT,
    ef_name varchar(255),
    PRIMARY KEY(ef_id) 
);


CREATE TABLE ACT (
    ac_id int NOT NULL AUTO_INCREMENT,
    ac_name varchar(255) NOT NULL,
    ac_desc varchar(255) NOT NULL,
    ac_img varchar(500),
    ac_type char(1), -- I = Item, A = Ability
    PRIMARY KEY (ac_id),
    CONSTRAINT chk_ac_type CHECK (ac_type IN ('I', 'A'))
);


CREATE TABLE PLAYERROLE (
    pr_id int NOT NULL AUTO_INCREMENT,
    pr_name varchar(50),
    pr_desc varchar(255),
    pr_hp int NOT NULL DEFAULT 0,
    pr_mp int NOT NULL DEFAULT 0,
    pr_atk int NOT NULL DEFAULT 0,
    pr_def int NOT NULL DEFAULT 0,
    PRIMARY KEY (pr_id)
);

CREATE TABLE BIOME(
    bi_id int NOT NULL AUTO_INCREMENT,
    bi_background varchar(500),
    PRIMARY KEY (bi_id)
);

-- Taula mapejada pel DbContext amb valors dins d'un ENUM
-- Valors de PARAMETER_TYPES:
-- Stat, MaxRange, MinRange, Mangitude, ValueType, Target, Duration, Effect
CREATE TABLE PARAMETER_TYPES ( 
    pt_id int NOT NULL AUTO_INCREMENT,
    pt_name varchar(20) NOT NULL,
    pt_type char(1) NOT NULL, -- N(number), S(string), L(list)
    pt_num_min int,
    pt_num_max int,  
    pt_value_list varchar(255),

    CONSTRAINT chk_pt_type CHECK (pt_type IN ('N', 'S', 'L')),
    PRIMARY KEY(pt_id)
);


CREATE TABLE YUMEDANT (
    yu_id int NOT NULL AUTO_INCREMENT,
    yu_name varchar(255) NOT NULL,
    yu_desc varchar(255),
	yu_level int NOT NULL DEFAULT 1,
    yu_hp int NOT NULL DEFAULT 1,  
    yu_mp int NOT NULL DEFAULT 1,   
    yu_atk int NOT NULL DEFAULT 1,  
    yu_def int NOT NULL DEFAULT 1,
    yu_image varchar(500),
	PRIMARY KEY (yu_id),
    CONSTRAINT chk_yu_stats CHECK (yu_level >= 1 AND yu_hp >= 0 AND yu_mp >= 0 AND yu_atk >= 0 AND yu_def >= 0)
);

CREATE TABLE ALTERED_STATE (
    as_id int NOT NULL AUTO_INCREMENT,
    as_name varchar(50),
    as_icon varchar(500),
    PRIMARY KEY(as_id)
);

-- ==========================================
-- 3. TAULES AMB DEPENDÈNCIES (Fills)
-- ==========================================

CREATE TABLE FLOOR (
    fl_id int NOT NULL AUTO_INCREMENT,
    fl_num int NOT NULL,
    fl_name varchar(20) NOT NULL,
    fl_biome_id int NOT NULL,
    PRIMARY KEY (fl_id),
    FOREIGN KEY (fl_biome_id) REFERENCES BIOME(bi_id)
    -- CONSTRAINT chk_fl_num CHECK (fl_num >= 0)
);

CREATE TABLE ITEM(
    it_action_id int NOT NULL,
    PRIMARY KEY (it_action_id),
    FOREIGN KEY (it_action_id) REFERENCES ACT(ac_id)
);


CREATE TABLE ABILITY(
    ab_action_id int NOT NULL,
    ab_cost int NOT NULL DEFAULT 10,
    PRIMARY KEY (ab_action_id),
    FOREIGN KEY (ab_action_id) REFERENCES ACT(ac_id),
    CONSTRAINT chk_ab_cost CHECK (ab_cost >= 0)
);


CREATE TABLE ACTION_EFFECT (
    ae_id int NOT NULL AUTO_INCREMENT,
    ae_effect_id int NOT NULL,
    ae_action_id int NOT NULL,
    PRIMARY KEY(ae_id),
    CONSTRAINT FK_ACTIONEFFECT_EFFECT FOREIGN KEY(ae_effect_id) REFERENCES EFFECT(ef_id),
    CONSTRAINT FK_ACTIONEFFECT_ACT FOREIGN KEY(ae_action_id) REFERENCES ACT(ac_id)
);


CREATE TABLE PARAMETER_EFFECT (
    pe_parametertype_id int NOT NULL,
    pe_effect_id int NOT NULL,
    PRIMARY KEY(pe_parametertype_id, pe_effect_id),
    FOREIGN KEY(pe_parametertype_id) REFERENCES PARAMETER_TYPES(pt_id),
    FOREIGN KEY(pe_effect_id) REFERENCES EFFECT(ef_id)
);


CREATE TABLE PARAMETER_VALUE (
    pv_actioneffect_id int NOT NULL,
    pv_parametertype_id int NOT NULL,
    pv_effect_id int NOT NULL,
    pv_string_value varchar(10), -- Pot ser null no ser utilitzat
    pv_num_value int, -- Pot ser null, no ser utilitzat
    pv_altered_state_value int, -- Pot ser null, no ser utilitzat (Per a efectes alterats)
    PRIMARY KEY(pv_actioneffect_id, pv_parametertype_id),
    FOREIGN KEY (pv_actioneffect_id) REFERENCES ACTION_EFFECT (ae_id),
    FOREIGN KEY(pv_parametertype_id, pv_effect_id) REFERENCES PARAMETER_EFFECT(pe_parametertype_id, pe_effect_id),
    FOREIGN KEY(pv_altered_state_value) REFERENCES ALTERED_STATE(as_id)
);


CREATE TABLE ENEMY (
    en_yumedant_id int NOT NULL,
    en_given_exp int NOT NULL,
    en_given_coins int NOT NULL DEFAULT 50,
    PRIMARY KEY (en_yumedant_id),
    FOREIGN KEY (en_yumedant_id) REFERENCES YUMEDANT(yu_id)
);


CREATE TABLE PLAYER (
    pl_yumedant_id int NOT NULL,
    pl_exp int NOT NULL,
    pl_role_id int,
    PRIMARY KEY (pl_yumedant_id),
    FOREIGN KEY (pl_yumedant_id) REFERENCES YUMEDANT(yu_id),
    FOREIGN KEY (pl_role_id) REFERENCES PLAYERROLE(pr_id),
    CONSTRAINT chk_pl_exp CHECK (pl_exp >= 0)
);


-- ==========================================
-- 4. TAULES DE RELACIÓ (N:M)
-- ==========================================

CREATE TABLE YUMEDANT_ACT (
    ya_yumedant_id int NOT NULL,
    ya_action_id int NOT NULL,
    PRIMARY KEY (ya_yumedant_id, ya_action_id),
    FOREIGN KEY (ya_yumedant_id) REFERENCES YUMEDANT(yu_id),
    FOREIGN KEY (ya_action_id) REFERENCES ACT(ac_id)
);

CREATE TABLE PLAYERROLE_ABILITY (
    ra_playerrole_id int NOT NULL,
    ra_ability_id int NOT NULL,
    PRIMARY KEY (ra_playerrole_id, ra_ability_id),
    FOREIGN KEY (ra_playerrole_id) REFERENCES PLAYERROLE(pr_id),
    FOREIGN KEY (ra_ability_id) REFERENCES ABILITY(ab_action_id)
);


CREATE TABLE ENEMY_DROPS (
    ed_item_id int NOT NULL,
    ed_enemy_id int NOT NULL,
    ed_chance float NOT NULL DEFAULT 0.05, -- És un 5%
    PRIMARY KEY (ed_item_id, ed_enemy_id),
    FOREIGN KEY (ed_item_id) REFERENCES ITEM(it_action_id),
    FOREIGN KEY (ed_enemy_id) REFERENCES ENEMY(en_yumedant_id),
    CONSTRAINT chk_ed_chance CHECK (ed_chance >= 0 AND ed_chance <= 1)
);


CREATE TABLE ENEMY_FLOORS (
    enfl_enemy_id int NOT NULL,
    enfl_floor_id int NOT NULL,
    enfl_quantity int NOT NULL,
    PRIMARY KEY (enfl_enemy_id, enfl_floor_id),
    FOREIGN KEY (enfl_enemy_id) REFERENCES ENEMY(en_yumedant_id),
    FOREIGN KEY (enfl_floor_id) REFERENCES FLOOR(fl_id),
    CONSTRAINT chk_enfl_qty CHECK (enfl_quantity > 0)
);


-- ==========================================
-- 5. ÍNDEXS
-- ==========================================

ALTER TABLE ACT ADD INDEX idx_action_name (ac_name);
ALTER TABLE YUMEDANT ADD INDEX idx_yumedant_name (yu_name);


-- ==========================================
-- 6. TRIGGERS
-- ==========================================
DELIMITER //

CREATE TRIGGER trg_after_yumedant_insert
AFTER INSERT ON YUMEDANT
FOR EACH ROW
BEGIN
    -- L'atac bàsic ha de ser ya_action_id=1;
    INSERT INTO YUMEDANT_ACT (ya_yumedant_id, ya_action_id)
    VALUES (NEW.yu_id, 1);
END //

DELIMITER ;