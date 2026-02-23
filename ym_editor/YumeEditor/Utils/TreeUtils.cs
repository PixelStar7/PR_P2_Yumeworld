using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace YumeEditor.Utils
{
    public static class TreeUtils
    {
        public static void actualitzarTree()
        {
            if (Application.Current.MainWindow is MainWindow mainWindow)
            {
                mainWindow.CarregarArbre(); // Cridem al teu mètode per refrescar la llista!
            }
        }
    }
}
