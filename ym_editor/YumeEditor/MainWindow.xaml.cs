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
using YumeModel.Models;

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
            pageAct = new PageAct(null);
            pageEffect = new PageEffect(null);
            pageFloor = new PageFloor(null);
            pagePersonatge = new PagePersonatge(null);
            pageRole = new PageRole(null);
            CarregarArbre();
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

        // ==========================================
        // 1. FUNCIÓ PRINCIPAL PER MUNTAR L'ARBRE
        // ==========================================
        private void CarregarArbre()
        {
            tree_Contingut.Items.Clear();

            // Creem les carpetes (Nodes Arrel)
            TreeViewItem nodeYumedants = CrearCarpeta("Yumedants 🐾");
            TreeViewItem nodeActions = CrearCarpeta("Actions ⚔️");
            TreeViewItem nodeEffects = CrearCarpeta("Effects ✨");
            TreeViewItem nodeRoles = CrearCarpeta("Roles 🛡️");
            TreeViewItem nodeFloors = CrearCarpeta("Floors 🗺️");

            // INSTANCIEM EL CONTEXT (Aquí passa la màgia d'Entity Framework)
            // El "using" assegura que es tanca la connexió quan acaba de llegir
            using (var context = new AppDbContext())
            {
                // 1. YUMEDANTS 
                // (EF converteix 'yu_id' a 'YuId' i 'yu_name' a 'YuName' automàticament)
                var llistaYumes = context.Yumedants.ToList();
                foreach (var y in llistaYumes)
                {
                    AfegirItem(nodeYumedants, y.YuName, y.YuId, "Yumedant");
                }

                // 2. FLOORS
                var llistaFloors = context.Floors.ToList();
                foreach (var f in llistaFloors)
                {
                    AfegirItem(nodeFloors, f.FlName, f.FlId, "Floor");
                }

                // 3. ROLES (Taula PLAYERROLE -> Classe Playerrole)
                var llistaRoles = context.Playerroles.ToList();
                foreach (var r in llistaRoles)
                {
                    AfegirItem(nodeRoles, r.PrName, r.PrId, "Role");
                }

                // 4. EFFECTS
                var llistaEffects = context.Effects.ToList();
                foreach (var e in llistaEffects)
                {
                    // Nota: Al teu DDL era ef_name o ef_type? Revisa la propietat generada
                    AfegirItem(nodeEffects, e.EfName, e.EfId, "Effect");
                }

                // 5. ACTIONS (Taula ACT -> Classe Act)
                var llistaActions = context.Acts.ToList();
                foreach (var a in llistaActions)
                {
                    AfegirItem(nodeActions, a.AcName, a.AcId, "Action");
                }
            }

            // Afegim tot al TreeView
            tree_Contingut.Items.Add(nodeYumedants);
            tree_Contingut.Items.Add(nodeActions);
            tree_Contingut.Items.Add(nodeEffects);
            tree_Contingut.Items.Add(nodeRoles);
            tree_Contingut.Items.Add(nodeFloors);
        }

        // Els helpers 'CrearCarpeta' i 'AfegirItem' es queden IGUAL que abans, no cal tocar-los.

        // ==========================================
        // 2. HELPERS (FUNCIONS D'AJUDA)
        // ==========================================

        private TreeViewItem CrearCarpeta(string titol)
        {
            return new TreeViewItem
            {
                Header = titol,
                IsExpanded = true, // Que surtin obertes d'inici
                FontWeight = FontWeights.Bold
            };
        }

        private void AfegirItem(TreeViewItem pare, string nom, object dadesObjecte, string tipus)
        {
            TreeViewItem item = new TreeViewItem();
            item.Header = nom;
            item.FontWeight = FontWeights.Normal;

            // LA MÀGIA: Guardem un objecte anònim al TAG amb la info que necessitem
            item.Tag = new { DataObject = dadesObjecte, Type = tipus };

            pare.Items.Add(item);
        }


        // ==========================================
        // 3. EVENT DOBLE CLICK (LA NAVEGACIÓ)
        // ==========================================
        private void tree_Contingut_MouseDoubleClick(object sender, System.Windows.Input.MouseButtonEventArgs e)
        {
            // Mirem quin item hem clicat
            TreeViewItem itemSeleccionat = tree_Contingut.SelectedItem as TreeViewItem;

            // Si és null o no té Tag (és una carpeta), no fem res
            if (itemSeleccionat == null || itemSeleccionat.Tag == null) return;

            // Recuperem l'objecte sencer
            dynamic dades = itemSeleccionat.Tag;

            var obj = dades.DataObject;
            string type = dades.Type;

            // Naveguem segons el tipus
            switch (type)
            {
                case "Yumedant":
                    // Important: Has de modificar el constructor de PagePersonatge 
                    // per acceptar un ID! -> public PagePersonatge(int id)
                    frmMain.Navigate(new Pages.PagePersonatge((Yumedant)obj));
                    break;

                case "Floor":
                    frmMain.Navigate(new Pages.PageFloor((Floor)obj));
                    break;

                case "Role":
                    frmMain.Navigate(new Pages.PageRole((Playerrole)obj));
                    break;

                case "Action":
                    frmMain.Navigate(new Pages.PageAct((Act)obj));
                    break;

                case "Effect":
                    frmMain.Navigate(new Pages.PageEffect((Effect)obj));
                    break;
            }
        }
    }
}