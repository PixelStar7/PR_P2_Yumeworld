-- =============================================================================
-- Script de Dades Demo de la Base de Dades Yumeworld
-- (YumeworldData.sql + YumeworldNewAct.sql fusionats)
--
-- Ordre d'inserció respectant les dependències de claus foranes:
--   1. Taules independents: PLAYERROLE, BIOME, EFFECT, PARAMETER_TYPES, ALTERED_STATE
--   2. Relació paràmetre-efecte: PARAMETER_EFFECT
--   3. Accions i subtipus: ACT → ABILITY, ITEM
--   4. Personatges: YUMEDANT → PLAYER, ENEMY
--   5. Distribució de mapa: ENEMY_BIOMES
--   6. Associació rol-habilitat: PLAYERROLE_ABILITY
--   7. Efectes d'acció: ACTION_EFFECT → PARAMETER_VALUE
--   8. Drops d'enemic: ENEMY_DROPS
-- =============================================================================


-- =============================================================================
-- 1. TAULES INDEPENDENTS
-- =============================================================================

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

-- Biomes (l'ordre (bi_order) marca com es distribueixen pels pisos)
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


-- =============================================================================
-- 2. PARAMETER_EFFECT — quins paràmetres necessita cada efecte
-- =============================================================================

INSERT INTO PARAMETER_EFFECT (pe_parametertype_id, pe_effect_id)
VALUES
    -- Efecte 1: Simple Damage
    (4, 1), -- Magnitude
    (5, 1), -- ValueType
    (6, 1), -- Target
    (9, 1), -- RefAttr
    (2, 1), -- MaxRange (variància per a Ganivetada Furtiva i similars)
    (3, 1), -- MinRange

    -- Efecte 2: Cura percentual
    (4, 2), -- Magnitude
    (5, 2), -- ValueType
    (6, 2), -- Target
    (9, 2), -- RefAttr

    -- Efecte 3: Cura absoluta
    (4, 3), -- Magnitude
    (5, 3), -- ValueType
    (6, 3), -- Target

    -- Efecte 4: Estat alterat
    (6, 4), -- Target
    (7, 4), -- Duration
    (8, 4), -- AlteredState

    -- Efecte 5: Modificació d'atributs (buff / debuff)
    (4, 5), -- Magnitude
    (5, 5), -- ValueType
    (6, 5), -- Target
    (7, 5), -- Duration
    (9, 5); -- RefAttr


-- =============================================================================
-- 3. ACCIONS I SUBTIPUS
-- =============================================================================

INSERT INTO ACT (ac_id, ac_name, ac_desc, ac_type)
VALUES
    (1,  'Basic atack',        'Cop simple amb l''arma.',                                         'A'),
    (2,  'Basic heal',         'Cura un 33% de l''HP màxim.',                                    'A'),
    (3,  'Poció curativa',     'Recupera 10 punts de vida.',                                     'I'),
    (4,  'Enverinar',          'Enverina l''enemic durant 3 torns.',                             'A'),
    (5,  'Cop Demolidor',      'Atac físic pesat cap a un sol objectiu.',                        'A'),
    (6,  'Flama Expansiva',    'Causa dany a tots els enemics presents.',                        'A'),
    (7,  'Melodia Letàrgica',  'Adorm a tots els enemics.',                                      'A'),
    (8,  'Mur de Ferro',       'Augmenta un 50% la defensa pròpia.',                             'A'),
    (9,  'Pols de Vida',       'Restaura salut a tot l''equip.',                                 'A'),
    (10, 'Ganivetada Furtiva', 'Un atac amb alta variabilitat de dany.',                         'A'),
    (11, 'Extracte de Força',  'Consumible que augmenta l''ATK un 30% durant 2 torns.',         'I');

-- El trigger trg_after_act_insert ja crea les files a ABILITY i ITEM amb valors
-- per defecte quan s'insereix l'ACT. Aquí sobreescrivim els costos de MP reals.
INSERT INTO ABILITY (ab_action_id, ab_cost)
VALUES
    (1,  0),   -- Basic Atack (gratuït)
    (2,  5),   -- Basic Heal
    (4,  4),   -- Enverinar
    (5,  12),  -- Cop Demolidor   (Warrior)
    (6,  20),  -- Flama Expansiva (Mage)
    (7,  18),  -- Melodia Letàrgica (Bard)
    (8,  15),  -- Mur de Ferro   (Tank)
    (9,  25),  -- Pols de Vida   (Healer)
    (10, 15)   -- Ganivetada Furtiva (Assassin)
ON DUPLICATE KEY UPDATE ab_cost = VALUES(ab_cost);

-- ITEM no té columnes addicionals a actualitzar; el trigger ja ha creat les files.
INSERT IGNORE INTO ITEM (it_action_id)
VALUES
    (3),   -- Poció curativa
    (11);  -- Extracte de Força


-- =============================================================================
-- 4. PERSONATGES: YUMEDANT → PLAYER i ENEMY
-- =============================================================================

INSERT INTO YUMEDANT (yu_id, yu_name, yu_desc, yu_level, yu_hp, yu_mp, yu_atk, yu_def, yu_image)
VALUES
    (1, 'Pingu',   'Et mossega amb molt d''amor.',                1, 11, 15, 10,  5, 'pingu'),
    (2, 'Michi',   'Cega i altament desorientada.',              1, 17, 10, 15,  6, 'michi'),
    (3, 'Nus',     'Esperit protector.',                          1, 20,  5,  8, 15, 'nus'),
    (4, 'Pipa',    'S''espanta si respires massa fort.',          1,  8, 10, 10, 10, 'pipa'),
    (5, 'Lia',     'Curiosa i juganera, sempre busca aventures.', 1, 12, 12, 11,  7, 'lia'),
    (6, 'Doguh',   'Borda fluixet i ataca quan menys t''ho esperes.', 1, 10, 0, 12,  4, 'scareddog'),
    (7, 'Fang',    'És de fang... i menja pedres.',              1, 18,  0, 18,  6, 'fang');

-- Players: Pingu, Michi, Nus, Pipa i Lia
INSERT INTO PLAYER (pl_yumedant_id, pl_exp, pl_role_id)
VALUES
    (1, 0, 2),  -- Pingu -> Mage
    (2, 0, 3),  -- Michi -> Bard
    (3, 0, 4),  -- Nus   -> Tank
    (4, 0, 5),  -- Pipa  -> Healer
    (5, 0, 6);  -- Lia   -> Assassin

INSERT INTO ENEMY (en_yumedant_id, en_given_exp, en_given_coins)
VALUES
    (6, 50, 50),  -- ScaredDog
    (7, 50, 30);  -- Fang


-- =============================================================================
-- 5. DISTRIBUCIÓ DE MAPA
-- =============================================================================

INSERT INTO ENEMY_BIOMES (enbi_enemy_id, enbi_biome_id, enbi_quantity)
VALUES
    (6, 1, 1),  -- ScaredDog a numbness
    (6, 2, 2),  -- ScaredDog a lightSleep
    (6, 3, 1),  -- ScaredDog a deepSleep
    (7, 3, 1),  -- Fang a deepSleep
    (7, 4, 2),  -- Fang a remSleep
    (7, 5, 3);  -- Fang al nightmare (boss)


-- =============================================================================
-- 6. ASSOCIACIÓ ROL → HABILITAT
-- =============================================================================

INSERT INTO PLAYERROLE_ABILITY (ra_playerrole_id, ra_ability_id)
VALUES
    (1, 5),   -- Warrior   → Cop Demolidor
    (2, 6),   -- Mage      → Flama Expansiva
    (3, 7),   -- Bard      → Melodia Letàrgica
    (4, 8),   -- Tank      → Mur de Ferro
    (5, 9),   -- Healer    → Pols de Vida
    (6, 10);  -- Assassin  → Ganivetada Furtiva


-- =============================================================================
-- 7. EFECTES D'ACCIÓ I VALORS DE PARÀMETRES
-- =============================================================================

INSERT INTO ACTION_EFFECT (ae_id, ae_action_id, ae_effect_id)
VALUES
    (1,  1,  1),  -- Basic Atack       → Simple Damage
    (2,  2,  2),  -- Basic Heal        → Cura percentual
    (3,  3,  3),  -- Poció curativa    → Cura absoluta
    (4,  4,  4),  -- Enverinar         → Estat alterat
    (5,  5,  1),  -- Cop Demolidor     → Simple Damage
    (6,  6,  1),  -- Flama Expansiva   → Simple Damage
    (7,  7,  4),  -- Melodia Letàrgica → Estat alterat
    (8,  8,  5),  -- Mur de Ferro      → Modificació atributs
    (9,  9,  2),  -- Pols de Vida      → Cura percentual
    (10, 10, 1),  -- Ganivetada Furtiva → Simple Damage
    (11, 11, 5);  -- Extracte de Força  → Modificació atributs

INSERT INTO PARAMETER_VALUE (pv_actioneffect_id, pv_parametertype_id, pv_effect_id, pv_string_value, pv_num_value, pv_altered_state_value)
VALUES
    -- ae_id 1: Basic Atack (Simple Damage)
    (1, 4, 1, NULL,  1,    NULL),  -- Magnitude: 1
    (1, 5, 1, 'A',   NULL, NULL),  -- ValueType: Absolut
    (1, 6, 1, 'E',   NULL, NULL),  -- Target: ENEMY
    (1, 9, 1, 'ATK', NULL, NULL),  -- RefAttr: ATK

    -- ae_id 2: Basic Heal (Cura percentual)
    (2, 4, 2, NULL,  33,   NULL),  -- Magnitude: 33%
    (2, 5, 2, 'P',   NULL, NULL),  -- ValueType: Percentual
    (2, 6, 2, 'SA',  NULL, NULL),  -- Target: Self + Allies
    (2, 9, 2, 'HP',  NULL, NULL),  -- RefAttr: HP

    -- ae_id 3: Poció curativa (Cura absoluta)
    (3, 4, 3, NULL,  10,   NULL),  -- Magnitude: 10 HP
    (3, 5, 3, 'A',   NULL, NULL),  -- ValueType: Absolut
    (3, 6, 3, 'S',   NULL, NULL),  -- Target: Self

    -- ae_id 4: Enverinar (Estat alterat)
    (4, 6, 4, 'E',   NULL, NULL),  -- Target: ENEMY
    (4, 7, 4, NULL,  3,    NULL),  -- Duration: 3 torns
    (4, 8, 4, NULL,  NULL, 2),     -- AlteredState: Poisoned (2)

    -- ae_id 5: Cop Demolidor (Simple Damage)
    (5, 4, 1, NULL,  2,    NULL),  -- Magnitude: 2
    (5, 5, 1, 'A',   NULL, NULL),  -- ValueType: Absolut
    (5, 6, 1, 'E',   NULL, NULL),  -- Target: ENEMY
    (5, 9, 1, 'ATK', NULL, NULL),  -- RefAttr: ATK

    -- ae_id 6: Flama Expansiva (Simple Damage a tots els enemics)
    (6, 4, 1, NULL,  1,    NULL),  -- Magnitude: 1
    (6, 5, 1, 'A',   NULL, NULL),  -- ValueType: Absolut
    (6, 6, 1, 'T',   NULL, NULL),  -- Target: ALL_ENEMIES
    (6, 9, 1, 'ATK', NULL, NULL),  -- RefAttr: ATK

    -- ae_id 7: Melodia Letàrgica (Estat alterat: Asleep a tots els enemics)
    (7, 6, 4, 'T',   NULL, NULL),  -- Target: ALL_ENEMIES
    (7, 7, 4, NULL,  2,    NULL),  -- Duration: 2 torns
    (7, 8, 4, NULL,  NULL, 4),     -- AlteredState: Asleep (4)

    -- ae_id 8: Mur de Ferro (Buff DEF +50% durant 3 torns)
    (8, 4, 5, NULL,  50,   NULL),  -- Magnitude: 50
    (8, 5, 5, 'P',   NULL, NULL),  -- ValueType: Percentual
    (8, 6, 5, 'S',   NULL, NULL),  -- Target: Self
    (8, 7, 5, NULL,  3,    NULL),  -- Duration: 3 torns
    (8, 9, 5, 'DEF', NULL, NULL),  -- RefAttr: DEF

    -- ae_id 9: Pols de Vida (Cura percentual a tot l'equip)
    (9, 4, 2, NULL,  25,   NULL),  -- Magnitude: 25%
    (9, 5, 2, 'P',   NULL, NULL),  -- ValueType: Percentual
    (9, 6, 2, 'W',   NULL, NULL),  -- Target: TEAM
    (9, 9, 2, 'HP',  NULL, NULL),  -- RefAttr: HP

    -- ae_id 10: Ganivetada Furtiva (Simple Damage amb variància)
    (10, 5, 1, 'A',   NULL, NULL),  -- ValueType: Absolut
    (10, 6, 1, 'E',   NULL, NULL),  -- Target: ENEMY
    (10, 9, 1, 'ATK', NULL, NULL),  -- RefAttr: ATK
    (10, 3, 1, NULL,  150,  NULL),  -- MinRange: 150%
    (10, 2, 1, NULL,  250,  NULL),  -- MaxRange: 250%

    -- ae_id 11: Extracte de Força (Buff ATK +30% durant 2 torns)
    (11, 4, 5, NULL,  30,   NULL),  -- Magnitude: 30
    (11, 5, 5, 'P',   NULL, NULL),  -- ValueType: Percentual
    (11, 6, 5, 'A',   NULL, NULL),  -- Target: ALLY
    (11, 7, 5, NULL,  2,    NULL),  -- Duration: 2 torns
    (11, 9, 5, 'ATK', NULL, NULL);  -- RefAttr: ATK


-- =============================================================================
-- 8. DROPS D'ENEMIC
-- =============================================================================

INSERT INTO ENEMY_DROPS (ed_item_id, ed_enemy_id, ed_chance)
VALUES
    (3,  6, 0.05),  -- ScaredDog: 5%  Poció curativa
    (11, 6, 0.02),  -- ScaredDog: 2%  Extracte de Força
    (3,  7, 0.15),  -- Fang:      15% Poció curativa
    (11, 7, 0.10);  -- Fang:      10% Extracte de Força
