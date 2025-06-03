package kr.ac.uc.test_2025_05_19_k.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u000e"}, d2 = {"Lkr/ac/uc/test_2025_05_19_k/viewmodel/OnboardingViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "cacheManager", "Lkr/ac/uc/test_2025_05_19_k/repository/ProfileCacheManager;", "application", "Landroid/app/Application;", "(Lkr/ac/uc/test_2025_05_19_k/repository/ProfileCacheManager;Landroid/app/Application;)V", "_profileState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lkr/ac/uc/test_2025_05_19_k/ui/profile/ProfileState;", "profileState", "Lkotlinx/coroutines/flow/StateFlow;", "getProfileState", "()Lkotlinx/coroutines/flow/StateFlow;", "app_debug"})
public final class OnboardingViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kr.ac.uc.test_2025_05_19_k.repository.ProfileCacheManager cacheManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<kr.ac.uc.test_2025_05_19_k.ui.profile.ProfileState> _profileState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<kr.ac.uc.test_2025_05_19_k.ui.profile.ProfileState> profileState = null;
    
    @javax.inject.Inject()
    public OnboardingViewModel(@org.jetbrains.annotations.NotNull()
    kr.ac.uc.test_2025_05_19_k.repository.ProfileCacheManager cacheManager, @org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<kr.ac.uc.test_2025_05_19_k.ui.profile.ProfileState> getProfileState() {
        return null;
    }
}