using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Act
{
    public int AcId { get; set; }

    public string AcName { get; set; } = null!;

    public string AcDesc { get; set; } = null!;

    public string? AcImg { get; set; }

    public string? AcType { get; set; }

    public virtual Ability? Ability { get; set; }

    public virtual ICollection<ActionEffect> ActionEffects { get; set; } = new List<ActionEffect>();

    public virtual Item? Item { get; set; }

    public virtual ICollection<Yumedant> YaYumedants { get; set; } = new List<Yumedant>();
}
