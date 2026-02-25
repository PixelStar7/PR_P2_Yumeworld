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
    /// Lógica de interacción para PagePersonatge.xaml
    /// </summary>
    public partial class PagePersonatge : Page
    {
        public PagePersonatge(Yumedant yumedant = null)
        {
            InitializeComponent();
        }

        private void btnSaveYumedant_Click(object sender, RoutedEventArgs e)
        {

        }

        private void rbType_Click(object sender, RoutedEventArgs e)
        {
            if (rbPlayer.IsChecked == true)
            {
                pnlPlayerConfig.Visibility = Visibility.Visible;
                pnlEnemyConfig.Visibility = Visibility.Collapsed;
            }
            else
            {
                pnlPlayerConfig.Visibility = Visibility.Collapsed;
                pnlEnemyConfig.Visibility = Visibility.Visible;
            }
        }
    }
}
