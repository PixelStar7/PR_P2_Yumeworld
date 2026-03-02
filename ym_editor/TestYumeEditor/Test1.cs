using FlaUI.Core.AutomationElements;
using FlaUI.UIA3;
using System.Windows.Controls;
using WPF = System.Windows.Controls;

namespace TestYumeEditor
{
    [TestClass]
    public class EditorUITests
    {
        // Controls de la PageAct
        TextBox? txtNomAccio;
        TextBox? txtCost;
        ComboBox? cmbEffect;
        Button? btnSave;

        // Controls de la PageEffect
        TextBox? txtNomEfecte;
        ItemsControl? icParameters;

        [TestMethod]
        public void Test_CrearAccioBasica()
        {
            FlaUI.Core.Application app;
            UIA3Automation automation;

            // Inicialitzem la pàgina d'Accions
            InitPageAct(out app, out automation, out txtNomAccio, out txtCost, out cmbEffect, out btnSave);

            try
            {
                txtNomAccio?.Enter("Atac de Prova");
                txtCost?.Enter("15");
                cmbEffect?.Select(0); // Seleccionem el primer efecte [cite: 81]

                 btnSave?.Click(); [cite: 84]

                // Verifiquem si ha sortit el MessageBox d'èxit
                var window = app.GetMainWindow(automation);
                var messageBox = window.ModalWindows.FirstOrDefault();
                Assert.IsNotNull(messageBox, "Hauria d'aparèixer un missatge de confirmació.");
            }
            finally { automation.Dispose(); app.Close(); }
        }

        // ==========================================
        // MÈTODES INIT ESPECÍFICS
        // ==========================================

        private void InitPageAct(
            out FlaUI.Core.Application app,
            out UIA3Automation automation,
            out TextBox? txtNom,
            out TextBox? txtCost,
            out ComboBox? cmbEff,
            out Button? btnGuardar)
        {
            // 1. Llançament (Igual que el teu exemple)
            string path = Path.Combine(Directory.GetCurrentDirectory(), "YumeEditor.exe");
            app = FlaUI.Core.Application.Launch(path);
            automation = new UIA3Automation();
            var window = app.GetMainWindow(automation);

            // 2. Navegació: Cliquem el botó "Actions" de la MainWindow 
            window.FindFirstDescendant(cf => cf.ByAutomationId("btnAction")).AsButton().Click();

            // 3. Mapeig de controls de la PageAct [cite: 78, 79, 81, 84]
            txtNom = window.FindFirstDescendant(cf => cf.ByAutomationId("txtNom"))?.AsTextBox();
            txtCost = window.FindFirstDescendant(cf => cf.ByAutomationId("txtCost"))?.AsTextBox();
            cmbEff = window.FindFirstDescendant(cf => cf.ByAutomationId("cmbEffect"))?.AsComboBox();
            btnGuardar = window.FindFirstDescendant(cf => cf.ByAutomationId("btnSave"))?.AsButton();
        }

        private void InitPageEffect(
            out FlaUI.Core.Application app,
            out UIA3Automation automation,
            out TextBox? txtNom,
            out Button? btnGuardar)
        {
            string path = Path.Combine(Directory.GetCurrentDirectory(), "YumeEditor.exe");
            app = FlaUI.Core.Application.Launch(path);
            automation = new UIA3Automation();
            var window = app.GetMainWindow(automation);

            // Navegació a Effects 
            window.FindFirstDescendant(cf => cf.ByAutomationId("btnEffect")).AsButton().Click();

            // Mapeig [cite: 90, 97]
            txtNom = window.FindFirstDescendant(cf => cf.ByAutomationId("txtNom"))?.AsTextBox();
            btnGuardar = window.FindFirstDescendant(cf => cf.ByAutomationId("btnSave"))?.AsButton();
        }
    }
}
