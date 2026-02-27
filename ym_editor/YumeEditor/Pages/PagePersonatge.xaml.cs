using Microsoft.EntityFrameworkCore;
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
using YumeEditor.Utils;
using YumeModel.Models;

namespace YumeEditor.Pages
{
    /// <summary>
    /// Lógica de interacción para PagePersonatge.xaml
    /// </summary>
    public partial class PagePersonatge : Page
    {
        private Yumedant _yumeActual;

        // Llista temporal per gestionar els drops a la memòria abans de guardar a la BD
        private List<DropHelper> _dropsTemporal = new List<DropHelper>();

        public PagePersonatge(Yumedant yumedant = null)
        {
            InitializeComponent();

            // 1. Carreguem els ComboBox (Això es queda igual)
            CarregarComboBoxs();

            // 2. Assignem l'objecte que ens passa el MainWindow
            _yumeActual = yumedant;

            if (_yumeActual != null)
            {
                // --- MODO EDITAR ---
                carregarDades(); // Carrega les dades que venen de l'objecte
            }
            else
            {
                // --- MODO CREAR NOU ---
                _yumeActual = new Yumedant
                {
                    YuLevel = 1,
                    YuHp = 1,
                    YuMp = 1,
                    YuAtk = 1,
                    YuDef = 1
                };
                ActualitzarCampsText();
                rbPlayer.IsChecked = true;
                ActualitzarPanells();
            }
        }

        private void carregarDades()
        {
            ActualitzarCampsText();

            // Imatge
            if (!string.IsNullOrEmpty(_yumeActual.YuImage))
                MostrarImatge(_yumeActual.YuImage);

            // Comprovem si és Jugador o Enemic
            if (_yumeActual.Player != null)
            {
                rbPlayer.IsChecked = true;
                txtPlayerExp.Text = _yumeActual.Player.PlExp.ToString();
                cbRole.SelectedValue = _yumeActual.Player.PlRoleId;
            }
            else if (_yumeActual.Enemy != null)
            {
                rbEnemy.IsChecked = true;
                txtGivenExp.Text = _yumeActual.Enemy.EnGivenExp.ToString();

                // Carreguem els drops a la llista temporal
                _dropsTemporal.Clear();
                foreach (var drop in _yumeActual.Enemy.EnemyDrops)
                {
                    _dropsTemporal.Add(new DropHelper
                    {
                        ItemId = drop.EdItemId,
                        NomItem = drop.EdItem.ItAction.AcName,
                        Chance = drop.EdChance
                    });
                }
                RefrescarLlistaDrops();
            }

            ActualitzarPanells();
        }

        // ==========================================
        // CÀRREGA DE COMBOBOXS
        // ==========================================
        private void CarregarComboBoxs()
        {
            using (var context = new AppDbContext())
            {
                // Carregar Rols (Playerrole)
                var rols = context.Playerroles.ToList();
                cbRole.ItemsSource = rols;
                cbRole.DisplayMemberPath = "PrName";
                cbRole.SelectedValuePath = "PrId";

                // Carregar Ítems (Hem de fer Include a ItAction per saber el nom de l'ítem)
                var items = context.Items.Include(i => i.ItAction).ToList();
                cbItem.ItemsSource = items;
                cbItem.DisplayMemberPath = "ItAction.AcName";
                cbItem.SelectedValuePath = "ItActionId";
            }
        }

        private void ActualitzarCampsText()
        {
            txtNom.Text = _yumeActual.YuName;
            txtDesc.Text = _yumeActual.YuDesc;
            txtLevel.Text = _yumeActual.YuLevel.ToString();

            // Attributes
            txtHP.Text = _yumeActual.YuHp.ToString();
            txtMP.Text = _yumeActual.YuMp.ToString();
            txtAtk.Text = _yumeActual.YuAtk.ToString();
            txtDef.Text = _yumeActual.YuDef.ToString();
        }


