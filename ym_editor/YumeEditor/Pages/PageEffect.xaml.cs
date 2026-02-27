using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using YumeModel.Models;

namespace YumeEditor.Pages
{
    /// <summary>
    /// Lògica d'interacció per a la pàgina de gestió d'efectes (PageEffect.xaml).
    /// Aquesta pàgina permet la definició de nous efectes de joc i la configuració
    /// dels tipus de paràmetres que cada efecte requereix.
    /// </summary>
    public partial class PageEffect : Page
    {
        // Instància del context de la base de dades per a operacions amb Entity Framework
        private AppDbContext _context = new AppDbContext();

        // Variable de control d'estat: 
        // - Si és 'null', estem creant un efecte nou (INSERT).
        // - Si té un objecte assignat, estem modificant un existent (UPDATE).
        private Effect? _editedEffect = null;

        public PageEffect(Effect? effect=null)

        {
            InitializeComponent();
            CarregarParametres();
            _editedEffect= effect;
            this.DataContext = this;
            if (_editedEffect != null)
            {
                // Si estem en mode edició, omplim els camps amb les dades de l'efecte seleccionat
                txtNom.Text = _editedEffect.EfName;
                // Marquem els CheckBoxes corresponents als paràmetres associats a aquest efecte
                var parametresAssociats = _editedEffect.ParameterEffects.Select(pe => pe.PeParametertypeId).ToHashSet();
                foreach (var item in icParameters.Items)
                {
                    var container = icParameters.ItemContainerGenerator.ContainerFromItem(item) as FrameworkElement;
                    if (container != null)
                    {
                        CheckBox cb = FindVisualChild<CheckBox>(container);
                        if (cb != null)
                        {
                            var paramType = (ParameterType)item;
                            cb.IsChecked = parametresAssociats.Contains(paramType.PtId);
                        }
                    }
                }
            }
           
        }

        private void CarregarDadesEffect()
        {
            txtId.Text = _editedEffect.EfId.ToString();
            txtNom.Text = _editedEffect.EfName;
            var parametresAssociats = _editedEffect.ParameterEffects.Select(pe => pe.PeParametertypeId).ToHashSet();
            foreach (var item in icParameters.Items)
            {
                var container = icParameters.ItemContainerGenerator.ContainerFromItem(item) as FrameworkElement;
                if (container != null)
                {
                    CheckBox cb = FindVisualChild<CheckBox>(container);
                    if (cb != null)
                    {
                        var paramType = (ParameterType)item;
                        cb.IsChecked = parametresAssociats.Contains(paramType.PtId);
                    }
                }
            }
        }

        /// <summary>
        /// Carrega dinàmicament tots els tipus de paràmetres definits a la base de dades.
        /// Omple l'ItemsControl de la interfície per permetre la selecció múltiple.
        /// </summary>
        private void CarregarParametres()
        {
            try
            {
                // Obtenim el llistat des de la taula 'parameter_types'
                List<ParameterType> parameterTypes = _context.ParameterTypes.ToList();
                System.Diagnostics.Debug.WriteLine($"S'han carregat {parameterTypes.Count} paràmetres.");
                icParameters.ItemsSource = parameterTypes;
            }
            catch (Exception ex)
            {
                // Gestió d'errors en cas de fallada de connexió amb el servidor MySQL
                MessageBox.Show(ex.Message, "Error en carregar paràmetres", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        /// <summary>
        /// Executa la lògica de persistència de dades en prémer el botó de guardar.
        /// Gestiona tant l'entitat principal (Effect) com la relació N:M (ParameterEffect).
        /// </summary>
        private void btnSave_Click(object sender, RoutedEventArgs e)
        {
            if (string.IsNullOrWhiteSpace(txtNom.Text)) return;

            try
            {
                // 1. GESTIÓ DE L'ENTITAT PRINCIPAL
                if (_editedEffect == null)
                {
                    _editedEffect = new Effect { EfName = txtNom.Text };
                    _context.Effects.Add(_editedEffect);
                }
                else
                {
                    // Forzamos que el contexto sepa que este objeto existente ha cambiado
                    _editedEffect.EfName = txtNom.Text;
                    _context.Entry(_editedEffect).State = Microsoft.EntityFrameworkCore.EntityState.Modified;
                }

                // Guardamos el nombre PRIMERO
                _context.SaveChanges();

                // 2. GESTIÓ DE RELACIONS (PARAMETER_EFFECT)
                var relacionsAntigues = _context.ParameterEffects
                    .Where(pe => pe.PeEffectId == _editedEffect.EfId).ToList(); // Añade .ToList() para evitar errores de lectura

                _context.ParameterEffects.RemoveRange(relacionsAntigues);
                _context.SaveChanges(); // Limpiamos relaciones antiguas antes de añadir nuevas

                // 3. PROCESSAMENT DE LA INTERFÍCIE DINÀMICA
                foreach (var item in icParameters.Items)
                {
                    var container = icParameters.ItemContainerGenerator.ContainerFromItem(item) as FrameworkElement;
                    if (container != null)
                    {
                        CheckBox cb = FindVisualChild<CheckBox>(container);

                        if (cb != null && cb.IsChecked == true)
                        {
                            var paramType = (ParameterType)item;
                            _context.ParameterEffects.Add(new ParameterEffect
                            {
                                PeEffectId = _editedEffect.EfId,
                                PeParametertypeId = paramType.PtId
                            });
                        }
                    }
                }

                // Guardamos las nuevas relaciones
                _context.SaveChanges();
                // Buscamos la instancia de la ventana principal y llamamos a su método de carga
                if (Window.GetWindow(this) is MainWindow principal)
                {
                    principal.CarregarArbre();
                }
                MessageBox.Show("Dades guardades correctament!");
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error al guardar: " + ex.Message);
            }
        }

        /// <summary>
        /// Mètode recursiu per buscar controls fills dins de l'arbre visual de WPF.
        /// Essencial per trobar els CheckBoxes dins de l'ItemsTemplate.
        /// </summary>
        private T FindVisualChild<T>(DependencyObject obj) where T : DependencyObject
        {
            for (int i = 0; i < VisualTreeHelper.GetChildrenCount(obj); i++)
            {
                DependencyObject child = VisualTreeHelper.GetChild(obj, i);
                if (child != null && child is T) return (T)child;
                T childOfChild = FindVisualChild<T>(child);
                if (childOfChild != null) return childOfChild;
            }
            return null;
        }

        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            if (_editedEffect != null)
            {
                CarregarDadesEffect();
            }
        }
    }
}