using System.Windows;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;

using Once_v2_2015.View;

namespace Once_v2_2015.ViewModel
{
    public class MainViewModel : ViewModelBase
    {
        #region Command

        #region CounterCommand

        private RelayCommand<Window> _counterCommand;

        public RelayCommand<Window> CounterCommand
        {
            get { return _counterCommand ?? (_counterCommand = new RelayCommand<Window>(Counter)); }
        }

        private void Counter(Window w)
        {
            w.Visibility = Visibility.Collapsed;
            CounterWindow cw = new CounterWindow();
            cw.ShowDialog();
            w.Visibility = Visibility.Visible;
        }

        #endregion

        #region AdjustmentCommand

        private RelayCommand<Window> _adjustmentCommand;

        public RelayCommand<Window> AdjustmentCommand
        {
            get { return _adjustmentCommand ?? (_adjustmentCommand = new RelayCommand<Window>(Adjustment)); }
        }

        private void Adjustment(Window w)
        {
            w.Visibility = Visibility.Collapsed;
            AdjustmentWindow aw = new AdjustmentWindow();
            aw.ShowDialog();
            w.Visibility = Visibility.Visible;
        }

        #endregion

        #endregion

        public MainViewModel()
        {
            
        }
    }
}