package net.hearnsoft.gensokyoradio.trd.utils;

import android.app.Activity;
import android.app.Application;

import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;


/**
 * ViewModel工具类
 * @author CSDN JohnLiu_
 */
public class ViewModelUtils {
    private static Map<Class, ViewModel> viewModelMap = new HashMap<>();

    /**
     * 获取全局唯一的ViewModel
     * 常用与跨页面修改数据（并且需要刷新显示），比如在某个页面对该ViewModel里的MutableLiveData进行了observe，
     * 无论在哪里修改ViewModel里面的MutableLiveData的值，这个页面都会收到通知（页面在活跃状态下马上收到，非活跃状态下将在变为活跃状态的那一刻收到），收到通知后调用onChanged()方法（一般是刷新视图）
     * @param application  本项目所设置使用的application实体
     * @param viewModelClass ViewModel对应的类
     * @param <T>
     * @return
     */
    public static <T extends ViewModel> T getViewModel(Application application, Class<T> viewModelClass){
        if (viewModelMap.containsKey(viewModelClass)){
            return  (T) viewModelMap.get(viewModelClass);
        }
        T t = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(viewModelClass);
        viewModelMap.put(viewModelClass, t);
        return t;
    }
    public static <T extends ViewModel> T getViewModel(Fragment fragment, Class<T> viewModelClass){
        return getViewModel(fragment.getActivity(),viewModelClass);
    }
    public static <T extends ViewModel> T getViewModel(Activity activity, Class<T> viewModelClass){
        return getViewModel(activity.getApplication(),viewModelClass);
    }

    /**
     * 获取只在本页面唯一的ViewModel（一般用于保存页面状态，比如横竖屏切换后要恢复状态）
     * @param application 本项目所设置使用的application实体
     * @param viewModelClass ViewModel对应的类
     * @param owner  一般就是AppCompatActivity或Fragment
     * @return
     */
    public static <T extends ViewModel> T getPrivateViewModel(Application application, Class<T> viewModelClass,ViewModelStoreOwner owner){
        ViewModelProvider.Factory factory  = (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        return new ViewModelProvider(owner, factory).get(viewModelClass);
    }
    public static <T extends ViewModel> T getPrivateViewModel(Activity activity, Class<T> viewModelClass,ViewModelStoreOwner owner){
        return getPrivateViewModel(activity.getApplication(),viewModelClass,owner);
    }
    public static <T extends ViewModel> T getPrivateViewModel(Fragment fragment, Class<T> viewModelClass,ViewModelStoreOwner owner){
        return getPrivateViewModel(fragment.getActivity(),viewModelClass,owner);
    }
}
