-- Es defineixen els paràmetres necessaris per processar un buff/debuff (Efecte 5)
INSERT INTO PARAMETER_EFFECT (pe_parametertype_id, pe_effect_id)
VALUES
    (4, 5), -- Magnitude (Ex: 50)
    (5, 5), -- ValueType (P = Percentual, A = Absolut)
    (6, 5), -- Target (S(self), W(we), A(ally), E(enemy), T(them))
    (7, 5), -- Duration (Torns actiu)
    (9, 5); -- RefAttr (Atribut afectat: HP, MP, ATK, DEF)

-- ===================================================================================

-- 1. Definició de l'acció
INSERT INTO ACT (ac_id, ac_name, ac_desc, ac_type)
VALUES (5, 'Cop Demolidor', 'Atac físic pesat cap a un sol objectiu.', 'A');

-- 2. Definició com a habilitat (pr_id=1 correspon al Warrior)
INSERT INTO ABILITY (ab_action_id, ab_cost) VALUES (5, 12);
INSERT INTO PLAYERROLE_ABILITY (ra_playerrole_id, ra_ability_id) VALUES (1, 5);

-- 3. Vinculació amb l'efecte i paràmetres
INSERT INTO ACTION_EFFECT (ae_id, ae_action_id, ae_effect_id) VALUES (5, 5, 1);
INSERT INTO PARAMETER_VALUE (pv_actioneffect_id, pv_parametertype_id, pv_effect_id, pv_string_value, pv_num_value, pv_altered_state_value)
VALUES
    (5, 4, 1, NULL, 2, NULL),     -- Magnitude: 2
    (5, 6, 1, 'E', NULL, NULL),   -- Target: ENEMY
    (5, 9, 1, 'ATK', NULL, NULL), -- Atribut: ATK
    (5, 5, 1, 'A', NULL, NULL);   -- ValueType: Absolut

-- ===================================================================================

INSERT INTO ACT (ac_id, ac_name, ac_desc, ac_type)
VALUES (6, 'Flama Expansiva', 'Causa dany a tots els enemics presents.', 'A');

INSERT INTO ABILITY (ab_action_id, ab_cost) VALUES (6, 20);
INSERT INTO PLAYERROLE_ABILITY (ra_playerrole_id, ra_ability_id) VALUES (2, 6);

INSERT INTO ACTION_EFFECT (ae_id, ae_action_id, ae_effect_id) VALUES (6, 6, 1);
INSERT INTO PARAMETER_VALUE (pv_actioneffect_id, pv_parametertype_id, pv_effect_id, pv_string_value, pv_num_value, pv_altered_state_value)
VALUES
    (6, 4, 1, NULL, 1, NULL),     -- Magnitude: 1
    (6, 6, 1, 'T', NULL, NULL),   -- Target: ALL_ENEMIES
    (6, 9, 1, 'ATK', NULL, NULL), -- Atribut: ATK
    (6, 5, 1, 'A', NULL, NULL);   -- ValueType: Absolut

-- ===================================================================================

INSERT INTO ACT (ac_id, ac_name, ac_desc, ac_type)
VALUES (7, 'Melodia Letàrgica', 'Adorm a tots els enemics.', 'A');

INSERT INTO ABILITY (ab_action_id, ab_cost) VALUES (7, 18);
INSERT INTO PLAYERROLE_ABILITY (ra_playerrole_id, ra_ability_id) VALUES (3, 7);

INSERT INTO ACTION_EFFECT (ae_id, ae_action_id, ae_effect_id) VALUES (7, 7, 4);
INSERT INTO PARAMETER_VALUE (pv_actioneffect_id, pv_parametertype_id, pv_effect_id, pv_string_value, pv_num_value, pv_altered_state_value)
VALUES
    (7, 6, 4, 'T', NULL, NULL), -- Target: ALL_ENEMIES
    (7, 7, 4, NULL, 2, NULL),   -- Duration: 2
    (7, 8, 4, NULL, NULL, 4);   -- AlteredState: 4 (Asleep)

-- ===================================================================================

INSERT INTO ACT (ac_id, ac_name, ac_desc, ac_type)
VALUES (8, 'Mur de Ferro', 'Augmenta un 50% la defensa pròpia.', 'A');

INSERT INTO ABILITY (ab_action_id, ab_cost) VALUES (8, 15);
INSERT INTO PLAYERROLE_ABILITY (ra_playerrole_id, ra_ability_id) VALUES (4, 8);

INSERT INTO ACTION_EFFECT (ae_id, ae_action_id, ae_effect_id) VALUES (8, 8, 5);
INSERT INTO PARAMETER_VALUE (pv_actioneffect_id, pv_parametertype_id, pv_effect_id, pv_string_value, pv_num_value, pv_altered_state_value)
VALUES
    (8, 4, 5, NULL, 50, NULL),    -- Magnitude: 50
    (8, 5, 5, 'P', NULL, NULL),   -- ValueType: Percentual
    (8, 6, 5, 'S', NULL, NULL),   -- Target: SELF
    (8, 7, 5, NULL, 3, NULL),     -- Duration: 3 torns
    (8, 9, 5, 'DEF', NULL, NULL); -- Atribut: DEF

