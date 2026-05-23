-- Script de Dades Demo de la Base de Dades Yumeworld
-- INSERT INTO table_name (column1, column2, column3, ...)
-- VALUES (value1, value2, value3, ...);
-- ===================================
-- Taules bàsiques
-- ===================================
-- pr_hp/mp/atk/def: bonus de rol al level-up (> 0 = afavorit per aquest rol).
-- A cada pujada de nivell, cada stat amb bonus > 0 rep +1 o +2 addicional
-- sobre el boost base aleatori que tots els personatges ja reben.
INSERT INTO PLAYERROLE (pr_id, pr_name, pr_desc, pr_hp, pr_mp, pr_atk, pr_def)
VALUES
    (1, 'Warrior',  'Especialista en combat cos a cos.',                                    0, 0, 1, 1),
    (2, 'Mage',     'Usuari de màgia elemental.',                                           0, 1, 1, 0), -- Pingu
    (3, 'Bard',     'Utilitza la música per ajudar els seus aliats.',                       0, 1, 0, 0), -- Michi
    (4, 'Tank',     'Protegeix els seus aliats amb la seva alta DEF.',                      1, 0, 0, 1), -- Nus
    (5, 'Healer',   'Especialista en màgia curativa.',                                      1, 1, 0, 0), -- Pipa
    (6, 'Assassin', 'Mestre de les ombres, enfocat en causar molt dany a un sol objectiu.', 0, 0, 1, 0); -- Lia

-- ===================================
-- Biomes (els mateixos que utilitza el servidor a MapGenerationService)
-- L'ordre (bi_order) marca com es distribueixen pels pisos:
-- 0 = primer pis (més suau), 4 = últim pis (més intens)
-- ===================================
INSERT INTO BIOME (bi_id, bi_name, bi_order, bi_background)
VALUES
    (1, 'numbness',   0, 'bg1'),
    (2, 'lightSleep', 1, 'b2'),
    (3, 'deepSleep',  2, 'bg3'),
    (4, 'remSleep',   3, 'bg4'),
    (5, 'nightmare',  4, 'bg5');


INSERT INTO EFFECT (ef_id, ef_name)
VALUES
    (1, 'Simple Damage'),
    (2, 'Cura percentual'),
    (3, 'Cura absoluta'),
    (4, 'Estat alterat'),
    (5, 'Modificacio atributs'); -- buff / debuff

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

INSERT INTO YUMEDANT (yu_id, yu_name, yu_desc, yu_level, yu_hp, yu_mp, yu_atk, yu_def, yu_image)
VALUES
    (1, 'Pingu',     'Et mossega amb molt d''amor.',               1, 11, 15, 10,  5, 'pingu'),
    (2, 'Michi',     'Cega i altament desorientada.',              1, 17, 10, 15,  6 , 'michi'),
    (3, 'Nus',       'Esperit protector.',                          1, 20,  5,  8, 15, 'nus'),
    (4, 'Pipa',      'S''espanta si respires massa fort.',          1,  8, 10, 10, 10, 'pipa'),
    (5, 'Lia',       'Curiosa i juganera, sempre busca aventures.', 1, 12, 12, 11,  7, 'lia'),
    (6, 'Doguh', 'Borda fluixet i ataca quan menys t''ho esperes.', 1, 10,  0, 12,  4, 'scareddog'),
    (7, 'Fang',      'És de fang... i menja pedres',      1, 18,  0, 18,  6, 'fang');

-- ===================================
-- Players: Pingu, Michi, Nus, Pipa i Lia
-- Distribució final de rols.
-- ===================================
INSERT INTO PLAYER (pl_yumedant_id, pl_exp, pl_role_id)
VALUES
    (1, 0, 2),  -- Pingu -> Mage
    (2, 0, 3),  -- Michi -> Bard
    (3, 0, 4),  -- Nus   -> Tank (esperit protector)
    (4, 0, 5),  -- Pipa  -> Healer
    (5, 0, 6);  -- Lia   -> Assassin

-- ===================================
-- Enemics: ScaredDog i Fang
-- ===================================
INSERT INTO ENEMY (en_yumedant_id, en_given_exp, en_given_coins)
VALUES
    (6,  50, 50),  -- ScaredDog
    (7, 50, 30);  -- Fang

-- ===================================
-- Enemics que apareixen a cada bioma (substitueix l'antiga ENEMY_FLOORS)
-- ===================================
INSERT INTO ENEMY_BIOMES (enbi_enemy_id, enbi_biome_id, enbi_quantity)
VALUES
    (6, 1, 1),  -- ScaredDog a numbness
    (6, 2, 2),  -- ScaredDog a lightSleep
    (6, 3, 1),  -- ScaredDog a deepSleep
    (7, 3, 1),  -- Fang a deepSleep
    (7, 4, 2),  -- Fang a remSleep
    (7, 5, 3);  -- Fang al nightmare (boss)

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

