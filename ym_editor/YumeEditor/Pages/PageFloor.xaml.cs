using Microsoft.Win32;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using YumeModel.Models;

namespace YumeEditor.Pages
{
    /// <summary>
    /// Lógica de interacción para PageFloor.xaml
    /// </summary>
    public partial class PageFloor : Page
    {
        // Canviar per una referència a l'objecte que estiguem modificant.
        private Biome bioma_actual;

        public PageFloor(Floor? floor)
        {
            InitializeComponent();
        }

        private void btnUpload_Click(object sender, RoutedEventArgs e)
        {
            if (bioma_actual == null) return;

            // Creem un diàleg per obrir fitxers
            OpenFileDialog openFileDialog = new OpenFileDialog();

            // Filtrem perquè només mostri imatges
            openFileDialog.Filter = "Image Files|*.jpg;*.jpeg;*.png;*.bmp|All Files|*.*";
            openFileDialog.Title = "Selecciona una imatge per al personatge";

            // Mostrem el diàleg i comprovem si l'usuari ha seleccionat un fitxer
            if (openFileDialog.ShowDialog() == true)
            {
                bioma_actual.BiBackground = openFileDialog.FileName;
            }
        }

        private void btnSaveFloor_Click(object sender, RoutedEventArgs e)
        {

        }

        private bool validarCamps()
        {
            return true; 
        }
    }
}
