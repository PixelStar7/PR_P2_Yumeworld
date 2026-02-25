using Microsoft.EntityFrameworkCore;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Effects;
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
        private int actId;

        private AppDbContext _context = new AppDbContext();

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
        public void CarregarArbre()
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



        /// <summary>
        /// Mètode que navegarà a la pàgina de detall corresponent quan fem doble click a un item del TreeView.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void tree_Contingut_MouseDoubleClick(object sender, System.Windows.Input.MouseButtonEventArgs e)
        {
            // 1. Mirem quin item hem clicat al TreeView
            TreeViewItem itemSeleccionat = tree_Contingut.SelectedItem as TreeViewItem;

            // 2. Si és null o no té Tag (per exemple, si és una carpeta arrel), no fem res
            if (itemSeleccionat == null || itemSeleccionat.Tag == null) return;

            // 3. Recuperem l'objecte i el tipus guardats al Tag
            dynamic dades = itemSeleccionat.Tag;
            var obj = dades.DataObject;
            string type = dades.Type;

            // 4. Naveguem segons el tipus, carregant dades fresques del context global (_context)
            switch (type)
            {
                case "Yumedant":
                    var selectedYume = _context.Yumedants.Find(((Yumedant)obj).YuId);
                    if (selectedYume != null) frmMain.Navigate(new Pages.PagePersonatge(selectedYume));
                    break;

                case "Floor":
                    int floorId = ((Floor)obj).FlId;
                    // Incloem el Bioma per si la pàgina necessita mostrar detalls de l'entorn
                    Floor selectedFloor = _context.Floors
                        .Include(f => f.FlBiome).FirstOrDefault(f => f.FlId == floorId);
                    if (selectedFloor != null) frmMain.Navigate(new Pages.PageFloor(selectedFloor));
                    break;

                case "Role":
                    var selectedRole = _context.Playerroles.Find(((Playerrole)obj).PrId);
                    if (selectedRole != null) frmMain.Navigate(new Pages.PageRole(selectedRole));
                    break;

                case "Action":
                    int actId = ((Act)obj).AcId;
                    // Important: Carreguem ActionEffects per saber quins efectes té l'acció
                    Act selectedAction = _context.Acts.Include(a => a.ActionEffects)
                        .FirstOrDefault(a => a.AcId == actId);

                    if (selectedAction != null)
                        frmMain.Navigate(new Pages.PageAct(selectedAction));
                    else
                        MessageBox.Show("L'acció seleccionada ja no existeix a la base de dades.");
                    break;

                case "Effect":
                    int effectId = ((YumeModel.Models.Effect)obj).EfId;
                    // Important: Carreguem ParameterEffects per saber quins paràmetres demana l'efecte
                    YumeModel.Models.Effect selectedEffect = _context.Effects
                        .Include(e => e.ParameterEffects).FirstOrDefault(e => e.EfId == effectId);

                    if (selectedEffect != null)
                        frmMain.Navigate(new Pages.PageEffect(selectedEffect));
                    else
                        MessageBox.Show("L'efecte seleccionat ja no existeix a la base de dades.");
                    break;
            }
        }
    }
}