using Microsoft.EntityFrameworkCore;
using Microsoft.Win32;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Imaging;
using YumeModel.Models;
using YumeEditor.Utils;

namespace YumeEditor.Pages
{
    /// <summary>
    /// Lógica de interacción para PageFloor.xaml
    /// </summary>
    public partial class PageFloor : Page
    {
        // Variables globals per guardar l'estat actual de la pàgina
        private Floor _floorActual;
        private Biome _biomaActual;

        public PageFloor(Floor floor = null)
        {
            InitializeComponent();

            _floorActual = floor;

            if (_floorActual != null)
            {
                // MODIFICACIÓ
                carregarDades();
            }
            else
            {
                // INSERCIÓ NOU FLOOR
                _floorActual = new Floor();
                _biomaActual = new Biome();
            }
        }

        private void carregarDades()
        {
            _biomaActual = _floorActual.FlBiome ?? new Biome();

            txtNom.Text = _floorActual.FlName;
            txtNumPis.Text = _floorActual.FlNum.ToString();

            if (!string.IsNullOrEmpty(_biomaActual.BiBackground))
            {
                MostrarImatge(_biomaActual.BiBackground);
            }
        }

        private void btnUpload_Click(object sender, RoutedEventArgs e)
        {
            // Creem un diàleg per obrir fitxers
            OpenFileDialog openFileDialog = new OpenFileDialog();
            openFileDialog.Filter = "Image Files|*.jpg;*.jpeg;*.png;*.bmp|All Files|*.*";
            openFileDialog.Title = "Selecciona una imatge per al fons (Background)";

            // Si l'usuari selecciona un fitxer
            if (openFileDialog.ShowDialog() == true)
            {
                // Ho guardem a la memòria
                if (_biomaActual == null) _biomaActual = new Biome();
                _biomaActual.BiBackground = openFileDialog.FileName;

                // Ho mostrem per pantalla
                MostrarImatge(openFileDialog.FileName);
            }
        }

        private void MostrarImatge(string ruta)
        {
            try
            {
                // Creem un BitmapImage amb la ruta per poder mostrar-ho al requadre
                BitmapImage bitmap = new BitmapImage(new Uri(ruta, UriKind.Absolute));
                imgBackground.Source = bitmap;
            }
            catch (Exception ex)
            {
                // Si la ruta és invàlida o la imatge s'ha esborrat de l'ordinador
                MessageBox.Show($"No s'ha pogut carregar la imatge: {ex.Message}");
            }
        }

        private void btnSaveFloor_Click(object sender, RoutedEventArgs e)
        {
            // Validem que les dades introduïdes estiguin bé i les guardem a la BD
            if (validarCamps())
            {
                guardarDades();
            }
        }

        private void guardarDades()
        {
            using (var context = new AppDbContext())
            {
                if (_floorActual.FlId == 0)
                {
                    // INSERIR NOU PIS
                    _floorActual.FlName = txtNom.Text;
                    _floorActual.FlNum = int.Parse(txtNumPis.Text);
                    _floorActual.FlBiome = _biomaActual;

                    context.Biomes.Add(_biomaActual);
                    context.Floors.Add(_floorActual);
                }
                else
                {
                    // MODIFICAR PIS EXISTENT
                    var trackFloor = context.Floors
                                            .Include(f => f.FlBiome)
                                            .FirstOrDefault(f => f.FlId == _floorActual.FlId);

                    if (trackFloor != null)
                    {
                        trackFloor.FlName = txtNom.Text;
                        trackFloor.FlNum = int.Parse(txtNumPis.Text);

                        if (trackFloor.FlBiome != null)
                        {
                            trackFloor.FlBiome.BiBackground = _biomaActual.BiBackground;
                        }
                        else
                        {
                            trackFloor.FlBiome = _biomaActual;
                        }
                    }
                }

                // Desem a la base de dades
                context.SaveChanges();
            } // El context es tanca aquí (bona pràctica)

            MessageBox.Show("S'ha guardat el Pis i el seu Bioma correctament!", "Èxit", MessageBoxButton.OK, MessageBoxImage.Information);

            // ========================================================
            // LA MÀGIA: ACTUALITZAR L'ARBRE DEL MAIN WINDOW
            // ========================================================
            // Busquem quina és la finestra principal de l'aplicació i la tractem com a 'MainWindow'
            TreeUtils.actualitzarTree();
        }

        private bool validarCamps()
        {
            // Validem que el nom no estigui buit
            if (string.IsNullOrWhiteSpace(txtNom.Text))
            {
                MessageBox.Show("El nom del pis no pot estar buit.", "Atenció", MessageBoxButton.OK, MessageBoxImage.Warning);
                return false;
            }

            // Validem que el número de pis sigui de veritat un número
            if (!int.TryParse(txtNumPis.Text, out _))
            {
                MessageBox.Show("El número de pis ha de ser un valor numèric vàlid.", "Atenció", MessageBoxButton.OK, MessageBoxImage.Warning);
                return false;
            }

            return true;
        }
    }
}
