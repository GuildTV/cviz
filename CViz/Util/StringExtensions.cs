using System.Text;

namespace CViz.Util
{
    static class StringExtensions
    {
        public static string AddSlashes(this string str)
        {
            var ret = new StringBuilder();
            foreach (char c in str)
            {
                switch (c)
                {
                    case '\'': ret.Append("\\\'"); break;
                    case '\"': ret.Append("\\\""); break;
                    case '\0': ret.Append("\\0"); break;
                    case '\\': ret.Append("\\\\"); break;
                    default: ret.Append(c.ToString()); break;
                }
            }
            return ret.ToString();
        }
    }
}