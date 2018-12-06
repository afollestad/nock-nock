package com.afollestad.nocknock.di.viewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.afollestad.nocknock.ui.addsite.AddSiteViewModel;
import com.afollestad.nocknock.ui.main.MainViewModel;
import com.afollestad.nocknock.ui.viewsite.ViewSiteViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

/** https://proandroiddev.com/viewmodel-with-dagger2-architecture-components-2e06f06c9455 */
@Module
public abstract class ViewModelModule {

  @Binds
  abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);

  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel.class)
  abstract ViewModel mainViewModel(MainViewModel viewModel);

  @Binds
  @IntoMap
  @ViewModelKey(AddSiteViewModel.class)
  abstract ViewModel addSiteViewModel(AddSiteViewModel viewModel);

  @Binds
  @IntoMap
  @ViewModelKey(ViewSiteViewModel.class)
  abstract ViewModel viewSiteViewModel(ViewSiteViewModel viewModel);
}