-- ===================================================================================

INSERT INTO ACT (ac_id, ac_name, ac_desc, ac_type)
VALUES (9, 'Pols de Vida', 'Restaura salut a tot l''equip.', 'A');

INSERT INTO ABILITY (ab_action_id, ab_cost) VALUES (9, 25);
INSERT INTO PLAYERROLE_ABILITY (ra_playerrole_id, ra_ability_id) VALUES (5, 9);

INSERT INTO ACTION_EFFECT (ae_id, ae_action_id, ae_effect_id) VALUES (9, 9, 2);
INSERT INTO PARAMETER_VALUE (pv_actioneffect_id, pv_parametertype_id, pv_effect_id, pv_string_value, pv_num_value, pv_altered_state_value)
VALUES
    (9, 4, 2, NULL, 25, NULL),   -- Magnitude: 25%
    (9, 5, 2, 'P', NULL, NULL),  -- ValueType: Percentual
    (9, 6, 2, 'W', NULL, NULL),  -- Target: TEAM
    (9, 9, 2, 'HP', NULL, NULL); -- Atribut: HP

-- ===================================================================================

-- 1. Definició de l'acció
INSERT INTO ACT (ac_id, ac_name, ac_desc, ac_type)
VALUES (10, 'Ganivetada Furtiva', 'Un atac amb alta variabilitat de dany.', 'A');

-- 2. Registre i vinculació al rol Assassin (pr_id = 6)
INSERT INTO ABILITY (ab_action_id, ab_cost) VALUES (10, 15);
INSERT INTO PLAYERROLE_ABILITY (ra_playerrole_id, ra_ability_id) VALUES (6, 10);

-- 3. Vinculació a l'efecte de dany simple (1)
INSERT INTO ACTION_EFFECT (ae_id, ae_action_id, ae_effect_id) VALUES (10, 10, 1);

-- 4. Definició dels paràmetres, incloent el rang
INSERT INTO PARAMETER_VALUE (pv_actioneffect_id, pv_parametertype_id, pv_effect_id, pv_string_value, pv_num_value, pv_altered_state_value)
VALUES
    (10, 6, 1, 'E', NULL, NULL),   -- Target: ENEMY (Un sol objectiu)
    (10, 9, 1, 'ATK', NULL, NULL), -- RefAttr: Es basa en l'ATK
    (10, 5, 1, 'A', NULL, NULL),   -- ValueType: Absolut
    (10, 3, 1, NULL, 150, NULL),   -- MinRange: 150 (ex: 150% de l'ATK)
    (10, 2, 1, NULL, 250, NULL);   -- MaxRange: 250 (ex: fins a un 250% de l'ATK)

-- ===================================================================================

-- ==========================================
-- 1. CREACIÓ DEL NOU ÍTEM
-- ==========================================
-- Es crea l'acció base
INSERT INTO ACT (ac_id, ac_name, ac_desc, ac_type)
VALUES (11, 'Extracte de Força', 'Consumible que augmenta l''ATK un 30% durant 2 torns.', 'I');

-- Es registra com a Ítem (això el farà elegible pel mètode generateChestLoot)
INSERT INTO ITEM (it_action_id)
VALUES (11);

-- Es vincula a l'Efecte 5 (Modificació d'atributs)
INSERT INTO ACTION_EFFECT (ae_id, ae_action_id, ae_effect_id)
VALUES (11, 11, 5);

-- Es defineixen els paràmetres del buff
INSERT INTO PARAMETER_VALUE (pv_actioneffect_id, pv_parametertype_id, pv_effect_id, pv_string_value, pv_num_value, pv_altered_state_value)
VALUES
    (11, 4, 5, NULL, 30, NULL),   -- Magnitude: 30
    (11, 5, 5, 'P', NULL, NULL),  -- ValueType: Percentual
    (11, 6, 5, 'A', NULL, NULL),  -- Target: ALLY (es pot aplicar a un aliat)
    (11, 7, 5, NULL, 2, NULL),    -- Duration: 2 torns
    (11, 9, 5, 'ATK', NULL, NULL);-- Atribut: ATK

-- ==========================================
-- 2. CONFIGURACIÓ DE DROPS (Lògica de LootService)
-- ==========================================
-- Es vincula l'ítem "Extracte de Força" (11) i la "Poció Curativa" (3) als enemics

-- L'enemic ScaredDog (id 6) té una probabilitat baixa de donar objectes
INSERT INTO ENEMY_DROPS (ed_item_id, ed_enemy_id, ed_chance)
VALUES 
    (3, 6, 0.05), -- 5% de probabilitat de donar Poció Curativa
    (11, 6, 0.02); -- 2% de probabilitat de donar Extracte de Força

-- L'enemic Fang (id 7), com que és més fort, té millors probabilitats
INSERT INTO ENEMY_DROPS (ed_item_id, ed_enemy_id, ed_chance)
VALUES 
    (3, 7, 0.15), -- 15% de probabilitat de donar Poció Curativa
    (11, 7, 0.10); -- 10% de probabilitat de donar Extracte de Força

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================

-- ===================================================================================
