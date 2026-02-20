using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using YumeModel.Models;

namespace YumeModel.Diccionaris
{
    public static class ParameterTypes_Values
    {


        public static Dictionary<string, string> GetValorsValueType()
        {
            return new Dictionary<string, string>
            {
                { "A", "ABSOLUTE" }, // La Key és el que es guarda (pv_string_value)
                { "P", "PERCENTUAL" }
            };
        }

        public static bool isValidValueType(String valueType)
        {
            if (valueType == null) return false;
            if (GetValorsValueType().ContainsKey(valueType)) return true;
            else return false;
        }

        public static Dictionary<string, string> GetValorsTarget()
        {
            return new Dictionary<string, string>
            {
                { "S", "SELF" },
                { "A", "ALLY" },
                { "W", "ALL ALLIES" },
                { "E", "ENEMY" },
                { "T", "ALL ENEMIES" }
            };
        }

        public static bool isValidTarget(String target)
        {
            if (target == null) return false;
            if (GetValorsTarget().ContainsKey(target)) return true;
            else return false;
        }

        public static List<string> GetValorsRefAttr()
        {
            List<string> attributes = new List<string>();
            attributes.Add("HP");
            attributes.Add("MP");
            attributes.Add("ATK");
            attributes.Add("DEF");
            return attributes;
        }

        public static bool isValidRefAttr(String refAttr)
        {
            if (refAttr == null) return false;
            if (GetValorsRefAttr().Contains(refAttr)) return true;
            else return false;
        }

        public static List<AlteredState> getValorsAlteredState()
        {
            AppDbContext _context = new AppDbContext();
            return _context.AlteredStates.ToList();
        }

        public static List<AlteredState> getValorsAlteredState(AppDbContext db) => db.AlteredStates.ToList();




    }

}
