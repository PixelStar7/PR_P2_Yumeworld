using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class ParameterType
{
    public int PtId { get; set; }

    public string? PtName { get; set; }

    public virtual ICollection<ParameterEffect> ParameterEffects { get; set; } = new List<ParameterEffect>();
}
