using Microsoft.Win32;
using System;
using System.Collections.Generic;
using System.Linq; // Necesario para OrderBy en GetClosestColor
using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Imaging;

namespace ProvesProjecte2
{
    public partial class MainWindow : Window
    {
        private BitmapSource _originalImage;
        private int _pixelSize = 10;
        private List<Color> _palette; // Recuperamos la lista de colores

        public MainWindow()
        {
            InitializeComponent();
            InitializePalette();
            UpdateCounterDisplay();
        }

        private void InitializePalette()
        {
            // Paleta estándar de 16 colores (puedes añadir o quitar los que quieras)
            _palette = new List<Color>
            {
                Colors.Black, Colors.White, Colors.Gray, Colors.DarkGray,
                Colors.Red, Colors.DarkRed, Colors.Green, Colors.DarkGreen,
                Colors.Blue, Colors.DarkBlue, Colors.Cyan, Colors.DarkCyan,
                Colors.Magenta, Colors.DarkMagenta, Colors.Yellow, Colors.Orange
            };
        }

        // --- GESTIÓN DEL CONTADOR ---
        private void BtnMas_Click(object sender, RoutedEventArgs e)
        {
            if (_pixelSize < 200) { _pixelSize++; UpdateCounterDisplay(); }
        }

        private void BtnMenos_Click(object sender, RoutedEventArgs e)
        {
            if (_pixelSize > 2) { _pixelSize--; UpdateCounterDisplay(); }
        }

        private void UpdateCounterDisplay()
        {
            TxtPixelSize.Text = _pixelSize.ToString();
        }

        // --- IMPORTAR ---
        private void BtnImportar_Click(object sender, RoutedEventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog();
            openFileDialog.Filter = "Imágenes|*.jpg;*.jpeg;*.png;*.bmp";

            if (openFileDialog.ShowDialog() == true)
            {
                // Cargamos como Bgra32 para asegurar compatibilidad con la manipulación de bytes
                BitmapImage bitmap = new BitmapImage();
                bitmap.BeginInit();
                bitmap.UriSource = new Uri(openFileDialog.FileName);
                bitmap.CacheOption = BitmapCacheOption.OnLoad;
                bitmap.EndInit();

                _originalImage = new FormatConvertedBitmap(bitmap, PixelFormats.Bgra32, null, 0);
                PixelArtDisplay.Source = _originalImage;
            }
        }

        // --- LÓGICA PRINCIPAL ---
        private void BtnPixelar_Click(object sender, RoutedEventArgs e)
        {
            if (_originalImage == null) return;

            // 1. Reducir tamaño (Geometría)
            BitmapSource result = ResizeImage(_originalImage, _pixelSize);

            // 2. Reducir colores (Estética) - Solo si el checkbox está marcado
            if (CheckPalette.IsChecked == true)
            {
                result = ApplyPalette(result, _palette);
            }

            PixelArtDisplay.Source = result;
        }

        // Función A: Redimensionar (Pixelado geométrico)
        private BitmapSource ResizeImage(BitmapSource source, int pixelSize)
        {
            int newWidth = Math.Max(1, (int)(source.PixelWidth / pixelSize));
            int newHeight = Math.Max(1, (int)(source.PixelHeight / pixelSize));

            return new TransformedBitmap(source,
                new ScaleTransform((double)newWidth / source.PixelWidth,
                                   (double)newHeight / source.PixelHeight));
        }

        // Función B: Aplicar Paleta (Recuperada)
        private BitmapSource ApplyPalette(BitmapSource source, List<Color> palette)
        {
            if (source.Format != PixelFormats.Bgra32)
                source = new FormatConvertedBitmap(source, PixelFormats.Bgra32, null, 0);

            int width = source.PixelWidth;
            int height = source.PixelHeight;
            int stride = width * 4;
            byte[] pixelData = new byte[height * stride];

            source.CopyPixels(pixelData, stride, 0);

            for (int i = 0; i < pixelData.Length; i += 4)
            {
                // Leemos el color original del píxel
                byte b = pixelData[i];
                byte g = pixelData[i + 1];
                byte r = pixelData[i + 2];

                Color current = Color.FromRgb(r, g, b);

                // Buscamos el más parecido en nuestra lista
                Color match = GetClosestColor(current, palette);

                // Sobrescribimos
                pixelData[i] = match.B;
                pixelData[i + 1] = match.G;
                pixelData[i + 2] = match.R;
            }

            return BitmapSource.Create(width, height, 96, 96, PixelFormats.Bgra32, null, pixelData, stride);
        }

        // Función C: Buscar color cercano (Recuperada)
        private Color GetClosestColor(Color pixelColor, List<Color> palette)
        {
            return palette.OrderBy(c =>
                Math.Pow(c.R - pixelColor.R, 2) +
                Math.Pow(c.G - pixelColor.G, 2) +
                Math.Pow(c.B - pixelColor.B, 2))
                .First();
        }
    }
}