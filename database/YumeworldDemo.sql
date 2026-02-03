-- Script de Dades Demo de la Base de Dades Yumeworld
-- INSERT INTO table_name (column1, column2, column3, ...)
-- VALUES (value1, value2, value3, ...);

-- Taules bàsiques

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

INSERT INTO EFFECT (ef_id, ef_type)
VALUES 
    (1, 'Mal simple'), 
    (2, 'Curació'), 
    (3, 'Estat alterat'), 
    (4, 'Modificacio estadístiques');

INSERT INTO PARAMETER_TYPES (pt_id, pt_name)
VALUES
    (1, 'Stat'), 
    (2, 'MaxRange'), 
    (3, 'MinRange'), 
    (4, 'Mangitude'), 
    (5, 'ValueType'), 
    (6, 'Target'), 
    (7, 'Duration'), 
    (8, 'AlteredState');


-- Taules amb herències
INSERT INTO ACT (ac_id, ac_name, ac_desc, ac_type)
VALUES
    (1, 'Basic atack', 'Cop simple amb l''arma.', 'A'),
    (2, 'Basic heal', 'Cura un 33% de l''HP màxim.', 'A'),
    (3, 'Poció curativa', 'Recupera 10 punts de vida.', 'I'),
    (4, 'Enverinar', 'Enverina l''enemic durant 3 torns', 'A');

INSERT INTO ITEM (it_action_id) 
VALUES (3);

INSERT INTO ABILITY (ab_action_id, ab_cost) 
VALUES (1, 0), (2,5);

INSERT INTO YUMEDANT (yu_id, yu_name, yu_desc, yu_level, yu_hp, yu_mp, yu_atk, yu_def)
VALUES 
    (1, 'Pingu', 'Et mossega amb molt d''amor.', 1, 11, 15, 10, 5), 
    (2, 'Michi', 'Cega i altament desorientada.', 1, 17, 10, 15, 6),
    (3, 'Nus', 'Esperit protector.', 1, 20, 5, 8, 15),
    (4, 'Pipa', 'S''espanta si respires massa fort.', 1, 8, 10, 10, 10),
    (5, 'Sau', 'Persevera en la seva aventura', 1, 20, 10, 10, 10);

INSERT INTO ENEMY (en_yumedant_id, en_given_exp)
VALUES 
    (1, 50), 
    (2, 50),  
    (3, 100),  
    (4, 50); 

INSERT INTO PLAYER (pl_yumedant_id, pl_exp, pl_role_id)
VALUES 
    (5, 0, 2);  -- Player Sau

-- Taules que relacionen altres taules

INSERT INTO PARAMETER_EFFECT (pe_parametertype_id, pe_effect_id)
VALUES
    -- Ens diu quines coses necessita cada efecte.
    -- El "Mal simple" (1) requereix una Magnitud (4) i un Target (6)
    (4, 1), 
    (6, 1),
    -- La "Curació" (2) requereix una Magnitud (4)
    (4, 2),
    (6, 2),
    --Modificacio d'estat alterat
    (6, 4),
    (7, 4),
    (8, 4);

INSERT INTO ACTION_EFFECT (ae_id, ae_action_id, ae_effect_id)
VALUES
    (1, 1, 1), -- Basic Atk fa Mal simple
    (2, 3, 2), -- Poció fa Curació
    (3, 2, 2); -- Basic Heal fa Curació

