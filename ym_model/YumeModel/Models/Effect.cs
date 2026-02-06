using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Effect
{
    public int EfId { get; set; }

    public string? EfName { get; set; }

    public virtual ICollection<ActionEffect> ActionEffects { get; set; } = new List<ActionEffect>();

    public virtual ICollection<ParameterEffect> ParameterEffects { get; set; } = new List<ParameterEffect>();
}
