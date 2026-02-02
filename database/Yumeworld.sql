-- Script de creació de les taules de la BD yumeworld

-- ==========================================
-- 1. ELIMINACIÓ DE TAULES (Ordre invers a la creació)
-- ==========================================

-- Primer les taules que connecten molts a molts o tenen moltes dependències
DROP TABLE IF EXISTS ENEMY_FLOORS;
DROP TABLE IF EXISTS ENEMY_DROPS;
DROP TABLE IF EXISTS PLAYER_ITEM;        
DROP TABLE IF EXISTS PLAYERROLE_ABILITY; 
DROP TABLE IF EXISTS YUMEDANT_ABILITY;   
DROP TABLE IF EXISTS PARAMETER_VALUE;
DROP TABLE IF EXISTS PARAMETER_EFFECT;

-- Després les entitats específiques (Fills)
DROP TABLE IF EXISTS PLAYER;
DROP TABLE IF EXISTS ENEMY;
DROP TABLE IF EXISTS ACTION_EFFECT;
DROP TABLE IF EXISTS ABILITY;
DROP TABLE IF EXISTS ITEM;

-- Finalment les entitats base i catàlegs (Pares)
DROP TABLE IF EXISTS YUMEDANT;
DROP TABLE IF EXISTS PARAMETER_TYPES;
DROP TABLE IF EXISTS FLOOR;
DROP TABLE IF EXISTS PLAYERROLE;
DROP TABLE IF EXISTS ACT;
DROP TABLE IF EXISTS EFFECT;


-- ==========================================
-- 2. CREACIÓ DE TAULES (Pares primer)
-- ==========================================

-- Taules sense Referències
CREATE TABLE EFFECT (
    ef_id int NOT NULL AUTO_INCREMENT,
    ef_type varchar(255),
    PRIMARY KEY(ef_id)
);


CREATE TABLE ACT (
    ac_id int NOT NULL AUTO_INCREMENT,
    ac_name varchar(255) NOT NULL,
    ac_desc varchar(255) NOT NULL,
    ac_img BLOB,
    ac_type char(1), -- I = Item, A = Ability
    PRIMARY KEY (ac_id)
);


CREATE TABLE PLAYERROLE (
    pr_id int NOT NULL AUTO_INCREMENT,
    pr_name varchar(50),
    pr_desc varchar(255),
    PRIMARY KEY (pr_id)
);


CREATE TABLE FLOOR (
    fl_id int NOT NULL AUTO_INCREMENT,
    fl_num int NOT NULL,
    fl_name varchar(20),
    fl_background BLOB,
    PRIMARY KEY (fl_id)
);


-- Taula mapejada pel DbContext amb valors dins d'un ENUM
-- Valors de PARAMETER_TYPES:
-- Stat, MaxRange, MinRange, Mangitude, ValueType, Target, Duration, Effect
CREATE TABLE PARAMETER_TYPES ( 
    pt_id int NOT NULL AUTO_INCREMENT,
    pt_name varchar(20),

    PRIMARY KEY(pt_id)
);


CREATE TABLE YUMEDANT (
    yu_id int NOT NULL AUTO_INCREMENT,
    yu_name varchar(255) NOT NULL,
    yu_desc varchar(255),
	yu_level int NOT NULL DEFAULT 1,
    att_hp int NOT NULL DEFAULT 1,  
    att_mp int NOT NULL DEFAULT 1,   
    att_atk int NOT NULL DEFAULT 1,  
    att_def int NOT NULL DEFAULT 1,
	PRIMARY KEY (yu_id)
);


-- ==========================================
-- 3. TAULES AMB DEPENDÈNCIES (Fills)
-- ==========================================

CREATE TABLE ITEM(
    it_ac_id int NOT NULL,
    PRIMARY KEY (it_ac_id),
    FOREIGN KEY (it_ac_id) REFERENCES ACT(ac_id)
);


