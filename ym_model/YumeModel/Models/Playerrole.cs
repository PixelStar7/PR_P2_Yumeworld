using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Playerrole
{
    public int PrId { get; set; }

    public string? PrName { get; set; }

    public string? PrDesc { get; set; }

    public virtual ICollection<Player> Players { get; set; } = new List<Player>();

    public virtual ICollection<Ability> RaAbilities { get; set; } = new List<Ability>();
}
