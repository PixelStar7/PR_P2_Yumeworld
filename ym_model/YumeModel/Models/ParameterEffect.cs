using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class ParameterEffect
{
    public int PeParametertypeId { get; set; }

    public int PeEffectId { get; set; }

    public virtual ICollection<ParameterValue> ParameterValues { get; set; } = new List<ParameterValue>();

    public virtual Effect PeEffect { get; set; } = null!;

    public virtual ParameterType PeParametertype { get; set; } = null!;
}
