using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Biome
{
    public int BiId { get; set; }

    public string? BiBackground { get; set; }

    public virtual ICollection<Floor> Floors { get; set; } = new List<Floor>();
}
