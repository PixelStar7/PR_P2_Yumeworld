using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Playerrole
{
    public int PrId { get; set; }

    public string? PrName { get; set; }

    public string? PrDesc { get; set; }

    public int PrHp { get; set; }

    public int PrMp { get; set; }

    public int PrAtk { get; set; }

    public int PrDef { get; set; }

    public virtual ICollection<Player> Players { get; set; } = new List<Player>();

    public virtual ICollection<Ability> RaAbilities { get; set; } = new List<Ability>();
}
