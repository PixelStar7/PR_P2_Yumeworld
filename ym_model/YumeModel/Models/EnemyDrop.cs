using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class EnemyDrop
{
    public int EdItemId { get; set; }

    public int EdEnemyId { get; set; }

    public float EdChance { get; set; }

    public virtual Enemy EdEnemy { get; set; } = null!;

    public virtual Item EdItem { get; set; } = null!;
}
