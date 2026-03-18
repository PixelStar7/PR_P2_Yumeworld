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

        // Llista temporal per guardar tots els efectes d'aquesta acció
        private List<ActionEffectHelper> _effectsTemporal = new List<ActionEffectHelper>();
        // Variable per saber quin és l'efecte que estem editant actualment a la part dreta
        private ActionEffectHelper? _currentSelectedEffectHelper = null;

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

        // ==========================================
        // FASE 2: GESTIÓ DE LA LLISTA D'EFECTES (Afegir/Treure)
        // ==========================================

        private void btnAddEffect_Click(object sender, RoutedEventArgs e)
        {
            // 1. Comprovem quin efecte ha triat l'usuari al desplegable (ComboBox)
            if (cmbEffect.SelectedItem is Effect selectedEffect)
            {
                // 2. Creem una nova "Caixa" (Helper) per guardar les dades d'aquest efecte concret
                var nouHelper = new ActionEffectHelper
                {
                    EffectId = selectedEffect.EfId,
                    EffectName = selectedEffect.EfName
                };

                // 3. L'afegim a la nostra llista temporal de la memòria
                _effectsTemporal.Add(nouHelper);

                // 4. Actualitzem la llista visual perquè aparegui a la pantalla
                RefrescarLlistaEfectes();

                // 5. Seleccionem automàticament aquest nou efecte a la llista perquè sigui més còmode per a l'usuari
                lsbEffects.SelectedItem = nouHelper;
            }
        }

        private void btnRemoveEffect_Click(object sender, RoutedEventArgs e)
        {
            // 1. Mirem quin efecte està marcat (seleccionat) a la llista de l'esquerra
            if (lsbEffects.SelectedItem is ActionEffectHelper seleccionat)
            {
                // 2. L'esborrem de la nostra memòria temporal
                _effectsTemporal.Remove(seleccionat);

                // 3. Com que l'hem esborrat, li diem al programa que ara mateix "no hi ha res seleccionat"
                _currentSelectedEffectHelper = null;

                // 4. Buidem el panell de la dreta perquè no continuï mostrant els paràmetres d'un efecte que ja no existeix
                pnlParameters.Children.Clear();

                // 5. Actualitzem la llista visual perquè desaparegui de la pantalla
                RefrescarLlistaEfectes();
            }
        }

        private void RefrescarLlistaEfectes()
        {
            // Truc clàssic de WPF: Per forçar que una ListBox es redibuixi, primer li traiem les dades (null) i li tornem a posar
            lsbEffects.ItemsSource = null;
            lsbEffects.ItemsSource = _effectsTemporal;
        }


        // ==========================================
        // FASE 3: UI DINÀMICA I MEMÒRIA TEMPORAL
        // ==========================================
        private void lsbEffects_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            // 1. Abans de netejar la pantalla i mostrar el nou efecte, GUARDEM el que l'usuari havia escrit a l'efecte anterior!
            GuardarValorsActuals();

            // 2. Canviem l'enfocament al nou efecte seleccionat a la llista
            _currentSelectedEffectHelper = lsbEffects.SelectedItem as ActionEffectHelper;

            // 3. Buidem el panell de la dreta per posar-hi els nous controls
            pnlParameters.Children.Clear();

            if (_currentSelectedEffectHelper == null) return;

            // 4. Busquem a la base de dades quins paràmetres necessita l'efecte seleccionat
            var efecte = _context.Effects.Include(ef => ef.ParameterEffects).FirstOrDefault(ef => ef.EfId == _currentSelectedEffectHelper.EffectId);
            if (efecte == null) return;

            // 5. Generem els controls per a cada paràmetre (igual que feies abans, però amb memòria)
            foreach (var pe in efecte.ParameterEffects)
            {
                var tipusParametre = _context.ParameterTypes.Find(pe.PeParametertypeId);
                if (tipusParametre == null) continue;

                Label lbl = new Label { Content = tipusParametre.PtName, FontWeight = FontWeights.Bold, Margin = new Thickness(0, 10, 0, 0) };
                pnlParameters.Children.Add(lbl);

                if (tipusParametre.PtType == "L") // Si és tipus LLISTA (RadioButtons)
                {
                    StackPanel radioGroup = new StackPanel { Name = "groupParam_" + tipusParametre.PtId, Margin = new Thickness(10, 0, 0, 10) };

                    List<string> opcions = tipusParametre.PtId == 8
                        ? _context.AlteredStates.Select(a => a.AsName).ToList()
                        : tipusParametre.PtValueList?.Split(',').Select(s => s.Trim()).ToList() ?? new List<string>();

                    foreach (var opcio in opcions)
                    {
                        RadioButton rb = new RadioButton
                        {
                            Content = opcio,
                            // TRUC: Posem el UniqueId al GroupName perquè si afegeixes 2 atacs de foc, els radiobuttons d'un no desmarquin els de l'altre!
                            GroupName = "paramGroup_" + tipusParametre.PtId + "_" + _currentSelectedEffectHelper.UniqueId,
                            Margin = new Thickness(0, 2, 20, 2)
                        };

                        // MEMÒRIA: Si l'usuari ja havia marcat aquest botó abans, el tornem a marcar!
                        if (_currentSelectedEffectHelper.ParameterValues.ContainsKey(tipusParametre.PtId) &&
                            _currentSelectedEffectHelper.ParameterValues[tipusParametre.PtId] == opcio)
                        {
                            rb.IsChecked = true;
                        }

                        radioGroup.Children.Add(rb);
                    }
                    pnlParameters.Children.Add(radioGroup);
                }
                else // Si és tipus NÚMERO (TextBox)
                {
                    TextBox txt = new TextBox { Name = "txtParam_" + tipusParametre.PtId, Margin = new Thickness(10, 0, 0, 10), Width = 100, HorizontalAlignment = HorizontalAlignment.Left };

                    // MEMÒRIA: Si l'usuari ja havia escrit un número abans, el tornem a posar!
                    if (_currentSelectedEffectHelper.ParameterValues.ContainsKey(tipusParametre.PtId))
                    {
                        txt.Text = _currentSelectedEffectHelper.ParameterValues[tipusParametre.PtId];
                    }

                    pnlParameters.Children.Add(txt);
                }
            }
        }

        private void GuardarValorsActuals()
        {
            // Si no hi ha cap efecte seleccionat, no hi ha res a guardar
            if (_currentSelectedEffectHelper == null) return;

            var efecte = _context.Effects.Include(ef => ef.ParameterEffects).FirstOrDefault(ef => ef.EfId == _currentSelectedEffectHelper.EffectId);
            if (efecte == null) return;

            // Recorrem els controls que hi ha ARA MATEIX per pantalla i els guardem al Diccionari del Helper
            foreach (var pe in efecte.ParameterEffects)
            {
                var tipusParametre = _context.ParameterTypes.Find(pe.PeParametertypeId);
                if (tipusParametre == null) continue;

                if (tipusParametre.PtType == "L") // Llegim el RadioButton marcat
                {
                    var radioGroup = pnlParameters.Children.OfType<StackPanel>().FirstOrDefault(sp => sp.Name == "groupParam_" + tipusParametre.PtId);
                    if (radioGroup != null)
                    {
                        var rbSeleccionat = radioGroup.Children.OfType<RadioButton>().FirstOrDefault(rb => rb.IsChecked == true);
                        if (rbSeleccionat != null)
                            _currentSelectedEffectHelper.ParameterValues[tipusParametre.PtId] = rbSeleccionat.Content.ToString();
                    }
                }
                else // Llegim el TextBox
                {
                    var txt = pnlParameters.Children.OfType<TextBox>().FirstOrDefault(t => t.Name == "txtParam_" + tipusParametre.PtId);
                    if (txt != null && !string.IsNullOrWhiteSpace(txt.Text))
                    {
                        _currentSelectedEffectHelper.ParameterValues[tipusParametre.PtId] = txt.Text;
                    }
                }
            }
        }


        // ==========================================
        // FASE 4: GUARDAR MÚLTIPLES EFECTES
        // ==========================================
        private void btnSave_Click(object sender, RoutedEventArgs e)
        {
            if (string.IsNullOrWhiteSpace(txtNom.Text))
            {
                MessageBox.Show("El nom de l'acció és obligatori.");
                return;
            }

            // IMPORTANT: Guardem els valors del control que estigui obert ARA MATEIX abans de procedir
            GuardarValorsActuals();

            try
            {
                // 1. Guardar el Act (Acció base)
                if (_editedAct == null)
                {
                    _editedAct = new Act();
                    _context.Acts.Add(_editedAct);
                }

                _editedAct.AcName = txtNom.Text;
                _editedAct.AcDesc = txtDesc.Text;
                _editedAct.AcType = rbItem.IsChecked == true ? "I" : "A";

                // Guardem Ability / Item
                if (_editedAct.AcType == "A")
                {
                    int.TryParse(txtCost.Text, out int cost);
                    if (_editedAct.Ability == null) _editedAct.Ability = new Ability { AbCost = cost };
                    else _editedAct.Ability.AbCost = cost;
                }
                else
                {
                    if (_editedAct.Item == null) _editedAct.Item = new Item();
                }

                _context.SaveChanges(); // Necessitem l'ID del Act per fer els ActionEffects!

                // 2. ESBORREM TOT EL QUE HI HAVIA (Neteja total)
                var anticsEfectes = _context.ActionEffects.Where(ae => ae.AeActionId == _editedAct.AcId).ToList();
                foreach (var vellAe in anticsEfectes)
                {
                    var paramsVells = _context.ParameterValues.Where(pv => pv.PvActioneffectId == vellAe.AeId);
                    _context.ParameterValues.RemoveRange(paramsVells);
                }
                _context.ActionEffects.RemoveRange(anticsEfectes);
                _context.SaveChanges();

                // 3. CREEM TOTS ELS EFECTES DE LA LLISTA (Un per un)
                foreach (var helper in _effectsTemporal)
                {
                    ActionEffect nouAe = new ActionEffect { AeActionId = _editedAct.AcId, AeEffectId = helper.EffectId };
                    _context.ActionEffects.Add(nouAe);
                    _context.SaveChanges(); // Necessitem l'ID d'aquest nou ActionEffect per vincular els paràmetres

                    // 4. GUARDEM ELS PARÀMETRES D'AQUEST EFECTE (Del diccionari a la BD)
                    foreach (var kvp in helper.ParameterValues)
                    {
                        int ptId = kvp.Key;
                        string valorText = kvp.Value;

                        var tipus = _context.ParameterTypes.Find(ptId);
                        if (tipus == null) continue;

                        ParameterValue pv = new ParameterValue { PvActioneffectId = nouAe.AeId, PvParametertypeId = ptId, PvEffectId = helper.EffectId };

                        if (tipus.PtType == "L") // Si és llista
                        {
                            if (ptId == 8) // Cas especial: Altered State
                            {
                                var estat = _context.AlteredStates.FirstOrDefault(s => s.AsName == valorText);
                                if (estat != null) pv.PvAlteredStateValue = estat.AsId;
                            }
                            else
                            {
                                pv.PvStringValue = valorText;
                            }
                        }
                        else // Si és número
                        {
                            if (int.TryParse(valorText, out int num)) pv.PvNumValue = num;
                        }

                        _context.ParameterValues.Add(pv);
                    }
                }

                _context.SaveChanges(); // L'últim commit
                MessageBox.Show("Acció i tots els seus efectes guardats correctament!", "Èxit", MessageBoxButton.OK, MessageBoxImage.Information);

                if (Window.GetWindow(this) is MainWindow principal) principal.CarregarArbre();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error al guardar: " + (ex.InnerException?.Message ?? ex.Message));
            }
        }


        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            CarregarDadesAct();
        }

        // ==========================================
        // CÀRREGA DE DADES EXISTENTS
        // ==========================================
        private void CarregarDadesAct()
        {
            if (_editedAct != null)
            {
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
                    var ability = _context.Abilities.Find(_editedAct.AcId);
                    if (ability != null) txtCost.Text = ability.AbCost.ToString();
                }

                // BUIdEM LA LLISTA VISUAL I OMPLIM LA MEMÒRIA AMB ELS EFECTES DE LA BD
                _effectsTemporal.Clear();
                var accioAmbEfectes = _context.Acts.Include(a => a.ActionEffects).FirstOrDefault(a => a.AcId == _editedAct.AcId);

                if (accioAmbEfectes != null)
                {
                    foreach (var ae in accioAmbEfectes.ActionEffects)
                    {
                        var efecteDb = _context.Effects.Find(ae.AeEffectId);
                        if (efecteDb == null) continue;

                        var helper = new ActionEffectHelper { EffectId = efecteDb.EfId, EffectName = efecteDb.EfName };

                        // Carregar els paràmetres de la base de dades a la memòria del helper
                        var paramsDb = _context.ParameterValues.Where(pv => pv.PvActioneffectId == ae.AeId).ToList();
                        foreach (var pv in paramsDb)
                        {
                            var tipus = _context.ParameterTypes.Find(pv.PvParametertypeId);
                            if (tipus == null) continue;

                            if (tipus.PtType == "L")
                            {
                                if (tipus.PtId == 8 && pv.PvAlteredStateValue != null)
                                {
                                    var estat = _context.AlteredStates.Find(pv.PvAlteredStateValue);
                                    if (estat != null) helper.ParameterValues[tipus.PtId] = estat.AsName;
                                }
                                else if (!string.IsNullOrEmpty(pv.PvStringValue))
                                {
                                    helper.ParameterValues[tipus.PtId] = pv.PvStringValue;
                                }
                            }
                            else if (pv.PvNumValue != null)
                            {
                                helper.ParameterValues[tipus.PtId] = pv.PvNumValue.ToString();
                            }
                        }

                        _effectsTemporal.Add(helper);
                    }

                    RefrescarLlistaEfectes();
                }
            }
        }
    }
}
