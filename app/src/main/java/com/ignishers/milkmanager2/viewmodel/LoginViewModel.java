package com.ignishers.milkmanager2.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ignishers.milkmanager2.data.model.UserAccount;
import com.ignishers.milkmanager2.data.repository.UserRepository;

public class LoginViewModel extends ViewModel {

    private UserRepository repository;

    // LiveData for UI Observation
    private final MutableLiveData<String> loginResult = new MutableLiveData<>(); // Success = Token or "OK", Fail = Error Msg
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<UserAccount> userProfile = new MutableLiveData<>();

    public LoginViewModel() {
        repository = new UserRepository();
    }

    public LiveData<String> getLoginResult() { return loginResult; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<UserAccount> getUserProfile() { return userProfile; }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            loginResult.setValue("Please enter email and password");
            return;
        }

        isLoading.setValue(true);
        repository.login(email, password, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(String token, String userId) {
                // Login Success -> Now fetch Profile
                fetchUserProfile(userId);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                loginResult.setValue("Error: " + error);
            }
        });
    }

    private final MutableLiveData<String> navigateTo = new MutableLiveData<>(); // "ADMIN", "SELLER"

    public LiveData<String> getNavigateTo() { return navigateTo; }

    private void fetchUserProfile(String authUid) {
        repository.fetchUserDetails(authUid, new UserRepository.UserDetailsCallback() {
            @Override
            public void onUserLoaded(UserAccount user) {
                isLoading.setValue(false);

                if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
                    loginResult.setValue("Error: Account is Inactive. Contact Admin.");
                    return; // Stop here
                }

                userProfile.setValue(user); // Triggers Navigation in UI
                
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    navigateTo.setValue("ADMIN");
                } else {
                    navigateTo.setValue("SELLER");
                }
                
                loginResult.setValue("SUCCESS");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                // Even if profile fails, we might still let them in, or block them
                // For now, block them if no profile found
                loginResult.setValue("Profile Error: " + error);
            }
        });
    }
}
