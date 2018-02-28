package fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lenovo.mypoetry.R;

import model.MyApplication;


/**
 * Created by Lenovo on 2017/12/29.
 */

public class NotesFragment  extends Fragment {

    private Context context;
    private View contentView;
    private MyBroadcastReceiver myBroadcastReceiver;
    private IntentFilter intentFilter;

    private TextView tv_note;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        intentFilter = new IntentFilter();
        intentFilter.addAction("MyPoetry");

        myBroadcastReceiver = new MyBroadcastReceiver();
        context.registerReceiver(myBroadcastReceiver, intentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        contentView = inflater.inflate(R.layout.fragment_notes_layout, null);
        initView();
        return contentView;
    }

    private void initView() {
        tv_note = (TextView) contentView.findViewById(R.id.tv_notes);
    }


    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("Msg");
            if ("PoetryUpdate".equals(msg)) {
                MyApplication myApplication = (MyApplication)getActivity().getApplication();
                tv_note.setText(myApplication.getCurrPoetry().getNotes());
            }
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        context.unregisterReceiver(myBroadcastReceiver);
    }
}