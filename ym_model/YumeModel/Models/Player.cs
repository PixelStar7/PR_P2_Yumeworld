using System;
using System.Collections.Generic;

namespace YumeModel.Models;

public partial class Player
{
    public int PlYumedantId { get; set; }

    public int PlExp { get; set; }

    public int? PlRoleId { get; set; }

    public virtual Playerrole? PlRole { get; set; }

    public virtual Yumedant PlYumedant { get; set; } = null!;
}
