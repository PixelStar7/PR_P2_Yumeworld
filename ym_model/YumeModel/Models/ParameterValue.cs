using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class ParameterValue
{
    public int PvActioneffectId { get; set; }

    public int PvParametertypeId { get; set; }

    public int PvEffectId { get; set; }

    public string? PvStringValue { get; set; }

    public int? PvNumValue { get; set; }

    public int? PvAlteredStateValue { get; set; }

    public virtual ParameterEffect ParameterEffect { get; set; } = null!;

    public virtual ActionEffect PvActioneffect { get; set; } = null!;

    public virtual AlteredState? PvAlteredStateValueNavigation { get; set; }
}
