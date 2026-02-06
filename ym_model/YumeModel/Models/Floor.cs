using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Floor
{
    public int FlId { get; set; }

    public int FlNum { get; set; }

    public string FlName { get; set; } = null!;

    public int FlBiomeId { get; set; }

    public virtual ICollection<EnemyFloor> EnemyFloors { get; set; } = new List<EnemyFloor>();

    public virtual Biome FlBiome { get; set; } = null!;
}
