package ch.epfl.sweng.erpa.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.epfl.sweng.erpa.R;

public class MyAccountButtonAdapter extends RecyclerView.Adapter<MyAccountButtonAdapter.MyAccountButtonHolder> {

    private Context context;
    private ArrayList<MyAccountButton> buttonArrayList;
    public MyAccountButtonAdapter(Context context, ArrayList<MyAccountButton> buttonArrayList) {
        this.context = context;
        this.buttonArrayList = buttonArrayList;
    }

    @NonNull @Override
    public MyAccountButtonHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_account_button, viewGroup, false);
        return new MyAccountButtonHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAccountButtonHolder myAccountButtonHolder, int i) {
        MyAccountButton button = buttonArrayList.get(i);
        myAccountButtonHolder.setDetails(button);
    }

    @Override public int getItemCount() {
        return buttonArrayList.size();
    }

    public class MyAccountButtonHolder extends RecyclerView.ViewHolder {
        private TextView text;
        private Class activityClass;
        private ImageView imageView;

        public MyAccountButtonHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.buttonName);
            imageView = itemView.findViewById(R.id.buttonImage);

        }

        public void setDetails(MyAccountButton button) {
            text.setText(button.getText());
            imageView.setImageResource(R.mipmap.ic_launcher);

        }
    }
}

