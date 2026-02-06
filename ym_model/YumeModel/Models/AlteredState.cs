using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class AlteredState
{
    public int AsId { get; set; }

    public string? AsName { get; set; }

    public string? AsIcon { get; set; }

    public virtual ICollection<ParameterValue> ParameterValues { get; set; } = new List<ParameterValue>();
}
