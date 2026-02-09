using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using YumeEditor.Pages;
using YumeModel;

namespace YumeEditor
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public PageAct pageAct;
        public PageEffect pageEffect;
        public PageFloor pageFloor;
        public PagePersonatge pagePersonatge;
        public PageRole pageRole;
        public MainWindow()
        {
            InitializeComponent();
            pageAct = new PageAct();
            pageEffect = new PageEffect();
            pageFloor = new PageFloor();
            pagePersonatge = new PagePersonatge();
            pageRole = new PageRole();
        }

        private void btnYumedant_Click(object sender, RoutedEventArgs e)
        {
            frmMain.Navigate(pagePersonatge);
        }

        private void btnAction_Click(object sender, RoutedEventArgs e)
        {
            frmMain.Navigate(pageAct);

        }

        private void btnEffect_Click(object sender, RoutedEventArgs e)
        {
            frmMain.Navigate(pageEffect);

        }

        private void btnRole_Click(object sender, RoutedEventArgs e)
        {
            frmMain.Navigate(pageRole);

        }

        private void btnFloor_Click(object sender, RoutedEventArgs e)
        {
            frmMain.Navigate(pageFloor);

        }

        private void Window_Loaded(object sender, RoutedEventArgs e)
        {
            if (frmMain != null)
            {
                frmMain.Navigate(pagePersonatge);
            }
        }
    }
}