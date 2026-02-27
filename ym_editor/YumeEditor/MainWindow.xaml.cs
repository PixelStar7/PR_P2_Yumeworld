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

        private AppDbContext _context = new AppDbContext();

        public MainWindow()
        {
            InitializeComponent();
            CarregarArbre();
        }

        private void btnYumedant_Click(object sender, RoutedEventArgs e)
        {
            frmMain.Navigate(new PagePersonatge());
        }

        private void btnAction_Click(object sender, RoutedEventArgs e)
        {
            frmMain.Navigate(new PageAct());

        }

        private void btnEffect_Click(object sender, RoutedEventArgs e)
        {
            frmMain.Navigate(new PageEffect());

        }

        private void btnRole_Click(object sender, RoutedEventArgs e)
        {
            frmMain.Navigate(new PageRole());

        }

        private void btnFloor_Click(object sender, RoutedEventArgs e)
        {
            frmMain.Navigate(new PageFloor());

        }

        private void Window_Loaded(object sender, RoutedEventArgs e)
        {
            if (frmMain != null)
            {
                frmMain.Navigate(new PagePersonatge());
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
                    AfegirItem(nodeRoles, r.PrName, r.PrId, "PlayerRole");
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

            // 3. Recuperem l'ID i el tipus guardats al Tag
            dynamic dades = itemSeleccionat.Tag;

            int id = (int)dades.DataObject;
            string type = dades.Type;

            // 4. Naveguem segons el tipus, carregant dades fresques del context global (_context)
            using (var context = new AppDbContext())
            {
                switch (type)
                {
                    case "Yumedant":
                        // Carreguem el Yumedant amb TOTA la seva descendència (Player, Enemy, Drops, Items, Acts)
                        var selectedYume = context.Yumedants
                            .Include(y => y.Player)
                            .Include(y => y.Enemy)
                                .ThenInclude(e => e.EnemyDrops)
                                .ThenInclude(ed => ed.EdItem)
                                .ThenInclude(i => i.ItAction)
                            .FirstOrDefault(y => y.YuId == id);

                        if (selectedYume != null) frmMain.Navigate(new Pages.PagePersonatge(selectedYume));
                        break;

                    case "Floor":
                        Floor selectedFloor = context.Floors
                            .Include(f => f.FlBiome).FirstOrDefault(f => f.FlId == id);
                        if (selectedFloor != null) frmMain.Navigate(new Pages.PageFloor(selectedFloor));
                        break;

                    case "PlayerRole":
                        // Incloem les habilitats (RaAbilities) i de pas l'acció (AbAction) per saber el nom!
                        var selectedRole = context.Playerroles
                            .Include(pr => pr.RaAbilities)
                                .ThenInclude(ab => ab.AbAction)
                            .FirstOrDefault(pr => pr.PrId == id);
                        if (selectedRole != null) frmMain.Navigate(new Pages.PageRole(selectedRole));
                        break;

                    case "Action":
                        Act selectedAction = context.Acts.Include(a => a.ActionEffects)
                            .FirstOrDefault(a => a.AcId == id);
                        if (selectedAction != null) frmMain.Navigate(new Pages.PageAct(selectedAction));
                        break;

                    case "Effect":
                        YumeModel.Models.Effect selectedEffect = context.Effects
                            .Include(e => e.ParameterEffects).FirstOrDefault(e => e.EfId == id);
                        if (selectedEffect != null) frmMain.Navigate(new Pages.PageEffect(selectedEffect));
                        break;
                }
            }
        }
    }
}