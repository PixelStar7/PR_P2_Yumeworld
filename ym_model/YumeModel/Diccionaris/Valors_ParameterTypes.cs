using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace YumeModel.Diccionaris
{
    public static class Valors_ParameterTypes
    {
        public static Dictionary<string, string> GetValorsValueType()
        {
            return new Dictionary<string, string>
            {
                { "A", "ABSOLUTE" }, // La Key és el que es guarda (pv_string_value)
                { "P", "PERCENTUAL" }
            };
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

        public static List<string> GetValorsAttributes()
        {
            List<string> attributes = new List<string>();
            attributes.Add("HP");
            attributes.Add("MP");
            attributes.Add("ATK");
            attributes.Add("DEF");
            return attributes;
        }


    }

}
