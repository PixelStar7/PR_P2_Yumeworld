using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Yumedant
{
    public int YuId { get; set; }

    public string YuName { get; set; } = null!;

    public string? YuDesc { get; set; }

    public int YuLevel { get; set; }

    public int YuHp { get; set; }

    public int YuMp { get; set; }

    public int YuAtk { get; set; }

    public int YuDef { get; set; }

    public string? YuImage { get; set; }

    public virtual Enemy? Enemy { get; set; }

    public virtual Player? Player { get; set; }

    public virtual ICollection<Act> YaActions { get; set; } = new List<Act>();
}
