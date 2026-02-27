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
using YumeEditor.Utils;
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

        public PageAct(Act? act=null)

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
            // 1. Validaciones básicas
            if (string.IsNullOrWhiteSpace(txtNom.Text) || cmbEffect.SelectedItem == null)
            {
                MessageBox.Show("El nom de l'acció i l'efecte són obligatoris.");
                return;
            }

            try
            {
                // 2. Guardar o Actualizar el ACT principal
                if (_editedAct == null)
                {
                    _editedAct = new Act();
                    _context.Acts.Add(_editedAct);
                }

                _editedAct.AcName = txtNom.Text;
                _editedAct.AcDesc = txtDesc.Text;
                _editedAct.AcType = rbItem.IsChecked == true ? "I" : "A";

                // Si es una habilidad, guardamos su costo en la tabla ABILITY (relación 1:1)
                if (_editedAct.AcType == "A")
                {
                    int cost = 0;
                    int.TryParse(txtCost.Text, out cost);

                    if (_editedAct.Ability == null)
                    {
                        _editedAct.Ability = new Ability { AbCost = cost };
                    }
                    else
                    {
                        _editedAct.Ability.AbCost = cost;
                    }
                }
                else
                {
                    // Si es un Item, nos aseguramos de que exista el registro en la tabla ITEM
                    if (_editedAct.Item == null)
                    {
                        _editedAct.Item = new Item();
                    }
                }

                // Guardamos para obtener el AcId generado
                _context.SaveChanges();

                // 3. Gestionar ACTION_EFFECT
                // Por simplicidad, asumimos 1 acción = 1 efecto en la UI actual.
                // Si ya tenía un efecto asignado, lo recuperamos, si no, lo creamos.
                var actionEffect = _context.ActionEffects
                    .Include(ae => ae.ParameterValues)
                    .FirstOrDefault(ae => ae.AeActionId == _editedAct.AcId);

                var selectedEffect = (Effect)cmbEffect.SelectedItem;

                if (actionEffect == null)
                {
                    actionEffect = new ActionEffect
                    {
                        AeActionId = _editedAct.AcId,
                        AeEffectId = selectedEffect.EfId
                    };
                    _context.ActionEffects.Add(actionEffect);
                    _context.SaveChanges(); // Guardamos para obtener el AeId
                }
                else if (actionEffect.AeEffectId != selectedEffect.EfId)
                {
                    // Si el usuario cambió el efecto en el ComboBox, tenemos que borrar los valores viejos
                    _context.ParameterValues.RemoveRange(actionEffect.ParameterValues);
                    actionEffect.AeEffectId = selectedEffect.EfId;
                    _context.SaveChanges();
                }


                // 4. EL RETO: Leer los controles dinámicos y guardar PARAMETER_VALUE
                // Borramos los valores anteriores para este ActionEffect
                var valoresViejos = _context.ParameterValues.Where(pv => pv.PvActioneffectId == actionEffect.AeId);
                _context.ParameterValues.RemoveRange(valoresViejos);

                // Iteramos por los ParameterEffects que exige el efecto seleccionado
                foreach (var pe in selectedEffect.ParameterEffects)
                {
                    var tipoParametro = _context.ParameterTypes.Find(pe.PeParametertypeId);
                    if (tipoParametro == null) continue;

                    ParameterValue nuevoValor = new ParameterValue
                    {
                        PvActioneffectId = actionEffect.AeId,
                        PvParametertypeId = tipoParametro.PtId,
                        PvEffectId = selectedEffect.EfId
                    };

                    // Buscamos el control correcto en pnlParameters según el tipo (L o N)
                    if (tipoParametro.PtType == "L") // Era una lista de RadioButtons
                    {
                        // Buscamos el StackPanel que agrupa los RadioButtons
                        var radioGroup = pnlParameters.Children.OfType<StackPanel>()
                                        .FirstOrDefault(sp => sp.Name == "groupParam_" + tipoParametro.PtId);

                        if (radioGroup != null)
                        {
                            // Encontramos cuál RadioButton está marcado
                            var rbSeleccionado = radioGroup.Children.OfType<RadioButton>()
                                                 .FirstOrDefault(rb => rb.IsChecked == true);

                            if (rbSeleccionado != null)
                            {
                                string valorTexto = rbSeleccionado.Content.ToString();

                                if (tipoParametro.PtId == 8) // Es un AlteredState
                                {
                                    // Buscamos la ID del estado alterado por su nombre
                                    var estado = _context.AlteredStates.FirstOrDefault(a => a.AsName == valorTexto);
                                    if (estado != null) nuevoValor.PvAlteredStateValue = estado.AsId;
                                }
                                else // Es un valor de texto normal (S, A, W, etc.)
                                {
                                    nuevoValor.PvStringValue = valorTexto;
                                }
                            }
                        }
                    }
                    else // Era un TextBox numérico (N)
                    {
                        var txt = pnlParameters.Children.OfType<TextBox>()
                                  .FirstOrDefault(t => t.Name == "txtParam_" + tipoParametro.PtId);

                        if (txt != null && int.TryParse(txt.Text, out int numero))
                        {
                            nuevoValor.PvNumValue = numero;
                        }
                    }

                    _context.ParameterValues.Add(nuevoValor);
                }

                // 5. Confirmar todos los cambios
                _context.SaveChanges();
                // Buscamos la instancia de la ventana principal y llamamos a su método de carga
                if (Window.GetWindow(this) is MainWindow principal)
                {
                    principal.CarregarArbre();
                }
                MessageBox.Show("Acció guardada correctament!");

            }
            catch (Exception ex)
            {
                MessageBox.Show("Error al guardar: " + ex.Message);
            }
        }

      

        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            CarregarDadesAct();
        }

        private void CarregarDadesAct()
        {
            if (_editedAct != null)
            {
                // 1. Cargar datos básicos de la acción
                txtId.Text = _editedAct.AcId.ToString();
                txtNom.Text = _editedAct.AcName;
                txtDesc.Text = _editedAct.AcDesc;

                if (_editedAct.AcType == "I")
                {
                    rbItem.IsChecked = true;
                    txtCost.Text = string.Empty;
                }
                else
                {
                    rbAbility.IsChecked = true;
                    // Buscamos la habilidad para mostrar su coste
                    var ability = _context.Abilities.Find(_editedAct.AcId);
                    if (ability != null)
                    {
                        txtCost.Text = ability.AbCost.ToString();
                    }
                }

                // 2. Cargar el Efecto y los Parámetros Dinámicos
                var actionEffect = _editedAct.ActionEffects.FirstOrDefault();

                if (actionEffect != null)
                {
                    // Seleccionamos el efecto en el ComboBox.
                    // IMPORTANTE: Esto dispara el evento cmbTipus_SelectionChanged
                    // que crea automáticamente los TextBoxes y StackPanels en la UI.
                    cmbEffect.SelectedItem = cmbEffect.Items.OfType<Effect>()
                                            .FirstOrDefault(e => e.EfId == actionEffect.AeEffectId);

                    // Ahora buscamos los valores guardados para rellenar los controles recién creados
                    var valoresGuardados = _context.ParameterValues
                                            .Where(pv => pv.PvActioneffectId == actionEffect.AeId).ToList();

                    foreach (var valor in valoresGuardados)
                    {
                        var tipoParametro = _context.ParameterTypes.Find(valor.PvParametertypeId);
                        if (tipoParametro == null) continue;

                        if (tipoParametro.PtType == "L") // Listas (RadioButtons)
                        {
                            // Buscamos el grupo de RadioButtons correspondiente a este parámetro
                            var radioGroup = pnlParameters.Children.OfType<StackPanel>()
                                .FirstOrDefault(sp => sp.Name == "groupParam_" + tipoParametro.PtId);

                            if (radioGroup != null)
                            {
                                string valorBuscado = valor.PvStringValue;

                                // Si es un estado alterado (ID 8), la base de datos guarda la ID del estado, no el texto.
                                // Por tanto, debemos buscar el nombre del estado para saber qué RadioButton marcar.
                                if (tipoParametro.PtId == 8 && valor.PvAlteredStateValue != null)
                                {
                                    var estado = _context.AlteredStates.Find(valor.PvAlteredStateValue);
                                    if (estado != null) valorBuscado = estado.AsName;
                                }

                                // Iteramos por los RadioButtons para marcar el correcto
                                var rbToSelect = radioGroup.Children.OfType<RadioButton>()
                                    .FirstOrDefault(rb => rb.Content.ToString() == valorBuscado);

                                if (rbToSelect != null) rbToSelect.IsChecked = true;
                            }
                        }
                        else // Números (TextBox)
                        {
                            // Buscamos el TextBox correspondiente
                            var txt = pnlParameters.Children.OfType<TextBox>()
                                .FirstOrDefault(t => t.Name == "txtParam_" + tipoParametro.PtId);

                            if (txt != null && valor.PvNumValue != null)
                            {
                                txt.Text = valor.PvNumValue.ToString();
                            }
                        }
                    }
                }
            }
        }
    }
}
