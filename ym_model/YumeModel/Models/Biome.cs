using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Biome
{
    public int BiId { get; set; }

    public string BiName { get; set; } = null!;

    public int BiOrder { get; set; }

    public string? BiBackground { get; set; }

    public virtual ICollection<EnemyBiome> EnemyBiomes { get; set; } = new List<EnemyBiome>();
}