        // ==========================================
        // UI & INTERACCIÓ (Panells i Imatge)
        // ==========================================
        private void rbType_Click(object sender, RoutedEventArgs e)
        {
            ActualitzarPanells();
        }

        private void ActualitzarPanells()
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


        private void btnUpload_Click(object sender, RoutedEventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog();
            openFileDialog.Filter = "Image Files|*.jpg;*.jpeg;*.png;*.bmp|All Files|*.*";
            openFileDialog.Title = "Selecciona una imatge per al Yumedant";

            if (openFileDialog.ShowDialog() == true)
            {
                _yumeActual.YuImage = openFileDialog.FileName;
                MostrarImatge(openFileDialog.FileName);
            }
        }

        private void MostrarImatge(string ruta)
        {
            try
            {
                BitmapImage bitmap = new BitmapImage(new Uri(ruta, UriKind.Absolute));
                imgYumedant.Source = bitmap;
            }
            catch (Exception ex)
            {
                MessageBox.Show($"No s'ha pogut carregar la imatge: {ex.Message}");
            }
        }


        // ==========================================
        // GESTIÓ DE BOTÍ (DROPS)
        // ==========================================
        private void btnAddItem_Click(object sender, RoutedEventArgs e)
        {
            if (cbItem.SelectedValue == null) return;

            int idItemSeleccionat = (int)cbItem.SelectedValue;
            string nomItem = cbItem.Text; // El text que es veu al ComboBox

            // Netejar el "%" i convertir a float (ex: "10%" -> 10 -> 0.10f)
            string textPercent = txtPercentatge.Text.Replace("%", "").Trim();
            if (float.TryParse(textPercent, out float percentNum))
            {
                float probabilitat = percentNum / 100f;

                // Evitar duplicats a la llista
                if (_dropsTemporal.Any(d => d.ItemId == idItemSeleccionat))
                {
                    MessageBox.Show("Aquest ítem ja està a la llista de botí.");
                    return;
                }

                _dropsTemporal.Add(new DropHelper { ItemId = idItemSeleccionat, NomItem = nomItem, Chance = probabilitat });
                RefrescarLlistaDrops();
            }
            else
            {
                MessageBox.Show("El format del percentatge no és vàlid.");
            }
        }

        private void btnRemoveItem_Click(object sender, RoutedEventArgs e)
        {
            if (lsbItemsDrop.SelectedItem is DropHelper dropSeleccionat)
            {
                _dropsTemporal.Remove(dropSeleccionat);
                RefrescarLlistaDrops();
            }
        }

        private void RefrescarLlistaDrops()
        {
            lsbItemsDrop.ItemsSource = null;
            lsbItemsDrop.ItemsSource = _dropsTemporal;
        }

