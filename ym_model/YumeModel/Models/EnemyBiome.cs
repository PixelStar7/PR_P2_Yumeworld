using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class EnemyBiome
{
    public int EnbiEnemyId { get; set; }

    public int EnbiBiomeId { get; set; }

    public int EnbiQuantity { get; set; }

    public virtual Enemy EnbiEnemy { get; set; } = null!;

    public virtual Biome EnbiBiome { get; set; } = null!;
}
