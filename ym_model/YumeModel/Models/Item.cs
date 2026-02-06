using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Item
{
    public int ItActionId { get; set; }

    public virtual ICollection<EnemyDrop> EnemyDrops { get; set; } = new List<EnemyDrop>();

    public virtual Act ItAction { get; set; } = null!;
}
