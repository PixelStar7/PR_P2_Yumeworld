using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class ParameterType
{
    public int PtId { get; set; }

    public string PtName { get; set; } = null!;

    public string PtType { get; set; } = null!;

    public int? PtNumMin { get; set; }

    public int? PtNumMax { get; set; }

    public string? PtValueList { get; set; }

    public virtual ICollection<ParameterEffect> ParameterEffects { get; set; } = new List<ParameterEffect>();
}
