using System;
using System.Collections.Generic;

namespace CViz.Util
{
    static class ListExtensions
    {
        public static void AddRange<T>(this ICollection<T> ls, IEnumerable<T> objs)
        {
            foreach (T obj in objs)
                ls.Add(obj);
        }

        public static void AddRange<T>(this ICollection<T> ls, params T[] objs)
        {
            foreach (T obj in objs)
                ls.Add(obj);
        }


        public static void ForEach<T>(this IEnumerable<T> ls, Action<T> act)
        {
            foreach (T obj in ls)
                act(obj);
        }
    }
}
