using Microsoft.EntityFrameworkCore;
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
    /// Lógica de interacción para PageRole.xaml
    /// </summary>
    public partial class PageRole : Page
    {
        private Playerrole _roleActual;

        // Llista temporal per gestionar les habilitats a la memòria abans de guardar
        private List<AbilityHelper> _abilitiesTemporal = new List<AbilityHelper>();

        public PageRole(Playerrole role = null)
        {
            InitializeComponent();
            carregarDades(role);
        }

        private void carregarDades(Playerrole role)
        {
            // 1. Carregar el desplegable d'habilitats
            CarregarComboBox();

            _roleActual = role;

            if (_roleActual != null)
            {
                // --- MODO EDITAR ---
                txt_name.Text = _roleActual.PrName;
                txt_desc.Text = _roleActual.PrDesc;

                txt_hp.Text = _roleActual.PrHp.ToString();
                txt_mp.Text = _roleActual.PrMp.ToString();
                txt_atk.Text = _roleActual.PrAtk.ToString();
                txt_def.Text = _roleActual.PrDef.ToString();

                // Carreguem les habilitats del rol a la llista temporal
                _abilitiesTemporal.Clear();
                foreach (var ab in _roleActual.RaAbilities)
                {
                    _abilitiesTemporal.Add(new AbilityHelper
                    {
                        AbilityId = ab.AbActionId,
                        NomAbility = ab.AbAction.AcName
                    });
                }
                RefrescarLlistaAbilities();
            }
            else
            {
                // --- MODO CREAR ---
                _roleActual = new Playerrole();
            }
        }

        private void CarregarComboBox()
        {
            using (var context = new AppDbContext())
            {
                // Busquem totes les habilitats i incloem l'Acció per poder mostrar-ne el nom
                var habilitats = context.Abilities.Include(a => a.AbAction).ToList();
                cbAbility.ItemsSource = habilitats;
                cbAbility.DisplayMemberPath = "AbAction.AcName";
                cbAbility.SelectedValuePath = "AbActionId";
            }
        }

        private void RefrescarLlistaAbilities()
        {
            lsbAbilityPull.ItemsSource = null;
            lsbAbilityPull.ItemsSource = _abilitiesTemporal;
        }

        private void btnAddAbility_Click(object sender, RoutedEventArgs e)
        {
            if (cbAbility.SelectedValue == null) return;

            int idHabilitat = (int)cbAbility.SelectedValue;
            string nomHabilitat = cbAbility.Text;

            // Evitem afegir-la si ja hi és
            if (_abilitiesTemporal.Any(a => a.AbilityId == idHabilitat))
            {
                MessageBox.Show("Aquesta habilitat ja està assignada a aquest rol.");
                return;
            }

            _abilitiesTemporal.Add(new AbilityHelper { AbilityId = idHabilitat, NomAbility = nomHabilitat });
            RefrescarLlistaAbilities();
        }

        private void btnRemoveAbility_Click(object sender, RoutedEventArgs e)
        {
            if (lsbAbilityPull.SelectedItem is AbilityHelper seleccio)
            {
                _abilitiesTemporal.Remove(seleccio);
                RefrescarLlistaAbilities();
            }
        }

        // ==========================================
        // GUARDAR DADES
        // ==========================================
        private void btnSave_Click(object sender, RoutedEventArgs e)
        {
            if (string.IsNullOrWhiteSpace(txt_name.Text))
            {
                MessageBox.Show("El nom del rol no pot estar buit.");
                return;
            }

            using (var context = new AppDbContext())
            {
                Playerrole trackRole;

                if (_roleActual.PrId == 0)
                {
                    // INSERIR NOU
                    trackRole = new Playerrole();
                    context.Playerroles.Add(trackRole);
                }
                else
                {
                    // MODIFICAR
                    // L'hem de buscar a la BD forçant el tracking de RaAbilities per a les relacions N:M
                    trackRole = context.Playerroles
                        .Include(r => r.RaAbilities)
                        .FirstOrDefault(r => r.PrId == _roleActual.PrId);
                }

                if (trackRole != null)
                {
                    trackRole.PrName = txt_name.Text;
                    trackRole.PrDesc = txt_desc.Text;

                    trackRole.PrHp = int.TryParse(txt_hp.Text, out int hp) ? hp : 0;
                    trackRole.PrMp = int.TryParse(txt_mp.Text, out int mp) ? mp : 0;
                    trackRole.PrAtk = int.TryParse(txt_atk.Text, out int atk) ? atk : 0;
                    trackRole.PrDef = int.TryParse(txt_def.Text, out int def) ? def : 0;

                    // --- MÀGIA RELACIONAL (Molts a Molts) ---
                    // Buidem la llista actual d'Entity Framework...
                    trackRole.RaAbilities.Clear();

                    // I li tornem a afegir les que hi ha a la nostra llista de la interfície
                    foreach (var helper in _abilitiesTemporal)
                    {
                        // Hem de fer un "Find" de l'habilitat dins el context perquè sàpiga que ja existeix
                        var ability = context.Abilities.Find(helper.AbilityId);
                        if (ability != null)
                        {
                            trackRole.RaAbilities.Add(ability);
                        }
                    }

                    context.SaveChanges();
                    MessageBox.Show("S'ha guardat el Rol i les seves habilitats correctament!", "Èxit", MessageBoxButton.OK, MessageBoxImage.Information);
                    TreeUtils.actualitzarTree();
                }
            }
        }
    }
}