        // ==========================================
        // GUARDAR DADES
        // ==========================================
        private void btnSaveYumedant_Click(object sender, RoutedEventArgs e)
        {
            if (!ValidarCamps()) return;

            using (var context = new AppDbContext())
            {
                Yumedant trackYume;

                if (_yumeActual.YuId == 0)
                {
                    // --- NOU ---
                    trackYume = new Yumedant();
                    context.Yumedants.Add(trackYume);
                }
                else
                {
                    // --- MODIFICAR ---
                    // Carreguem l'entitat amb el Player i Enemy per poder-los modificar o esborrar de forma segura
                    trackYume = context.Yumedants
                        .Include(y => y.Player)
                        .Include(y => y.Enemy).ThenInclude(en => en.EnemyDrops)
                        .FirstOrDefault(y => y.YuId == _yumeActual.YuId);
                }

                if (trackYume != null)
                {
                    // 1. Guardar Dades Base de Yumedant
                    trackYume.YuName = txtNom.Text;
                    trackYume.YuDesc = txtDesc.Text;
                    trackYume.YuLevel = int.Parse(txtLevel.Text);
                    trackYume.YuHp = int.Parse(txtHP.Text);
                    trackYume.YuMp = int.Parse(txtMP.Text);
                    trackYume.YuAtk = int.Parse(txtAtk.Text);
                    trackYume.YuDef = int.Parse(txtDef.Text);
                    trackYume.YuImage = _yumeActual.YuImage;

                    // 2. Gestionar Herència (Jugador vs Enemic)
                    if (rbPlayer.IsChecked == true)
                    {
                        // Si abans era Enemic, l'esborrem de la BD
                        if (trackYume.Enemy != null)
                        {
                            context.Enemies.Remove(trackYume.Enemy);
                            trackYume.Enemy = null;
                        }

                        // Creem o actualitzem el Jugador
                        if (trackYume.Player == null) trackYume.Player = new Player();

                        trackYume.Player.PlExp = int.Parse(txtPlayerExp.Text);
                        trackYume.Player.PlRoleId = (int?)cbRole.SelectedValue;
                    }
                    else // rbEnemy.IsChecked == true
                    {
                        // Si abans era Jugador, l'esborrem de la BD
                        if (trackYume.Player != null)
                        {
                            context.Players.Remove(trackYume.Player);
                            trackYume.Player = null;
                        }

                        // Creem o actualitzem l'Enemic
                        if (trackYume.Enemy == null) trackYume.Enemy = new Enemy();

                        trackYume.Enemy.EnGivenExp = int.Parse(txtGivenExp.Text);

                        // Gestionar Botí (EnemyDrops)
                        // La manera més neta a EF és esborrar els vells i posar els nous
                        if (trackYume.Enemy.EnemyDrops != null)
                        {
                            context.EnemyDrops.RemoveRange(trackYume.Enemy.EnemyDrops);
                        }

                        trackYume.Enemy.EnemyDrops = new List<EnemyDrop>();
                        foreach (var drop in _dropsTemporal)
                        {
                            trackYume.Enemy.EnemyDrops.Add(new EnemyDrop
                            {
                                EdItemId = drop.ItemId,
                                EdChance = drop.Chance
                            });
                        }
                    }

                    // Fem el Commit a MySQL i actualitzem la UI
                    context.SaveChanges();
                    MessageBox.Show("S'ha guardat el Yumedant correctament!", "Èxit", MessageBoxButton.OK, MessageBoxImage.Information);
                    TreeUtils.actualitzarTree();
                }
            }
        }

        // ==========================================
        // VALIDACIONS
        // ==========================================
        private bool ValidarCamps()
        {
            if (string.IsNullOrWhiteSpace(txtNom.Text)) { MessageBox.Show("Falta el nom."); return false; }
            if (!int.TryParse(txtLevel.Text, out _)) { MessageBox.Show("Nivell invàlid."); return false; }
            if (!int.TryParse(txtHP.Text, out _)) { MessageBox.Show("HP invàlid."); return false; }
            if (!int.TryParse(txtMP.Text, out _)) { MessageBox.Show("MP invàlid."); return false; }
            if (!int.TryParse(txtAtk.Text, out _)) { MessageBox.Show("ATK invàlid."); return false; }
            if (!int.TryParse(txtDef.Text, out _)) { MessageBox.Show("DEF invàlid."); return false; }

            if (rbPlayer.IsChecked == true)
            {
                if (!int.TryParse(txtPlayerExp.Text, out _)) { MessageBox.Show("Exp actual invàlida."); return false; }
            }
            else
            {
                if (!int.TryParse(txtGivenExp.Text, out _)) { MessageBox.Show("Exp donada invàlida."); return false; }
            }

            return true;
        }
    }

    // ==========================================
    // CLASSE AUXILIAR PELS DROPS A LA LLISTA
    // ==========================================
    public class DropHelper
    {
        public int ItemId { get; set; }
        public string NomItem { get; set; }
        public float Chance { get; set; }

        // Aquest mètode controla què es llegeix visualment a la ListBox
        public override string ToString()
        {
            return $"{NomItem} - {Chance * 100}% de probabilitat";
        }
    }
}
