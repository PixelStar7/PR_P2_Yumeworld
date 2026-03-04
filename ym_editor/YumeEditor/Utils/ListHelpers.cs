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

    // ==========================================
    // HELPER PER CONTROLAR EFECTES MÚLTIPLES
    // ==========================================
    public class ActionEffectHelper
    {
        // Un ID únic per evitar que si poses 2 cops el mateix efecte, els RadioButtons es tornin bojos
        public string UniqueId { get; set; } = Guid.NewGuid().ToString();
        public int EffectId { get; set; }
        public string EffectName { get; set; }

        // Aquest Diccionari guardarà: [ID del Parametre] -> "Valor escrit per l'usuari"
        public Dictionary<int, string> ParameterValues { get; set; } = new Dictionary<int, string>();

        public override string ToString()
        {
            return $"✨ {EffectName}"; // Això és el que es llegirà a la ListBox
        }
    }
}
