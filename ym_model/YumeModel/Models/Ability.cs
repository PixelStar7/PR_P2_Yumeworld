using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Ability
{
    public int AbActionId { get; set; }

    public int AbCost { get; set; }

    public virtual Act AbAction { get; set; } = null!;

    public virtual ICollection<Playerrole> RaPlayerroles { get; set; } = new List<Playerrole>();
}
