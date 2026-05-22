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

        private void btnBiome_Click(object sender, RoutedEventArgs e)
        {
            frmMain.Navigate(new PageBiome());

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
            TreeViewItem nodeBiomes = CrearCarpeta("Biomes 🌲");

            // INSTANCIEM EL CONTEXT (Aquí passa la màgia d'Entity Framework)
            // El "using" assegura que es tanca la connexió quan acaba de llegir
            using (var context = new AppDbContext())
            {
                // 1. YUMEDANTS 
                // (EF converteix 'yu_id' a 'YuId' i 'yu_name' a 'YuName' automàticament)
                var llistaYumes = context.Yumedants.ToList();
                foreach (Yumedant y in llistaYumes)
                {
                    AfegirItem(nodeYumedants, y.YuName, y.YuId, "Yumedant");
                }

                // 2. BIOMES (ordenats pel camp BiOrder per veure l'ordre amb què el
                //    servidor els distribueix pels pisos)
                var llistaBiomes = context.Biomes.OrderBy(b => b.BiOrder).ToList();
                foreach (var b in llistaBiomes)
                {
                    AfegirItem(nodeBiomes, $"{b.BiOrder} · {b.BiName}", b.BiId, "Biome");
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
            tree_Contingut.Items.Add(nodeBiomes);
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

                    case "Biome":
                        Biome selectedBiome = context.Biomes
                            .Include(b => b.EnemyBiomes)
                                .ThenInclude(eb => eb.EnbiEnemy)
                                .ThenInclude(en => en.EnYumedant)
                            .FirstOrDefault(b => b.BiId == id);
                        if (selectedBiome != null) frmMain.Navigate(new Pages.PageBiome(selectedBiome));
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

        private void btn_DeleteItem_Click(object sender, RoutedEventArgs e)
        {
            TreeViewItem itemSeleccionat = tree_Contingut.SelectedItem as TreeViewItem;

            if (itemSeleccionat == null || itemSeleccionat.Tag == null)
            {
                MessageBox.Show("Selecciona un element vàlid de la llista per eliminar.", "Avís", MessageBoxButton.OK, MessageBoxImage.Warning);
                return;
            }

            // Extraiem les dades primer per saber de quin tipus estem parlant
            dynamic dades = itemSeleccionat.Tag;
            int id = (int)dades.DataObject;
            string type = dades.Type;
            string nomItem = itemSeleccionat.Header.ToString();

            // 1. Preparem un missatge personalitzat i detallat segons l'entitat
            string missatgeDetall = "";

            switch (type)
            {
                case "Yumedant":
                    missatgeDetall = "\n\nS'eliminarà el personatge i TOTA la seva informació:\n" +
                                     "- La seva configuració com a Jugador o Enemic.\n" +
                                     "- La seva llista de botí (Drops) i els pisos on apareix.\n" +
                                     "- Totes les relacions amb accions que hagi après.";
                    break;
                case "Biome":
                    missatgeDetall = "\n\nS'eliminarà aquest bioma i, a més, s'esborrarà:\n" +
                                     "- Tota la llista d'enemics que apareixien aquí.";
                    break;
                case "PlayerRole":
                    missatgeDetall = "\n\nS'eliminarà aquest rol i es desvincularan totes les seves habilitats.\n\n" +
                                     "⚠️ Nota: L'operació es cancel·larà si hi ha algun Jugador que actualment tingui aquest rol assignat.";
                    break;
                case "Action":
                    missatgeDetall = "\n\nS'eliminarà aquesta acció i, a més, s'esborrarà:\n" +
                                     "- La seva informació específica com a Habilitat o Ítem.\n" +
                                     "- S'esborrarà de l'inventari/habilitats de tots els Yumedants.\n" +
                                     "- S'eliminaran automàticament els seus efectes i paràmetres (via Trigger de BD).";
                    break;
                case "Effect":
                    missatgeDetall = "\n\nS'eliminarà aquest Efecte base.\n\n" +
                                     "⚠️ Nota: L'operació es cancel·larà si hi ha alguna Acció que l'estigui utilitzant per fer mal, curar, etc.";
                    break;
            }

            // 2. Mostrem el MessageBox amb el missatge dinàmic
            MessageBoxResult confirmacio = MessageBox.Show(
                $"Estàs segur que vols eliminar '{nomItem}'?{missatgeDetall}\n\nAquesta acció no es pot desfer.",
                "Confirmar Eliminació",
                MessageBoxButton.YesNo,
                MessageBoxImage.Warning);

            if (confirmacio != MessageBoxResult.Yes) return;

            // 3. Si diu que SÍ, executem la mateixa lògica d'esborrat segura d'abans
            try
            {
                using (var context = new AppDbContext())
                {
                    switch (type)
                    {
                        case "Yumedant":
                            var yume = context.Yumedants
                                .Include(y => y.YaActions)
                                .Include(y => y.Player)
                                .Include(y => y.Enemy).ThenInclude(en => en.EnemyDrops)
                                .Include(y => y.Enemy).ThenInclude(en => en.EnemyBiomes)
                                .FirstOrDefault(y => y.YuId == id);

                            if (yume != null)
                            {
                                yume.YaActions.Clear();

                                if (yume.Player != null) context.Players.Remove(yume.Player);

                                if (yume.Enemy != null)
                                {
                                    context.EnemyDrops.RemoveRange(yume.Enemy.EnemyDrops);
                                    context.EnemyBiomes.RemoveRange(yume.Enemy.EnemyBiomes);
                                    context.Enemies.Remove(yume.Enemy);
                                }
                                context.Yumedants.Remove(yume);
                            }
                            break;

                        case "Biome":
                            var biome = context.Biomes
                                .Include(b => b.EnemyBiomes)
                                .FirstOrDefault(b => b.BiId == id);

                            if (biome != null)
                            {
                                context.EnemyBiomes.RemoveRange(biome.EnemyBiomes);
                                context.Biomes.Remove(biome);
                            }
                            break;

                        case "PlayerRole":
                            var role = context.Playerroles
                                .Include(r => r.RaAbilities)
                                .Include(r => r.Players)
                                .FirstOrDefault(r => r.PrId == id);

                            if (role != null)
                            {
                                if (role.Players.Any())
                                    throw new Exception("Hi ha jugadors que tenen aquest rol assignat. Canvia el seu rol abans d'eliminar-lo.");

                                role.RaAbilities.Clear();
                                context.Playerroles.Remove(role);
                            }
                            break;

                        case "Action":
                            var action = context.Acts
                                .Include(a => a.Ability)
                                .Include(a => a.Item)
                                .Include(a => a.YaYumedants)
                                .FirstOrDefault(a => a.AcId == id);

                            if (action != null)
                            {
                                action.YaYumedants.Clear();

                                if (action.Ability != null) context.Abilities.Remove(action.Ability);
                                if (action.Item != null) context.Items.Remove(action.Item);

                                context.Acts.Remove(action);
                            }
                            break;

                        case "Effect":
                            var effect = context.Effects
                                .Include(e => e.ActionEffects)
                                .Include(e => e.ParameterEffects)
                                .FirstOrDefault(e => e.EfId == id);

                            if (effect != null)
                            {
                                if (effect.ActionEffects.Any() || effect.ParameterEffects.Any())
                                    throw new Exception("Aquest efecte s'està utilitzant en alguna Acció o Paràmetre i no es pot esborrar.");

                                context.Effects.Remove(effect);
                            }
                            break;
                    }

                    context.SaveChanges();
                }

                MessageBox.Show("L'element s'ha eliminat correctament.", "Èxit", MessageBoxButton.OK, MessageBoxImage.Information);

                CarregarArbre();
                frmMain.Content = null;
            }
            catch (Exception ex)
            {
                MessageBox.Show($"No s'ha pogut eliminar l'element.\n\nDetall: {ex.InnerException?.Message ?? ex.Message}",
                                "Error d'Eliminació",
                                MessageBoxButton.OK,
                                MessageBoxImage.Error);
            }
        }
    }
}