CREATE TABLE ABILITY(
    ab_ac_id int NOT NULL,
    ab_cost int NOT NULL,
    PRIMARY KEY (ab_ac_id),
    FOREIGN KEY (ab_ac_id) REFERENCES ACT(ac_id)
);


CREATE TABLE ACTION_EFFECT (
    ae_id int NOT NULL AUTO_INCREMENT,
    ae_ef_id int NOT NULL,
    ae_ac_id int NOT NULL,
    PRIMARY KEY(ae_id),
    CONSTRAINT FK_ACTIONEFFECT_EFFECT FOREIGN KEY(ae_ef_id) REFERENCES EFFECT(ef_id),
    CONSTRAINT FK_ACTIONEFFECT_ACT FOREIGN KEY(ae_ac_id) REFERENCES ACT(ac_id)
);


CREATE TABLE PARAMETER_EFFECT (
    pe_pt_id int NOT NULL,
    pe_ef_id int NOT NULL,
    PRIMARY KEY(pe_pt_id, pe_ef_id),
    FOREIGN KEY(pe_pt_id) REFERENCES PARAMETER_TYPES(pt_id),
    FOREIGN KEY(pe_ef_id) REFERENCES EFFECT(ef_id)
);


CREATE TABLE PARAMETER_VALUE (
    pv_ae_id int NOT NULL,
    pv_pe_pt_id int NOT NULL,
    pv_pe_ef_id int NOT NULL,
    pv_string_value varchar(10), -- Pot ser null no ser utilitzat
    pv_num_value int, -- Pot ser null, no ser utilitzat
    PRIMARY KEY(pv_ae_id, pv_pe_pt_id),
    FOREIGN KEY (pv_ae_id) REFERENCES ACTION_EFFECT (ae_id),
    FOREIGN KEY(pv_pe_pt_id, pv_pe_ef_id) REFERENCES PARAMETER_EFFECT(pe_pt_id, pe_ef_id)
);


CREATE TABLE ENEMY (
    en_yu_id int NOT NULL,
    en_given_exp int NOT NULL,
    PRIMARY KEY (en_yu_id),
    FOREIGN KEY (en_yu_id) REFERENCES YUMEDANT(yu_id)
);


CREATE TABLE PLAYER (
    pl_yu_id int NOT NULL,
    pl_exp int NOT NULL,
    pl_role_id int,
    PRIMARY KEY (pl_yu_id),
    FOREIGN KEY (pl_yu_id) REFERENCES YUMEDANT(yu_id),
    FOREIGN KEY (pl_role_id) REFERENCES PLAYERROLE(pr_id)
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
    FOREIGN KEY (ra_ability_id) REFERENCES ABILITY(ab_ac_id)
);


CREATE TABLE ENEMY_DROPS (
    ed_item_id int NOT NULL,
    ed_enemy_id int NOT NULL,
    ed_chance float NOT NULL DEFAULT 0.05, -- És un 5%
    PRIMARY KEY (ed_item_id, ed_enemy_id),
    FOREIGN KEY (ed_item_id) REFERENCES ITEM(it_ac_id),
    FOREIGN KEY (ed_enemy_id) REFERENCES ENEMY(en_yu_id)
);


CREATE TABLE ENEMY_FLOORS (
    enfl_enemy_id int NOT NULL,
    enfl_floor_id int NOT NULL,
    enfl_quantity int NOT NULL,
    PRIMARY KEY (enfl_enemy_id, enfl_floor_id),
    FOREIGN KEY (enfl_enemy_id) REFERENCES ENEMY(en_yu_id),
    FOREIGN KEY (enfl_floor_id) REFERENCES FLOOR(fl_id)
);


-- ==========================================
-- 5. ÍNDEXS
-- ==========================================

ALTER TABLE ACT ADD INDEX idx_action_name (ac_name);
ALTER TABLE YUMEDANT ADD INDEX idx_yumedant_name (yu_name);