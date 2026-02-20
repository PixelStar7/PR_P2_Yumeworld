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
        private Effect _efecteEditat = null;

        public PageEffect(Effect effect)
        {
            InitializeComponent();
            CarregarParametres();
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
                icParameters.ItemsSource = parameterTypes;
            }
            catch (Exception ex)
            {
                // Gestió d'errors en cas de fallada de connexió amb el servidor MySQL
                MessageBox.Show(ex.Message, "Error en carregar paràmetres", MessageBoxButton.OK, MessageBoxImage.Error);
                return;
            }
        }

        /// <summary>
        /// Executa la lògica de persistència de dades en prémer el botó de guardar.
        /// Gestiona tant l'entitat principal (Effect) com la relació N:M (ParameterEffect).
        /// </summary>
        private void btnSave_Click(object sender, RoutedEventArgs e)
        {
            // Validació de seguretat: el nom és obligatori a la base de dades
            if (string.IsNullOrWhiteSpace(txtNom.Text)) return;

            try
            {
                // 1. GESTIÓ DE L'ENTITAT PRINCIPAL (EFFECT)
                if (_efecteEditat == null)
                {
                    // Mode Creació: Afegim un nou objecte al context
                    _efecteEditat = new Effect { EfName = txtNom.Text };
                    _context.Effects.Add(_efecteEditat);
                }
                else
                {
                    // Mode Edició: Actualitzem les propietats de l'objecte traquejat
                    _efecteEditat.EfName = txtNom.Text;
                }

                // Guardem per assegurar que l'objecte té un ID vàlid abans de crear relacions
                _context.SaveChanges();

                // 2. GESTIÓ DE RELACIONS (PARAMETER_EFFECT)
                // Per evitar duplicats o conflictes en l'edició, eliminem les relacions actuals
                // i les tornem a crear basant-nos en l'estat actual dels CheckBoxes.
                var relacionsAntigues = _context.ParameterEffects
                    .Where(pe => pe.PeEffectId == _efecteEditat.EfId);

                _context.ParameterEffects.RemoveRange(relacionsAntigues);

                // 3. PROCESSAMENT DE LA INTERFÍCIE DINÀMICA
                // Recorrem els elements de l'ItemsControl per detectar quins estan marcats
                foreach (var item in icParameters.Items)
                {
                    // Obtenim el contenidor visual per poder accedir al CheckBox
                    var container = icParameters.ItemContainerGenerator.ContainerFromItem(item) as FrameworkElement;
                    if (container != null)
                    {
                        CheckBox cb = FindVisualChild<CheckBox>(container);

                        // Si l'usuari ha marcat el paràmetre, creem la nova entrada a la taula de relació
                        if (cb != null && cb.IsChecked == true)
                        {
                            var paramType = (ParameterType)item;
                            _context.ParameterEffects.Add(new ParameterEffect
                            {
                                PeEffectId = _efecteEditat.EfId,
                                PeParametertypeId = paramType.PtId
                            });
                        }
                    }
                }

                // Confirmem tots els canvis a la base de dades en una única transacció
                _context.SaveChanges();
                MessageBox.Show("Dades guardades correctament!");

                // Nota: Aquí es podria netejar el formulari o refrescar la llista principal
            }
            catch (Exception ex)
            {
                // Mostra l'error detallat (útil si salta algun Trigger de la BD)
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
    }
}