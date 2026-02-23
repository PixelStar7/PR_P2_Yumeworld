-- Script de Dades Demo de la Base de Dades Yumeworld
-- INSERT INTO table_name (column1, column2, column3, ...)
-- VALUES (value1, value2, value3, ...);
-- ===================================
-- Taules bàsiques
-- ===================================
INSERT INTO PLAYERROLE (pr_id, pr_name, pr_desc)
VALUES 
    (1, 'Warrior', 'Especialista en combat cos a cos.'),
    (2, 'Mage', 'Usuari de màgia elemental.'),
    (3, 'Bard', 'Utilitza la música per ajudar els seus aliats.'),
    (4, 'Tank', 'Protegeix els seus aliats amb la seva alta DEF.'),
    (5, 'Healer', 'Especialista en màgia curativa.');

INSERT INTO BIOME (bi_id)
VALUES (1),(2),(3),(4);


INSERT INTO FLOOR (fl_id, fl_num, fl_name, fl_biome_id)
VALUES
    (1,1,'Adormecimiento 1', 1),
    (2,2,'Adormecimiento 2', 1);

INSERT INTO EFFECT (ef_id, ef_name)
VALUES 
    (1, 'Simple Damage'), 
    (2, 'Cura percentual'),
    (3, 'Cura absoluta'),
    (4, 'Estat alterat'), 
    (5, 'Modificacio atributs');


     
-- ===================================
-- Taules bàsiques (Actualitzada amb Lògica Dinàmica)
-- ===================================
INSERT INTO PARAMETER_TYPES (pt_id, pt_name, pt_type, pt_num_min, pt_num_max, pt_value_list)
VALUES
    (2, 'MaxRange',     'N', 0, 99,   NULL),
    (3, 'MinRange',     'N', 0, 99,   NULL),
    (4, 'Magnitude',    'N', 0, 9999, NULL),
    (5, 'ValueType',    'L', NULL, NULL, 'A,P'),
    (6, 'Target',       'L', NULL, NULL, 'S,A,W,E,T'),
    (7, 'Duration',     'N', 0, 99,   NULL),
    (8, 'AlteredState', 'L', NULL, NULL, NULL), -- Es gestiona via GetValorsAlteredStated
    (9, 'RefAttr',      'L', NULL, NULL, 'HP,MP,ATK,DEF');

INSERT INTO ALTERED_STATE (as_id, as_name)
VALUES
    (1, 'Downed'),
    (2, 'Poisoned'),
    (3, 'Paralyzed'),
    (4, 'Asleep'),
    (5, 'Stunned'),
    (6, 'Vulnerable');


-- ===================================
-- Taules amb herències
-- ===================================
INSERT INTO ACT (ac_id, ac_name, ac_desc, ac_type)
VALUES
    (1, 'Basic atack', 'Cop simple amb l''arma.', 'A'),
    (2, 'Basic heal', 'Cura un 33% de l''HP màxim.', 'A'),
    (3, 'Poció curativa', 'Recupera 10 punts de vida.', 'I'),
    (4, 'Enverinar', 'Enverina l''enemic durant 3 torns', 'A');

INSERT INTO ITEM (it_action_id) 
VALUES (3);

INSERT INTO ABILITY (ab_action_id, ab_cost) 
VALUES (1, 0), (2,5), (4, 4);

INSERT INTO YUMEDANT (yu_id, yu_name, yu_desc, yu_level, yu_hp, yu_mp, yu_atk, yu_def)
VALUES 
    (1, 'Pingu', 'Et mossega amb molt d''amor.', 1, 11, 15, 10, 5), 
    (2, 'Michi', 'Cega i altament desorientada.', 1, 17, 10, 15, 6),
    (3, 'Nus', 'Esperit protector.', 1, 20, 5, 8, 15),
    (4, 'Pipa', 'S''espanta si respires massa fort.', 1, 8, 10, 10, 10),
    (5, 'Sau', 'Persevera en la seva aventura', 1, 20, 10, 10, 10);

INSERT INTO ENEMY (en_yumedant_id, en_given_exp)
VALUES 
    (1, 50), -- Pingu
    (2, 50),  -- Michi
    (3, 100),  -- Nus
    (4, 50); -- Pipa

INSERT INTO PLAYER (pl_yumedant_id, pl_exp, pl_role_id)
VALUES 
    (5, 0, 2);  -- Player Sau

-- ===================================
-- Taules que relacionen altres taules
-- ===================================
INSERT INTO PARAMETER_EFFECT (pe_parametertype_id, pe_effect_id)
VALUES
    -- Ens diu quines coses necessita cada efecte.
    -- El "Mal simple" (1) requereix una Magnitud (4) i un Target (6)
    (4, 1), -- Magnitud
    (5, 1), -- ValueType(Absolute)
    (6, 1), -- Target
    (9, 1), -- RefStat

    -- La "CuracióPercentual" (2) requereix una Magnitud (4) i un Target (6)
    (4, 2),
    (5, 2),
    (6, 2),
    (9, 2),

    -- Cura absoluta
    (4, 3),
    (5, 3),
    (6, 3),

    -- Modificacio d'estat alterat
    (6, 4),
    (7, 4),
    (8, 4);

INSERT INTO ACTION_EFFECT (ae_id, ae_action_id, ae_effect_id)
VALUES
    (1, 1, 1), -- Basic Atk fa Mal simple
    (2, 2, 2), -- Basic Heal fa curació percentual
    (3, 3, 3), -- Poció fa curació absoluta
    (4, 4, 4); -- Enverinar aplica un estat alterat

INSERT INTO PARAMETER_VALUE (pv_actioneffect_id, pv_parametertype_id, pv_effect_id, pv_string_value, pv_num_value, pv_altered_state_value)
VALUES
    -- Configurar Basic Atk
    (1, 4, 1, NULL, 1, NULL),
    (1, 6, 1, 'E', NULL, NULL),
    (1, 9, 1, 'ATK', NULL, NULL),
    (1, 5, 1, 'A', NULL, NULL),

    -- Configurar Basic Heal
    (2, 4, 2, NULL, 33, NULL),
    (2, 5, 2, 'P', NULL, NULL), -- Percentual
    (2, 6, 2, 'SA', NULL, NULL), -- Self and Allies
    (2, 9, 2, 'HP',NULL, NULL),

    -- Configurar Poció
    (3, 4, 3, NULL, 10, NULL),
    (3, 5, 3, 'A', NULL, NULL), -- Absolute
    (3, 6, 3, 'S', NULL, NULL), -- Self

    -- Configuerar Enverinament
    (4, 6, 4, 'E', NULL, NULL),
    (4, 7, 4, NULL, 3, NULL),
    (4, 8, 4, NULL, NULL, 2); -- Enverinament id=2
