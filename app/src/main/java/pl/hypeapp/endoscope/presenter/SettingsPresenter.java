package pl.hypeapp.endoscope.presenter;

import net.grandcentrix.thirtyinch.TiPresenter;

import pl.hypeapp.endoscope.util.SettingsPreferencesUtil;
import pl.hypeapp.endoscope.view.SettingsView;

public class SettingsPresenter extends TiPresenter<SettingsView> {
    private static final String OPTION_RESOLUTION = "resolution";
    private static final String OPTION_VIDEO_ENCODER = "video_encoder";
    private final SettingsPreferencesUtil settingsPreferencesUtil;
    private boolean isAudioStream;

    public SettingsPresenter(SettingsPreferencesUtil settingsPreferencesUtil) {
        this.settingsPreferencesUtil = settingsPreferencesUtil;
    }

    @Override
    protected void onWakeUp() {
        super.onWakeUp();
        loadPortPreference();
        loadResolutionPreference();
        loadVideoEncoderPreference();
        loadAudioPreference();
    }

    public void onChangeResolution(int selectedPosition) {
        settingsPreferencesUtil.saveResolutionPreference(selectedPosition);
        getView().setResolution(selectedPosition);
    }

    public void onChangeVideoEncoder(int selectedPosition) {
        settingsPreferencesUtil.saveVideoEncoderPreference(selectedPosition);
        getView().setVideoEncoder(selectedPosition);
    }

    public void onChangeAudioStream() {
        isAudioStream = !isAudioStream;
        settingsPreferencesUtil.saveAudioStreamPreference(isAudioStream);
        getView().setAudioStream(isAudioStream);
    }

    public void onChangePort(String port) {
        if (port.isEmpty()) {
            getView().changePortError();
        } else {
            settingsPreferencesUtil.savePortPreferencePreference(port);
            getView().setPort(port);
            getView().changePortSuccessful();
        }
    }

    public void showChangeVideoEncoderDialog(CharSequence[] videoEncoders, String dialogTitle) {
        int selectedItem = settingsPreferencesUtil.loadVideoEncoderPreference();
        getView().showSelectItemDialog(videoEncoders, dialogTitle, selectedItem, OPTION_VIDEO_ENCODER);
    }

    public void showChangeResolutionDialog(CharSequence[] resolutions, String dialogTitle) {
        int selectedItem = settingsPreferencesUtil.loadResolutionPreference();
        getView().showSelectItemDialog(resolutions, dialogTitle, selectedItem, OPTION_RESOLUTION);
    }

    private void loadPortPreference() {
        String port = settingsPreferencesUtil.loadPortPreference();
        getView().setPort(port);
    }

    private void loadResolutionPreference() {
        int resolution = settingsPreferencesUtil.loadResolutionPreference();
        getView().setResolution(resolution);
    }

    private void loadVideoEncoderPreference() {
        int videoEncoder = settingsPreferencesUtil.loadVideoEncoderPreference();
        getView().setVideoEncoder(videoEncoder);
    }

    private void loadAudioPreference() {
        isAudioStream = settingsPreferencesUtil.loadAudioPreference();
        getView().setAudioStream(isAudioStream);
    }

    public Boolean VerifyPassword(String password){
        String login_password = settingsPreferencesUtil.loadPassword();
        if(password.equals(AESPresenter.decryptBase642String(login_password,"kakuishdyshifncgyrsjdiosfnvjfeas","asadfdedwderfvgd"))){
            return true;
        }
        else {
            return false;
        }
    }

    public void ChangePassword(String password){
        settingsPreferencesUtil.savePasswordPreference(AESPresenter.encryptString2Base64(password,"kakuishdyshifncgyrsjdiosfnvjfeas","asadfdedwderfvgd"));
        getView().changePasswordDone();
    }

}
