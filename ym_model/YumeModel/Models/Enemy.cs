using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Enemy
{
    public int EnYumedantId { get; set; }

    public int EnGivenExp { get; set; }

    public virtual Yumedant EnYumedant { get; set; } = null!;

    public virtual ICollection<EnemyDrop> EnemyDrops { get; set; } = new List<EnemyDrop>();

    public virtual ICollection<EnemyBiome> EnemyBiomes { get; set; } = new List<EnemyBiome>();
}
