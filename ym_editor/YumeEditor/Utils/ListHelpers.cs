using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace YumeEditor.Utils
{
    // ==========================================
    // HELPER PELS DROPS DELS ENEMICS
    // ==========================================
    public class DropHelper
    {
        public int ItemId { get; set; }
        public string NomItem { get; set; }
        public float Chance { get; set; }

        public override string ToString()
        {
            return $"{NomItem} - {Chance * 100}% de probabilitat";
        }
    }

    // ==========================================
    // HELPER PER LES HABILITATS DELS ROLS
    // ==========================================
    public class AbilityHelper
    {
        public int AbilityId { get; set; }
        public string NomAbility { get; set; }

        public override string ToString()
        {
            return $"⚔️ {NomAbility}";
        }
    }
}
