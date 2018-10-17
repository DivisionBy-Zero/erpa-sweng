package ch.epfl.sweng.erpa.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.erpa.R;

public class MyAccountButtonAdapter extends ArrayAdapter<MyAccountButton> {

    private Context context;
    private List<MyAccountButton> myButtonArrayList;
    private List<Drawable> myDrawables;

    public MyAccountButtonAdapter(Context context, List<MyAccountButton> buttonList,
                                  List<Drawable> drawables) {
        super(context, -1, buttonList);
        this.context = context;
        this.myButtonArrayList = new ArrayList<>(buttonList);
        this.myDrawables = drawables;
    }

    @NonNull @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.my_account_button, parent, false);

        MyAccountButtonHolder myAccountButtonHolder = new MyAccountButtonHolder(convertView,
                position);
        myAccountButtonHolder.setDetails(myButtonArrayList.get(position),
                myDrawables.get(position));

        convertView.setTag(myAccountButtonHolder);

        return convertView;
    }

    public class MyAccountButtonHolder {
        private TextView text;
        private ImageView image;
        int position;

        public MyAccountButtonHolder(View v, int position) {
            text = v.findViewById(R.id.buttonName);
            image = v.findViewById(R.id.buttonImage);
            this.position = position;
        }


        public void setDetails(MyAccountButton myAccountButton, Drawable drawable) {
            text.setText(myAccountButton.getText());
            image.setImageDrawable(drawable);
        }

    }
}
