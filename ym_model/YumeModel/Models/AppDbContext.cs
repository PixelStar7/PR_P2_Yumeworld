using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;
using Pomelo.EntityFrameworkCore.MySql.Scaffolding.Internal;

namespace YumeModel.Models;

public partial class AppDbContext : DbContext
{
    public AppDbContext()
    {
    }

    public AppDbContext(DbContextOptions<AppDbContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Ability> Abilities { get; set; }

    public virtual DbSet<Act> Acts { get; set; }

    public virtual DbSet<ActionEffect> ActionEffects { get; set; }

    public virtual DbSet<AlteredState> AlteredStates { get; set; }

    public virtual DbSet<Biome> Biomes { get; set; }

    public virtual DbSet<Effect> Effects { get; set; }

    public virtual DbSet<Enemy> Enemies { get; set; }

    public virtual DbSet<EnemyDrop> EnemyDrops { get; set; }

    public virtual DbSet<EnemyFloor> EnemyFloors { get; set; }

    public virtual DbSet<Floor> Floors { get; set; }

    public virtual DbSet<Item> Items { get; set; }

    public virtual DbSet<ParameterEffect> ParameterEffects { get; set; }

    public virtual DbSet<ParameterType> ParameterTypes { get; set; }

    public virtual DbSet<ParameterValue> ParameterValues { get; set; }

    public virtual DbSet<Player> Players { get; set; }

    public virtual DbSet<Playerrole> Playerroles { get; set; }

    public virtual DbSet<Yumedant> Yumedants { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
#warning To protect potentially sensitive information in your connection string, you should move it out of source code. You can avoid scaffolding the connection string by using the Name= syntax to read it from configuration - see https://go.microsoft.com/fwlink/?linkid=2131148. For more guidance on storing connection strings, see https://go.microsoft.com/fwlink/?LinkId=723263.
        => optionsBuilder.UseMySql("server=10.150.1.126;database=yumeworld;port=3306;uid=sara", Microsoft.EntityFrameworkCore.ServerVersion.Parse("10.4.32-mariadb"));

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder
            .UseCollation("utf8mb4_general_ci")
            .HasCharSet("utf8mb4");

        modelBuilder.Entity<Ability>(entity =>
        {
            entity.HasKey(e => e.AbActionId).HasName("PRIMARY");

            entity.ToTable("ability");

            entity.Property(e => e.AbActionId)
                .ValueGeneratedNever()
                .HasColumnType("int(11)")
                .HasColumnName("ab_action_id");
            entity.Property(e => e.AbCost)
                .HasDefaultValueSql("'10'")
                .HasColumnType("int(11)")
                .HasColumnName("ab_cost");

            entity.HasOne(d => d.AbAction).WithOne(p => p.Ability)
                .HasForeignKey<Ability>(d => d.AbActionId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("ability_ibfk_1");
        });

        modelBuilder.Entity<Act>(entity =>
        {
            entity.HasKey(e => e.AcId).HasName("PRIMARY");

            entity.ToTable("act");

            entity.HasIndex(e => e.AcName, "idx_action_name");

            entity.Property(e => e.AcId)
                .HasColumnType("int(11)")
                .HasColumnName("ac_id");
            entity.Property(e => e.AcDesc)
                .HasMaxLength(255)
                .HasColumnName("ac_desc");
            entity.Property(e => e.AcImg)
                .HasMaxLength(500)
                .HasColumnName("ac_img");
            entity.Property(e => e.AcName).HasColumnName("ac_name");
            entity.Property(e => e.AcType)
                .HasMaxLength(1)
                .IsFixedLength()
                .HasColumnName("ac_type");
        });

        modelBuilder.Entity<ActionEffect>(entity =>
        {
            entity.HasKey(e => e.AeId).HasName("PRIMARY");

            entity.ToTable("action_effect");

            entity.HasIndex(e => e.AeActionId, "FK_ACTIONEFFECT_ACT");

            entity.HasIndex(e => e.AeEffectId, "FK_ACTIONEFFECT_EFFECT");

            entity.Property(e => e.AeId)
                .HasColumnType("int(11)")
                .HasColumnName("ae_id");
            entity.Property(e => e.AeActionId)
                .HasColumnType("int(11)")
                .HasColumnName("ae_action_id");
            entity.Property(e => e.AeEffectId)
                .HasColumnType("int(11)")
                .HasColumnName("ae_effect_id");

            entity.HasOne(d => d.AeAction).WithMany(p => p.ActionEffects)
                .HasForeignKey(d => d.AeActionId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_ACTIONEFFECT_ACT");

            entity.HasOne(d => d.AeEffect).WithMany(p => p.ActionEffects)
                .HasForeignKey(d => d.AeEffectId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_ACTIONEFFECT_EFFECT");
        });

        modelBuilder.Entity<AlteredState>(entity =>
        {
            entity.HasKey(e => e.AsId).HasName("PRIMARY");

            entity.ToTable("altered_state");

            entity.Property(e => e.AsId)
                .HasColumnType("int(11)")
                .HasColumnName("as_id");
            entity.Property(e => e.AsIcon)
                .HasMaxLength(500)
                .HasColumnName("as_icon");
            entity.Property(e => e.AsName)
                .HasMaxLength(50)
                .HasColumnName("as_name");
        });

        modelBuilder.Entity<Biome>(entity =>
        {
            entity.HasKey(e => e.BiId).HasName("PRIMARY");

            entity.ToTable("biome");

            entity.Property(e => e.BiId)
                .HasColumnType("int(11)")
                .HasColumnName("bi_id");
            entity.Property(e => e.BiBackground)
                .HasMaxLength(500)
                .HasColumnName("bi_background");
        });

        modelBuilder.Entity<Effect>(entity =>
        {
            entity.HasKey(e => e.EfId).HasName("PRIMARY");

            entity.ToTable("effect");

            entity.Property(e => e.EfId)
                .HasColumnType("int(11)")
                .HasColumnName("ef_id");
            entity.Property(e => e.EfName)
                .HasMaxLength(255)
                .HasColumnName("ef_name");
        });

        modelBuilder.Entity<Enemy>(entity =>
        {
            entity.HasKey(e => e.EnYumedantId).HasName("PRIMARY");

            entity.ToTable("enemy");

            entity.Property(e => e.EnYumedantId)
                .ValueGeneratedNever()
                .HasColumnType("int(11)")
                .HasColumnName("en_yumedant_id");
            entity.Property(e => e.EnGivenExp)
                .HasColumnType("int(11)")
                .HasColumnName("en_given_exp");

            entity.HasOne(d => d.EnYumedant).WithOne(p => p.Enemy)
                .HasForeignKey<Enemy>(d => d.EnYumedantId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("enemy_ibfk_1");
        });

        modelBuilder.Entity<EnemyDrop>(entity =>
        {
            entity.HasKey(e => new { e.EdItemId, e.EdEnemyId })
                .HasName("PRIMARY")
                .HasAnnotation("MySql:IndexPrefixLength", new[] { 0, 0 });

            entity.ToTable("enemy_drops");

            entity.HasIndex(e => e.EdEnemyId, "ed_enemy_id");

            entity.Property(e => e.EdItemId)
                .HasColumnType("int(11)")
                .HasColumnName("ed_item_id");
            entity.Property(e => e.EdEnemyId)
                .HasColumnType("int(11)")
                .HasColumnName("ed_enemy_id");
            entity.Property(e => e.EdChance)
                .HasDefaultValueSql("'0.05'")
                .HasColumnName("ed_chance");

            entity.HasOne(d => d.EdEnemy).WithMany(p => p.EnemyDrops)
                .HasForeignKey(d => d.EdEnemyId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("enemy_drops_ibfk_2");

            entity.HasOne(d => d.EdItem).WithMany(p => p.EnemyDrops)
                .HasForeignKey(d => d.EdItemId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("enemy_drops_ibfk_1");
        });

        modelBuilder.Entity<EnemyFloor>(entity =>
        {
            entity.HasKey(e => new { e.EnflEnemyId, e.EnflFloorId })
                .HasName("PRIMARY")
                .HasAnnotation("MySql:IndexPrefixLength", new[] { 0, 0 });

            entity.ToTable("enemy_floors");

            entity.HasIndex(e => e.EnflFloorId, "enfl_floor_id");

            entity.Property(e => e.EnflEnemyId)
                .HasColumnType("int(11)")
                .HasColumnName("enfl_enemy_id");
            entity.Property(e => e.EnflFloorId)
                .HasColumnType("int(11)")
                .HasColumnName("enfl_floor_id");
            entity.Property(e => e.EnflQuantity)
                .HasColumnType("int(11)")
                .HasColumnName("enfl_quantity");

            entity.HasOne(d => d.EnflEnemy).WithMany(p => p.EnemyFloors)
                .HasForeignKey(d => d.EnflEnemyId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("enemy_floors_ibfk_1");

            entity.HasOne(d => d.EnflFloor).WithMany(p => p.EnemyFloors)
                .HasForeignKey(d => d.EnflFloorId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("enemy_floors_ibfk_2");
        });

        modelBuilder.Entity<Floor>(entity =>
        {
            entity.HasKey(e => e.FlId).HasName("PRIMARY");

            entity.ToTable("floor");

            entity.HasIndex(e => e.FlBiomeId, "fl_biome_id");

            entity.Property(e => e.FlId)
                .HasColumnType("int(11)")
                .HasColumnName("fl_id");
            entity.Property(e => e.FlBiomeId)
                .HasColumnType("int(11)")
                .HasColumnName("fl_biome_id");
            entity.Property(e => e.FlName)
                .HasMaxLength(20)
                .HasColumnName("fl_name");
            entity.Property(e => e.FlNum)
                .HasColumnType("int(11)")
                .HasColumnName("fl_num");

            entity.HasOne(d => d.FlBiome).WithMany(p => p.Floors)
                .HasForeignKey(d => d.FlBiomeId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("floor_ibfk_1");
        });

        modelBuilder.Entity<Item>(entity =>
        {
            entity.HasKey(e => e.ItActionId).HasName("PRIMARY");

            entity.ToTable("item");

            entity.Property(e => e.ItActionId)
                .ValueGeneratedNever()
                .HasColumnType("int(11)")
                .HasColumnName("it_action_id");

            entity.HasOne(d => d.ItAction).WithOne(p => p.Item)
                .HasForeignKey<Item>(d => d.ItActionId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("item_ibfk_1");
        });

        modelBuilder.Entity<ParameterEffect>(entity =>
        {
            entity.HasKey(e => new { e.PeParametertypeId, e.PeEffectId })
                .HasName("PRIMARY")
                .HasAnnotation("MySql:IndexPrefixLength", new[] { 0, 0 });

            entity.ToTable("parameter_effect");

            entity.HasIndex(e => e.PeEffectId, "pe_effect_id");

            entity.Property(e => e.PeParametertypeId)
                .HasColumnType("int(11)")
                .HasColumnName("pe_parametertype_id");
            entity.Property(e => e.PeEffectId)
                .HasColumnType("int(11)")
                .HasColumnName("pe_effect_id");

            entity.HasOne(d => d.PeEffect).WithMany(p => p.ParameterEffects)
                .HasForeignKey(d => d.PeEffectId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("parameter_effect_ibfk_2");

            entity.HasOne(d => d.PeParametertype).WithMany(p => p.ParameterEffects)
                .HasForeignKey(d => d.PeParametertypeId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("parameter_effect_ibfk_1");
        });

        modelBuilder.Entity<ParameterType>(entity =>
        {
            entity.HasKey(e => e.PtId).HasName("PRIMARY");

            entity.ToTable("parameter_types");

            entity.Property(e => e.PtId)
                .HasColumnType("int(11)")
                .HasColumnName("pt_id");
            entity.Property(e => e.PtName)
                .HasMaxLength(20)
                .HasColumnName("pt_name");
            entity.Property(e => e.PtNumMax)
                .HasColumnType("int(11)")
                .HasColumnName("pt_num_max");
            entity.Property(e => e.PtNumMin)
                .HasColumnType("int(11)")
                .HasColumnName("pt_num_min");
            entity.Property(e => e.PtType)
                .HasMaxLength(1)
                .IsFixedLength()
                .HasColumnName("pt_type");
            entity.Property(e => e.PtValueList)
                .HasMaxLength(255)
                .HasColumnName("pt_value_list");
        });

        modelBuilder.Entity<ParameterValue>(entity =>
        {
            entity.HasKey(e => new { e.PvActioneffectId, e.PvParametertypeId })
                .HasName("PRIMARY")
                .HasAnnotation("MySql:IndexPrefixLength", new[] { 0, 0 });

            entity.ToTable("parameter_value");

            entity.HasIndex(e => e.PvAlteredStateValue, "pv_altered_state_value");

            entity.HasIndex(e => new { e.PvParametertypeId, e.PvEffectId }, "pv_parametertype_id");

            entity.Property(e => e.PvActioneffectId)
                .HasColumnType("int(11)")
                .HasColumnName("pv_actioneffect_id");
            entity.Property(e => e.PvParametertypeId)
                .HasColumnType("int(11)")
                .HasColumnName("pv_parametertype_id");
            entity.Property(e => e.PvAlteredStateValue)
                .HasColumnType("int(11)")
                .HasColumnName("pv_altered_state_value");
            entity.Property(e => e.PvEffectId)
                .HasColumnType("int(11)")
                .HasColumnName("pv_effect_id");
            entity.Property(e => e.PvNumValue)
                .HasColumnType("int(11)")
                .HasColumnName("pv_num_value");
            entity.Property(e => e.PvStringValue)
                .HasMaxLength(10)
                .HasColumnName("pv_string_value");

            entity.HasOne(d => d.PvActioneffect).WithMany(p => p.ParameterValues)
                .HasForeignKey(d => d.PvActioneffectId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("parameter_value_ibfk_1");

            entity.HasOne(d => d.PvAlteredStateValueNavigation).WithMany(p => p.ParameterValues)
                .HasForeignKey(d => d.PvAlteredStateValue)
                .HasConstraintName("parameter_value_ibfk_3");

            entity.HasOne(d => d.ParameterEffect).WithMany(p => p.ParameterValues)
                .HasForeignKey(d => new { d.PvParametertypeId, d.PvEffectId })
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("parameter_value_ibfk_2");
        });

        modelBuilder.Entity<Player>(entity =>
        {
            entity.HasKey(e => e.PlYumedantId).HasName("PRIMARY");

            entity.ToTable("player");

            entity.HasIndex(e => e.PlRoleId, "pl_role_id");

            entity.Property(e => e.PlYumedantId)
                .ValueGeneratedNever()
                .HasColumnType("int(11)")
                .HasColumnName("pl_yumedant_id");
            entity.Property(e => e.PlExp)
                .HasColumnType("int(11)")
                .HasColumnName("pl_exp");
            entity.Property(e => e.PlRoleId)
                .HasColumnType("int(11)")
                .HasColumnName("pl_role_id");

            entity.HasOne(d => d.PlRole).WithMany(p => p.Players)
                .HasForeignKey(d => d.PlRoleId)
                .HasConstraintName("player_ibfk_2");

            entity.HasOne(d => d.PlYumedant).WithOne(p => p.Player)
                .HasForeignKey<Player>(d => d.PlYumedantId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("player_ibfk_1");
        });

        modelBuilder.Entity<Playerrole>(entity =>
        {
            entity.HasKey(e => e.PrId).HasName("PRIMARY");

            entity.ToTable("playerrole");

            entity.Property(e => e.PrId)
                .HasColumnType("int(11)")
                .HasColumnName("pr_id");
            entity.Property(e => e.PrAtk)
                .HasColumnType("int(11)")
                .HasColumnName("pr_atk");
            entity.Property(e => e.PrDef)
                .HasColumnType("int(11)")
                .HasColumnName("pr_def");
            entity.Property(e => e.PrDesc)
                .HasMaxLength(255)
                .HasColumnName("pr_desc");
            entity.Property(e => e.PrHp)
                .HasColumnType("int(11)")
                .HasColumnName("pr_hp");
            entity.Property(e => e.PrMp)
                .HasColumnType("int(11)")
                .HasColumnName("pr_mp");
            entity.Property(e => e.PrName)
                .HasMaxLength(50)
                .HasColumnName("pr_name");

            entity.HasMany(d => d.RaAbilities).WithMany(p => p.RaPlayerroles)
                .UsingEntity<Dictionary<string, object>>(
                    "PlayerroleAbility",
                    r => r.HasOne<Ability>().WithMany()
                        .HasForeignKey("RaAbilityId")
                        .OnDelete(DeleteBehavior.ClientSetNull)
                        .HasConstraintName("playerrole_ability_ibfk_2"),
                    l => l.HasOne<Playerrole>().WithMany()
                        .HasForeignKey("RaPlayerroleId")
                        .OnDelete(DeleteBehavior.ClientSetNull)
                        .HasConstraintName("playerrole_ability_ibfk_1"),
                    j =>
                    {
                        j.HasKey("RaPlayerroleId", "RaAbilityId")
                            .HasName("PRIMARY")
                            .HasAnnotation("MySql:IndexPrefixLength", new[] { 0, 0 });
                        j.ToTable("playerrole_ability");
                        j.HasIndex(new[] { "RaAbilityId" }, "ra_ability_id");
                        j.IndexerProperty<int>("RaPlayerroleId")
                            .HasColumnType("int(11)")
                            .HasColumnName("ra_playerrole_id");
                        j.IndexerProperty<int>("RaAbilityId")
                            .HasColumnType("int(11)")
                            .HasColumnName("ra_ability_id");
                    });
        });

        modelBuilder.Entity<Yumedant>(entity =>
        {
            entity.HasKey(e => e.YuId).HasName("PRIMARY");

            entity.ToTable("yumedant");

            entity.HasIndex(e => e.YuName, "idx_yumedant_name");

            entity.Property(e => e.YuId)
                .HasColumnType("int(11)")
                .HasColumnName("yu_id");
            entity.Property(e => e.YuAtk)
                .HasDefaultValueSql("'1'")
                .HasColumnType("int(11)")
                .HasColumnName("yu_atk");
            entity.Property(e => e.YuDef)
                .HasDefaultValueSql("'1'")
                .HasColumnType("int(11)")
                .HasColumnName("yu_def");
            entity.Property(e => e.YuDesc)
                .HasMaxLength(255)
                .HasColumnName("yu_desc");
            entity.Property(e => e.YuHp)
                .HasDefaultValueSql("'1'")
                .HasColumnType("int(11)")
                .HasColumnName("yu_hp");
            entity.Property(e => e.YuImage)
                .HasMaxLength(500)
                .HasColumnName("yu_image");
            entity.Property(e => e.YuLevel)
                .HasDefaultValueSql("'1'")
                .HasColumnType("int(11)")
                .HasColumnName("yu_level");
            entity.Property(e => e.YuMp)
                .HasDefaultValueSql("'1'")
                .HasColumnType("int(11)")
                .HasColumnName("yu_mp");
            entity.Property(e => e.YuName).HasColumnName("yu_name");

            entity.HasMany(d => d.YaActions).WithMany(p => p.YaYumedants)
                .UsingEntity<Dictionary<string, object>>(
                    "YumedantAct",
                    r => r.HasOne<Act>().WithMany()
                        .HasForeignKey("YaActionId")
                        .OnDelete(DeleteBehavior.ClientSetNull)
                        .HasConstraintName("yumedant_act_ibfk_2"),
                    l => l.HasOne<Yumedant>().WithMany()
                        .HasForeignKey("YaYumedantId")
                        .OnDelete(DeleteBehavior.ClientSetNull)
                        .HasConstraintName("yumedant_act_ibfk_1"),
                    j =>
                    {
                        j.HasKey("YaYumedantId", "YaActionId")
                            .HasName("PRIMARY")
                            .HasAnnotation("MySql:IndexPrefixLength", new[] { 0, 0 });
                        j.ToTable("yumedant_act");
                        j.HasIndex(new[] { "YaActionId" }, "ya_action_id");
                        j.IndexerProperty<int>("YaYumedantId")
                            .HasColumnType("int(11)")
                            .HasColumnName("ya_yumedant_id");
                        j.IndexerProperty<int>("YaActionId")
                            .HasColumnType("int(11)")
                            .HasColumnName("ya_action_id");
                    });
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
