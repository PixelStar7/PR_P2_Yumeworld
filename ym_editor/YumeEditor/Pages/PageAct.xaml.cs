using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
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
using YumeModel.Models;

namespace YumeEditor.Pages
{
    /// <summary>
    /// Lógica de interacción para PageAct.xaml
    /// </summary>
    public partial class PageAct : Page
    {
        private AppDbContext _context = new AppDbContext();
        private Act? _editedAct = null;

        public PageAct(Act act = null)
        {
            InitializeComponent();
            CarregarEfectes();
            _editedAct = act;
        }

        private void CarregarEfectes()
        {
            try
            {
                // Obtenim el llistat des de la taula 'parameter_types'
                List<Effect> effects = _context.Effects.Include(e => e.ParameterEffects).ToList();
                cmbEffect.ItemsSource = effects;
            }
            catch (Exception ex)
            {
                // Gestió d'errors en cas de fallada de connexió amb el servidor MySQL
                MessageBox.Show(ex.Message, "Error en carregar paràmetres", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void cmbTipus_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (cmbEffect.SelectedItem is Effect efecteSeleccionat)
            {
                pnlParameters.Children.Clear();

                foreach (var pe in efecteSeleccionat.ParameterEffects)
                {
                    var tipusParametre = _context.ParameterTypes.Find(pe.PeParametertypeId);

                    if (tipusParametre != null)
                    {
                        // 1. Afegim l'etiqueta amb el nom del paràmetre
                        Label lbl = new Label
                        {
                            Content = tipusParametre.PtName,
                            FontWeight = FontWeights.Bold,
                            Margin = new Thickness(0, 10, 0, 0)
                        };
                        pnlParameters.Children.Add(lbl);

                        // 2. Decidim quin control crear segons el PtType
                        if (tipusParametre.PtType == "L") // Tipus LLISTA
                        {
                            // Creem un contenidor per als RadioButtons
                            StackPanel radioGroup = new StackPanel
                            {
                                Name = "groupParam_" + tipusParametre.PtId,
                                Margin = new Thickness(10, 0, 0, 10)
                            };
                            radioGroup.Orientation = Orientation.Horizontal;

                            // Obtenim els valors (de la columna PtValueList o de la taula AlteredState)
                            List<string> opcions = new List<string>();

                            if (tipusParametre.PtId == 8) // Cas especial: AlteredState
                            {
                                opcions = _context.AlteredStates.Select(asl => asl.AsName).ToList();
                            }
                            else if (!string.IsNullOrEmpty(tipusParametre.PtValueList))
                            {
                                opcions = tipusParametre.PtValueList.Split(',').Select(s => s.Trim()).ToList();
                            }

                            // Creem un RadioButton per cada opció
                            foreach (var opcio in opcions)
                            {
                                RadioButton rb = new RadioButton
                                {
                                    Content = opcio,
                                    GroupName = "paramGroup_" + tipusParametre.PtId, // Important per separar grups
                                    Tag = tipusParametre.PtId, // Guardem l'ID per saber a qui pertany
                                    Margin = new Thickness(0, 2, 20, 2)
                                };
                                radioGroup.Children.Add(rb);
                            }
                            pnlParameters.Children.Add(radioGroup);
                        }
                        else // Tipus NÚMERO (TextBox)
                        {
                            TextBox txt = new TextBox
                            {
                                Name = "txtParam_" + tipusParametre.PtId,
                                Margin = new Thickness(10, 0, 0, 10),
                                Width = 100,
                                HorizontalAlignment = HorizontalAlignment.Left
                            };
                            pnlParameters.Children.Add(txt);
                        }
                    }
                }
            }
        }

        private void btnSave_Click(object sender, RoutedEventArgs e)
        {

        }

        private void txtNom_TextChanged(object sender, TextChangedEventArgs e)
        {


        }
    }
}
