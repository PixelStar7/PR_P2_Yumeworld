using Microsoft.EntityFrameworkCore;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Imaging;
using YumeModel.Models;
using YumeEditor.Utils;

namespace YumeEditor.Pages
{
    /// <summary>
    /// Lógica de interacción para PageBiome.xaml
    /// </summary>
    public partial class PageBiome : Page
    {
        // Estat actual de la pàgina
        private Biome _biomaActual;

        // Llista temporal d'enemics que apareixeran al bioma (abans de guardar a la BD)
        private List<EnemyBiomeHelper> _enemicsTemporal = new List<EnemyBiomeHelper>();

        public PageBiome(Biome? bioma = null)
        {
            InitializeComponent();

            CarregarComboEnemics();

            if (bioma != null)
            {
                // MODIFICACIÓ
                _biomaActual = bioma;
                CarregarDades();
            }
            else
            {
                // INSERCIÓ NOVA
                _biomaActual = new Biome { BiOrder = 0 };
                txtOrdre.Text = "0";
            }
        }

        // ==========================================
        // CÀRREGA DE DADES
        // ==========================================
        private void CarregarComboEnemics()
        {
            using (var context = new AppDbContext())
            {
                // Volem mostrar el nom del Yumedant darrere de cada Enemy
                var enemics = context.Enemies
                    .Include(en => en.EnYumedant)
                    .ToList();

                cbEnemy.ItemsSource = enemics;
                cbEnemy.DisplayMemberPath = "EnYumedant.YuName";
                cbEnemy.SelectedValuePath = "EnYumedantId";
            }
        }

        private void CarregarDades()
        {
            txtNom.Text = _biomaActual.BiName;
            txtOrdre.Text = _biomaActual.BiOrder.ToString();

            if (!string.IsNullOrEmpty(_biomaActual.BiBackground))
            {
                txtBackgroundUrl.Text = _biomaActual.BiBackground;
                MostrarImatge(_biomaActual.BiBackground);
            }

            // Carreguem la llista temporal d'enemics
            _enemicsTemporal.Clear();
            foreach (var eb in _biomaActual.EnemyBiomes)
            {
                _enemicsTemporal.Add(new EnemyBiomeHelper
                {
                    EnemyId = eb.EnbiEnemyId,
                    NomEnemic = eb.EnbiEnemy?.EnYumedant?.YuName ?? $"Enemic #{eb.EnbiEnemyId}",
                    Quantitat = eb.EnbiQuantity
                });
            }
            RefrescarLlistaEnemics();
        }

        // ==========================================
        // IMATGE
        // ==========================================
        private void btnPreviewImg_Click(object sender, RoutedEventArgs e)
        {
            string url = txtBackgroundUrl.Text.Trim();
            if (string.IsNullOrWhiteSpace(url)) return;

            _biomaActual.BiBackground = url;
            MostrarImatge(url);
        }

        private void MostrarImatge(string ruta)
        {
            try
            {
                BitmapImage bitmap = new BitmapImage(new Uri(ruta, UriKind.Absolute));
                imgBackground.Source = bitmap;
            }
            catch (Exception ex)
            {
                MessageBox.Show($"No s'ha pogut carregar la imatge: {ex.Message}");
            }
        }

        // ==========================================
        // ENEMICS AL BIOMA
        // ==========================================
        private void btnAddEnemy_Click(object sender, RoutedEventArgs e)
        {
            if (cbEnemy.SelectedValue == null)
            {
                MessageBox.Show("Selecciona un enemic.");
                return;
            }

            if (!int.TryParse(txtQuantitat.Text, out int qty) || qty <= 0)
            {
                MessageBox.Show("La quantitat ha de ser un número superior a 0.");
                return;
            }

            int idEnemic = (int)cbEnemy.SelectedValue;
            string nomEnemic = cbEnemy.Text;

            // Si ja existia, actualitzem la quantitat
            var existent = _enemicsTemporal.FirstOrDefault(en => en.EnemyId == idEnemic);
            if (existent != null)
            {
                existent.Quantitat = qty;
            }
            else
            {
                _enemicsTemporal.Add(new EnemyBiomeHelper
                {
                    EnemyId = idEnemic,
                    NomEnemic = nomEnemic,
                    Quantitat = qty
                });
            }

            RefrescarLlistaEnemics();
        }

        private void btnRemoveEnemy_Click(object sender, RoutedEventArgs e)
        {
            if (lsbEnemics.SelectedItem is EnemyBiomeHelper sel)
            {
                _enemicsTemporal.Remove(sel);
                RefrescarLlistaEnemics();
            }
        }

        private void RefrescarLlistaEnemics()
        {
            lsbEnemics.ItemsSource = null;
            lsbEnemics.ItemsSource = _enemicsTemporal;
        }

        // ==========================================
        // GUARDAR
        // ==========================================
        private void btnSaveBiome_Click(object sender, RoutedEventArgs e)
        {
            if (!ValidarCamps()) return;

            using (var context = new AppDbContext())
            {
                Biome trackBiome;

                if (_biomaActual.BiId == 0)
                {
                    // --- NOU ---
                    trackBiome = new Biome();
                    context.Biomes.Add(trackBiome);
                }
                else
                {
                    // --- MODIFICAR ---
                    trackBiome = context.Biomes
                        .Include(b => b.EnemyBiomes)
                        .FirstOrDefault(b => b.BiId == _biomaActual.BiId);

                    if (trackBiome == null)
                    {
                        MessageBox.Show("No s'ha trobat el bioma a la BD.");
                        return;
                    }
                }

                // 1. Camps bàsics
                trackBiome.BiName = txtNom.Text.Trim();
                trackBiome.BiOrder = int.Parse(txtOrdre.Text);
                trackBiome.BiBackground = string.IsNullOrWhiteSpace(txtBackgroundUrl.Text)
                                            ? null
                                            : txtBackgroundUrl.Text.Trim();

                // 2. Refer la llista d'enemics del bioma
                if (trackBiome.EnemyBiomes != null && trackBiome.EnemyBiomes.Count > 0)
                {
                    context.EnemyBiomes.RemoveRange(trackBiome.EnemyBiomes);
                }

                trackBiome.EnemyBiomes = new List<EnemyBiome>();
                foreach (var en in _enemicsTemporal)
                {
                    trackBiome.EnemyBiomes.Add(new EnemyBiome
                    {
                        EnbiEnemyId = en.EnemyId,
                        EnbiQuantity = en.Quantitat
                    });
                }

                try
                {
                    context.SaveChanges();
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Error guardant: {ex.InnerException?.Message ?? ex.Message}",
                                    "Error", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }
            }

            MessageBox.Show("S'ha guardat el Bioma correctament!", "Èxit",
                            MessageBoxButton.OK, MessageBoxImage.Information);

            TreeUtils.actualitzarTree();
        }

        // ==========================================
        // VALIDACIONS
        // ==========================================
        private bool ValidarCamps()
        {
            if (string.IsNullOrWhiteSpace(txtNom.Text))
            {
                MessageBox.Show("El nom del bioma no pot estar buit.", "Atenció",
                                MessageBoxButton.OK, MessageBoxImage.Warning);
                return false;
            }

            if (!int.TryParse(txtOrdre.Text, out int ord) || ord < 0)
            {
                MessageBox.Show("L'ordre ha de ser un número enter més gran o igual a 0.",
                                "Atenció", MessageBoxButton.OK, MessageBoxImage.Warning);
                return false;
            }

            return true;
        }
    }
}
