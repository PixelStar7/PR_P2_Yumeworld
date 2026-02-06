using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class ActionEffect
{
    public int AeId { get; set; }

    public int AeEffectId { get; set; }

    public int AeActionId { get; set; }

    public virtual Act AeAction { get; set; } = null!;

    public virtual Effect AeEffect { get; set; } = null!;

    public virtual ICollection<ParameterValue> ParameterValues { get; set; } = new List<ParameterValue>();
}
