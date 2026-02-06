using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class EnemyFloor
{
    public int EnflEnemyId { get; set; }

    public int EnflFloorId { get; set; }

    public int EnflQuantity { get; set; }

    public virtual Enemy EnflEnemy { get; set; } = null!;

    public virtual Floor EnflFloor { get; set; } = null!;
}
