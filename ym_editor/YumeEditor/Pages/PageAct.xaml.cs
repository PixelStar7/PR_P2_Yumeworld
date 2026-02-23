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
        private Act _editedAct = null;

        public PageAct(Act act)
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
            // 1. Obtenim l'efecte seleccionat
            Effect efecteSeleccionat = cmbEffect.SelectedItem as Effect;
            if (efecteSeleccionat == null) return;

             // 2. Netegem el panell de paràmetres per no acumular controls 
            pnlParameters.Children.Clear();

            // 3. Per cada paràmetre que aquest efecte necessiti (gràcies a l'Include que has fet)
            foreach (var pe in efecteSeleccionat.ParameterEffects)
            {
                // Necessitem carregar el nom del tipus de paràmetre si no ve a l'Include
                var tipusParametre = _context.ParameterTypes.Find(pe.PeParametertypeId);

                if (tipusParametre != null)
                {
                     // Creem un Label amb el nom del paràmetre
                    Label lbl = new Label
                    {
                        Content = tipusParametre.PtName,
                        Margin = new Thickness(0, 5, 0, 0)
                    };

                     // Creem un TextBox per introduir el valor
                    TextBox txt = new TextBox
                    {
                        Name = "txtParam_" + tipusParametre.PtId, // Li posem un nom per identificar-lo després
                        Margin = new Thickness(0, 0, 0, 10)
                    };

                    // Els afegim al StackPanel de la interfície [cite: 64]
                    pnlParameters.Children.Add(lbl);
                    pnlParameters.Children.Add(txt);
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
