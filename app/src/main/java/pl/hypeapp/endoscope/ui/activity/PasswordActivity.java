package pl.hypeapp.endoscope.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.arcsoft.arcfacedemo.activity.FaceAttrPreviewActivity;
import com.arcsoft.arcfacedemo.common.Constants;
import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.RuntimeABI;
import com.hb.dialog.dialog.ConfirmDialog;
import com.hb.dialog.myDialog.MyImageMsgDialog;
import com.hb.dialog.myDialog.MyPwdInputDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pl.hypeapp.endoscope.R;
import pl.hypeapp.endoscope.presenter.AESPresenter;
import pl.hypeapp.endoscope.util.SettingsPreferencesUtil;

import static net.majorkernelpanic.streaming.rtp.H264Packetizer.TAG;

public class PasswordActivity extends Activity implements View.OnClickListener {

    boolean libraryExists = true;

    private static final String[] LIBRARIES = new String[]{
            // 人脸相关
            "libarcsoft_face_engine.so",
            "libarcsoft_face.so",
            // 图像库相关
            "libarcsoft_image_util.so",
    };

    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    private SettingsPreferencesUtil settingsPreferencesUtil;

    private  String login_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_password);
        ButterKnife.bind(this);
        libraryExists = checkSoFile(LIBRARIES);
        settingsPreferencesUtil = new SettingsPreferencesUtil(PreferenceManager.getDefaultSharedPreferences(this));
        boolean havePassword = settingsPreferencesUtil.loadHavePasswordPreference();
        boolean activeEngine = settingsPreferencesUtil.loadactiveEngine();
        login_password = settingsPreferencesUtil.loadPassword();

        if(!activeEngine){
            activeEngine(settingsPreferencesUtil);
        }

        if(!havePassword){
            final MyPwdInputDialog pwdDialog = new MyPwdInputDialog(this)
                    .builder()
                    .setTitle("请输入密码");
            final ConfirmDialog confirmDialog = new ConfirmDialog(this);
            confirmDialog.setLogoImg(R.mipmap.dialog_notice).setMsg("首次使用，请输入密码");
            confirmDialog.setPositiveBtn(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmDialog.dismiss();
                    pwdDialog.show();
                }
            });
            confirmDialog.show();

            pwdDialog.setPasswordListener(new MyPwdInputDialog.OnPasswordResultListener() {
                @Override
                public void onPasswordResult(String password) {
                    pwdDialog.dismiss();
                    showToast("您输入的密码为"+password+"\n请妥善保管密码");
                    settingsPreferencesUtil.saveHavePasswordPreference(true);
                    settingsPreferencesUtil.savePasswordPreference(AESPresenter.encryptString2Base64(
                            password,"kakuishdyshifncgyrsjdiosfnvjfeas","asadfdedwderfvgd"));
                }
            });
        }



    }

    @OnClick({R.id.Type_Password})

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.Type_Password:
                final MyPwdInputDialog pwdDialog = new MyPwdInputDialog(this)
                        .builder()
                        .setTitle("请输入密码");
                pwdDialog.setPasswordListener(new MyPwdInputDialog.OnPasswordResultListener() {
                    @Override
                    public void onPasswordResult(String password) {
                        if(password.equals(AESPresenter.decryptBase642String(login_password,"kakuishdyshifncgyrsjdiosfnvjfeas","asadfdedwderfvgd"))){
                            Intent intent = new Intent(PasswordActivity.this, FaceAttrPreviewActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"密码错误",Toast.LENGTH_LONG).show();
                        }
                        pwdDialog.dismiss();
                    }
                });
                pwdDialog.show();
                break;
        }
    }


    private boolean checkSoFile(String[] libraries) {
        File dir = new File(getApplicationInfo().nativeLibraryDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        List<String> libraryNameList = new ArrayList<>();
        for (File file : files) {
            libraryNameList.add(file.getName());
        }
        boolean exists = true;
        for (String library : libraries) {
            exists &= libraryNameList.contains(library);
        }
        return exists;
    }



    protected boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }



    protected void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }



    public void activeEngine(final SettingsPreferencesUtil settingsPreferencesUtil){
        if (!libraryExists) {
            showToast(getString(R.string.library_not_found));
            return;
        }
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) {
                RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
                Log.i(TAG, "subscribe: getRuntimeABI() " + runtimeABI);

                long start = System.currentTimeMillis();
                int activeCode = FaceEngine.activeOnline(PasswordActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                Log.i(TAG, "subscribe cost: " + (System.currentTimeMillis() - start));
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            showToast(getString(R.string.active_success));
                            settingsPreferencesUtil.saveActiveEnginePreference(true);
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            showToast(getString(R.string.already_activated));
                        } else {
                            showToast(getString(R.string.active_failed, activeCode));
                        }

                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = FaceEngine.getActiveFileInfo(PasswordActivity.this, activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            Log.i(TAG, activeFileInfo.toString());
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast(e.getMessage());

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
